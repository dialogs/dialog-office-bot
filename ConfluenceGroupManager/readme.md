# Dialog-Office-bot
## Setup
```bash
cd Dialog-Office-bot
python3 -m venv ./venv
source ./venv/bin/activate
pip3 install -r requirements.txt
git clone https://github.com/dialogs/python-bot-sdk
cd python-bot-sdk
python3 setup.py install
cd ../src/
python3 main.py
```

Don't forget to setup all your ***GROUP_ID(S)*** in `settings.json`.
to get the group id of your group start the bot, send a message in the group and check the logs.

### Note
The user MUST start a conversation with the bot first, otherwise it can receive messages from that bot.

