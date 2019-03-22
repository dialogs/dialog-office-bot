# Python Standard Library
import os
import sys
import json
import logging
import urllib.parse

# Dialogs Python Bot SDK
import grpc
from dialog_bot_sdk.bot import DialogBot

# Extra
import requests
from bs4 import BeautifulSoup


def adapt_confluence_page(api_endpoint: str, page_id: int,
                          http_auth_user: str, http_auth_password: str,
                          base_url: str = None, params: dict = None,
                          link_to_url: bool = True, emoji: bool = False, ulist_pin: str = "->") -> str:
    """
    Retrieve confluence page content and adapt it for non-browser usage (html to text)

    :param api_endpoint: confluence API endpoint url (scheme required)
    :param page_id: id of required page
    :param http_auth_user: username
    :param http_auth_password: password
    :param base_url: domain with scheme that must be used to fix relative urls by default api_endpoint domain is used
    :param params: optional params for GET request
    :param link_to_url: if true link's url is shown in text output
    :param emoji: if true emoji-text is shown in text output
    :param ulist_pin: unordered list indicator

    :return: (str) Confluence page text
    """
    # url_params
    if params is None:
        params = {"expand": "body.view"}
    url_params = urllib.parse.urlencode(params)

    # base_url
    if base_url is None:
        tmp = urllib.parse.urlsplit(api_endpoint)
        base_url = "{0}://{1}".format(tmp.scheme, tmp.netloc)

    out = ""

    # Perform API GET request
    url = "{0}/rest/api/content/{1}?{2}".format(api_endpoint, page_id, url_params)
    with requests.get(url, auth=(http_auth_user, http_auth_password)) as req:
        if req.status_code == 200:  # Success
            data = req.json()  # Parse JSON response
            html = data["body"]["view"]["value"]  # Go to the interesting stuff
            parser = BeautifulSoup(html, "html.parser")  # Parse the interesting stuff

            if link_to_url:
                # Replace links (<a href="myurl.com">mysite</a>) with <p>mysite: myurl.com</p>
                # in this way url can be visualized (and clicked) by the end user
                for a in parser.findAll("a"):
                    rtag = parser.new_tag("p")
                    # Fix relative urls using urljoin
                    rtag.string = "{0}:{1}".format(a.string, urllib.parse.urljoin(base_url, a.get("href")))
                    a.replace_with(rtag)

            if emoji:
                # Confluence uses a jpeg image to show an emoji
                # luckily a param 'data-emoji-fallback' is available to get a text representation of the emoji
                # Ex: :check_mark:
                for img in parser.findAll("img"):
                    fallback = img.get("data-emoji-fallback")
                    if fallback:
                        rtag = parser.new_tag("p")
                        rtag.string = str(fallback)
                        img.replace_with(rtag)

            # Just extract text from html and fix some elements and newlines(\n)
            for current_child in parser.children:
                if current_child.name == "ul":  # We got a list, let's show it in a clear way
                    out += ulist_pin
                    out += current_child.get_text("\n" + ulist_pin)
                    out += "\n" * 2

                elif current_child.name == "ol":  # We got an ordered list, let's show it in a clear way
                    count = 0
                    for item in current_child.find_all("li"):
                        count += 1
                        out += "{0}){1}".format(count, item.get_text())
                        out += "\n"
                    out += "\n" * 2

                elif current_child.name == "p":  # After each paragraph add a new line
                    out += current_child.text
                    out += "\n"

                elif current_child.name in ("h1", "h2", "h3", "h4", "h5", "h6"):  # Heading
                    out += "\n"
                    out += current_child.text
                    out += "\n" * 2

                elif current_child.name == "hr":  # Horizontal spacer
                    out += "\n"

                elif (current_child.name == "div") and ("table-wrap" in current_child.get("class")):  # Table
                    out += "\n" * 2
                    for table_row in current_child.find_all("tr"):
                        out += table_row.get_text(" ")
                        out += "\n"
                    out += "\n"
                else:
                    out += current_child.text
            return out
        else:
            req.raise_for_status()


def handle_new_user(options, uid):
    try:
        if "welcome_message" in options and options["welcome_message"]:
            bot.messaging.send_message(uid, options["welcome_message"])
            log.info("Welcome Message Sent.")

        if "confluence_pages" in options:
            for confluence_page_id in options["confluence_pages"]:
                text = adapt_confluence_page(options["confluence_domain"], confluence_page_id,
                                             options["confluence_username"], options["confluence_password"])
                bot.messaging.send_message(uid, text)
                log.info("confluence page {0} Sent.".format(confluence_page_id))
    except:
        log.error("handle_new_user failed", exc_info=True)


def check_group(group_id):
    # Should I care about this specific group? if its ID is in settings.json YES!
    try:
        options = SETTINGS[str(group_id)]
        log.info("Group settings found.")
        return options
    except KeyError:
        log.error("No settings for this group. maybe you want add GROUP ID: {} in settings.json.".format(group_id))
        return None


def raw_handler(update):
    if update.WhichOneof("update") == 'updateGroupUserInvitedObsolete':
        log.info("User {0} joined {1} group".format(update.updateGroupUserInvitedObsolete.uid,
                                                    update.updateGroupUserInvitedObsolete.group_id))
        options = check_group(update.updateGroupUserInvitedObsolete.group_id)
        if options:
            user_id = bot.users.get_user_outpeer_by_id(update.updateGroupUserInvitedObsolete.uid)
            if user_id:
                handle_new_user(options, user_id)


def on_msg(*params):
    for param in params:
        log.debug("onMsg -> {}".format(param))


if __name__ == '__main__':
    SETTINGS_PATH = "../settings.json"

    log = logging.getLogger()
    log.setLevel(logging.INFO)
    log.addHandler(logging.StreamHandler())

    if os.path.exists(SETTINGS_PATH):
        try:
            SETTINGS = json.load(open(SETTINGS_PATH))
        except:
            log.error("Can't load settings", exc_info=True)
            sys.exit(1)

        try:
            bot = DialogBot.get_secure_bot(
                os.environ.get('BOT_ENDPOINT'),  # bot endpoint
                grpc.ssl_channel_credentials(),  # SSL credentials (empty by default!)
                os.environ.get('BOT_TOKEN')  # bot token
            )

            # bot.messaging.on_message(on_msg)
            bot.messaging.on_message(on_msg, raw_callback=raw_handler)
            log.error("Ready")
        except:
            log.error("Can't initialize bot", exc_info=True)
            sys.exit(1)
    else:
        log.error("{0} not found. Create one using settings_default.json as reference.".format(SETTINGS_PATH),
                  exc_info=True)
        sys.exit(1)
