# Python Standard Library
import datetime
import os
import pickle

# Google API
from googleapiclient.discovery import build
from google_auth_oauthlib.flow import InstalledAppFlow
from google.auth.transport.requests import Request


class GCaluAPI():
    """ Google Calendar uAPI (microAPI)"""
    SCOPES = ['https://www.googleapis.com/auth/calendar.readonly']
    # If modifying ^these^ scopes, delete the file token.pickle.

    def __init__(self, creds_json="../credentials.json", pickle_file="token.pickle"):
        """
        Google Calendar uAPI (microAPI)
        :param creds_json: json with credentials downloaded from google
        :param pickle_file: file to store access token
        """
        super(GCaluAPI, self).__init__()
        self._creds_json = creds_json
        self._pickle_file = pickle_file

        self.service = build('calendar', 'v3', credentials=self._auth_creds())

    def _auth_creds(self):
        creds = None
        # The file token.pickle stores the user's access and refresh tokens, and is
        # created automatically when the authorization flow completes for the first
        # time.
        if os.path.exists(self._pickle_file):
            with open(self._pickle_file, 'rb') as token:
                creds = pickle.load(token)
        # If there are no (valid) credentials available, let the user log in.
        if not creds or not creds.valid:
            if creds and creds.expired and creds.refresh_token:
                creds.refresh(Request())
            else:
                flow = InstalledAppFlow.from_client_secrets_file(self._creds_json, self.SCOPES)
                creds = flow.run_local_server()
            # Save the credentials for the next run
            with open(self._pickle_file, 'wb') as token:
                pickle.dump(creds, token)
        return creds

    def get_calendars(self) -> list:
        """
        returns a list of all available calendars useful to retrieve calendar_id
        :return: list of all calendars
        """
        page_token = None
        out = []
        while True:
            calendar_list = self.service.calendarList().list(pageToken=page_token).execute()
            for calendar_list_entry in calendar_list['items']:
                out.append(calendar_list_entry)
            page_token = calendar_list.get('nextPageToken')
            if not page_token:
                break
        return out

    def get_following_events(self, calendar_id: str, start: datetime.datetime, limit: int = 10,
                             end: datetime.datetime = None):
        """
        returns all events of a specific calendar in range (start, end)
        :param calendar_id: id of google calendar
        :param start: datetime beginning of range
        :param limit: maximum number of events to return
        :param end: datetime end of range
        :return: list of json-parse events directly from google API
        """
        # Call the Calendar API
        start = start.isoformat()
        if end:
            end = end.isoformat()

        events_result = self.service.events().list(calendarId=calendar_id, timeMin=start, timeMax=end, maxResults=limit,
                                                   singleEvents=True, orderBy='startTime').execute()
        return events_result.get('items', [])
