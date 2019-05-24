import re
import json
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import smtplib
import time
from pymongo import MongoClient
from bson.json_util import dumps
mongo = MongoClient("localhost", 27017)
db = mongo["CrossBoard"]
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
    res = users.insert_one({"name": username, "email": email, "updated": 0, "data": []})
    print(res)


def handle_fetch(email, time):
    res =  users.aggregate([{"$match":{"email":email}},{"$project":{"entry":{"$filter":{"input":"$entry", "as":"entry","cond":{"$gt":["$$entry.time",time]}}}}},{"$project":{"_id":0,"entry":1}}])
    resp=str({"status":1,"Message":"something","data":dumps(res)})
    return resp


def handle_upload(email, data):
    entry = {}
    entry["data"]=data
    entry["type"]="text" #TODO
    entry["time"] = int(round(time.time() * 1000))
    resp = users.update_one({"email": email}, {"$push": {"entry": entry}})
    users.update_one({"email":email},{"$set":{"updated":entry["time"]}})
   
    return '{"status":1,"time":%s,"type":%s}' % (entry['time'],entry["type"])


def pretty(string):
    return json.dumps(json.loads(string), indent=4)

