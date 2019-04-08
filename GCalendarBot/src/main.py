# Python Standard Library
import os
import sys
import json
import logging
import datetime
import calendar

# Dialogs Python Bot SDK
import grpc
from dialog_bot_sdk.bot import DialogBot

from GCaluAPI import GCaluAPI


# Prevent bot from crashing
def raw_call(*args, **kwargs):
    pass


def get_quarter_range(year: int, q: int, tz: datetime.timezone = None) -> tuple:
    """
    returns (start, end) datetime of selected quarter
    :param year: year
    :param q: quarter of the year 1-4
    :param tz: timezone (datetime.timezone)
    :return: tuple(start,end) as datetime.datetime
    """
    return (datetime.datetime(year, 3 * q - 2, 1, tzinfo=tz),
            datetime.datetime(year, 3 * q, calendar.monthrange(year, 3 * q)[1], tzinfo=tz))


def get_full_month_range(year: int, month: int, tz: datetime.timezone = None) -> tuple:
    """
    returns (start, end) datetime of selected month
    :param year: year
    :param month: month
    :param tz: timezone (datetime.timezone)
    :return: tuple(start,end) as datetime.datetime
    """
    return (datetime.datetime(year, month, 1, tzinfo=tz),
            datetime.datetime(year, month, calendar.monthrange(year, month)[1], tzinfo=tz))


def get_event_list(calendar_id: str, event_limit: int = 128, start: datetime.datetime = None,
                   end: datetime.datetime = None):
    """
    returns a text representing list of all events in range (start,end) dates 1 event per line
    :param calendar_id: id of calendar
    :param event_limit: maximum number of events to output
    :param start: start date
    :param end: end time
    :return: str
    """
    fmt = "%d/%m-%H:%M"
    fmt_b = "%H:%M"
    out = ""
    for evt in API.get_following_events(calendar_id, start, event_limit, end):
        event_start = datetime.datetime.fromisoformat(evt["start"]["dateTime"])
        event_end = datetime.datetime.fromisoformat(evt["end"]["dateTime"])
        event_delta = event_end - event_start
        out += ">{0}->{1} => {2} ({3})".format(
            event_start.strftime(fmt),
            event_end.strftime(fmt if event_delta.days > 0 else fmt_b),
            evt["summary"],
            evt["description"])
        out += "\n"
    return out


def on_msg(*params):
    for param in params:
        log.debug("onMsg -> {}".format(param))
        try:
            if param.peer.id == param.sender_uid:
                if SETTINGS["user_id"] and param.sender_uid not in SETTINGS["user_id"]:
                    # User Not authorized
                    continue
                txt = param.message.textMessage.text.lower()
                now = datetime.datetime.now(TZONE)

                if txt.startswith(SETTINGS["command0"]):
                    r = get_event_list(SETTINGS["calendar_id"], start=now, event_limit=SETTINGS["num_events_limit"])
                    r = "List of Upcoming Events:\n" + r
                    bot.messaging.send_message(param.peer, r)  # r

                elif txt.startswith(SETTINGS["command1"]):
                    blocks = txt.split(" ")
                    if len(blocks) >= 3:
                        month = blocks[2].lower()
                        if month in MONTHS:
                            start_month, end_month = get_full_month_range(now.year, MONTHS[month], TZONE)
                            r = get_event_list(SETTINGS["calendar_id"], start=start_month, end=end_month,
                                               event_limit=SETTINGS["num_events_limit"])
                            r = "List of Upcoming Events of {0}:\n".format(str(month)) + r
                            if len(r) > 0:
                                bot.messaging.send_message(param.peer, r)
                            else:
                                bot.messaging.send_message(param.peer, "No Events")
                        else:
                            bot.messaging.send_message(param.peer, "invalid month, try again.")

                elif txt.startswith(SETTINGS["command2"]):
                    blocks = txt.split(" ")
                    if len(blocks) >= 3:
                        try:
                            qrt = int(blocks[2])
                            if qrt < 1 or qrt > 4:  # ValueError
                                raise ValueError()
                        except ValueError:
                            bot.messaging.send_message(param.peer, "invalid quarter, try again.")
                            continue

                        start, end = get_quarter_range(now.year, qrt, TZONE)
                        r = get_event_list(SETTINGS["calendar_id"], start=start, end=end,
                                           event_limit=SETTINGS["num_events_limit"])
                        r = "List of Upcoming Events of Q{0}:\n".format(qrt) + r
                        if len(r) > 0:
                            bot.messaging.send_message(param.peer, r)
                        else:
                            bot.messaging.send_message(param.peer, "No Events")
                else:
                    bot.messaging.send_message(param.peer, HELP_TEXT)
        except:
            log.error("Exception", exc_info=True)
            continue


if __name__ == '__main__':
    SETTINGS_PATH = "../settings.json"

    MONTHS = {"jan": 1, "feb": 2, "mar": 3, "apr": 4, "may": 5, "jun": 6,
              "jul": 7, "aug": 8, "sep": 9, "oct": 10, "nov": 11, "dec": 12}

    log = logging.getLogger()
    log.setLevel(logging.INFO)
    log.addHandler(logging.StreamHandler())

    if os.path.exists(SETTINGS_PATH):
        try:
            SETTINGS = json.load(open(SETTINGS_PATH))
            TZONE = datetime.datetime.strptime(SETTINGS["timezone"], "%z").tzinfo
        except:
            log.error("Can't load settings", exc_info=True)
            sys.exit(1)

        HELP_TEXT = "This bot can help you, manage your calendar events.\n" \
                    ">send \"{0}\" to show all upcoming events.\n" \
                    ">send \"{1} <MONTH>\" to show all events of the month.\n" \
                    ">send \"{2} <QUARTER>\" to show all events of the QUARTER.\n".format(SETTINGS["command0"],
                                                                                          SETTINGS["command1"],
                                                                                          SETTINGS["command2"])
        # Initialize API
        try:
            API = GCaluAPI(SETTINGS["credentials_file"], SETTINGS["token_file"])
        except:
            log.error("Can't initialize GoogleuAPI", exc_info=True)
            sys.exit(1)

        # Enable this flag and use this to obtain google calendar id
        if SETTINGS["show_calendars"]:
            for current_cal in API.get_calendars():
                log.info("{0} => {1}".format(current_cal["id"], current_cal["summary"]))

        try:
            bot = DialogBot.get_secure_bot(
                os.environ.get('BOT_ENDPOINT'),  # bot endpoint
                grpc.ssl_channel_credentials(),  # SSL credentials (empty by default!)
                os.environ.get('BOT_TOKEN')  # bot token
            )
            bot.messaging.on_message(on_msg, raw_callback=raw_call)
        except:
            log.error("Can't initialize bot", exc_info=True)
            sys.exit(1)

    else:
        log.error("{0} not found. Create one using settings_default.json as reference.".format(SETTINGS_PATH),
                  exc_info=True)
        sys.exit(1)
