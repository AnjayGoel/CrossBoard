import re
import json
import time

def handle_fetch(req_info, username):
    req = json.loads(req_info)
    f = open("/%s/data" % username, 'r')
    data = map(f.read().split("\n->|", json.loads()))
    return data

def handle_upload(req, username):
    entry = json.loads(req);
    if is_url(entry['data']):
        entry['type'] = "link"
    else:
        entry['type'] = "text"
    f = open("/%s/data" % username,'a')
    f.write("\n->|"+json.dumps(entry))
    f.close()



def is_url(text):
    return re.findall('http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+', text)[0] == text