import re
import json
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import smtplib
import time
from pymongo import MongoClient
from bson.json_util import dumps
mongo = MongoClient("localhost", 27017)
db = mongo["crossboard"]
users = db["user_data"]
email_config = json.loads(open("email_config.json").read())


def send_mail(sender_addr, addr, message, subject=""):
    s = smtplib.SMTP(host=email_config["server"], port=email_config["port"])
    s.login(email_config["username"], email_config["password"])
    msg = MIMEMultipart()

    msg['From'] = sender_addr
    msg['To'] = addr
    msg['Subject'] = subject

    msg.attach(MIMEText(message, 'html'))
    s.send_message(msg)


def add_user(username, email):
    res = users.insert_one({"name": username, "email": email, "data": []})
    print(res)


def handle_fetch(email, req):
    req = json.loads(req)
    res = users.find({"email": email, "data.time": {"$gt": req['time']}}, {'data': 1, '_id': 0})
    return dumps(res[0])


def handle_upload(email, req):
    req = json.loads(req)
    req['time'] = int(round(time.time() * 1000))
    users.update_one({"email": email}, {"$push": {"data": req}})
    return '{"status":1,"time":%s}' % req['time']


print(handle_fetch("angoel123@gmail.com", '{"time":1558281581231}'))
