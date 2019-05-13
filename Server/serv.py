from flask import Flask
from flask import request
import mysql.connector as mysql
import time
import json
import hashlib
import os
from user_data import *
secret = json.loads(open("secret.json").read())
db = mysql.connect(host="localhost", user=secret["user"], passwd=secret["passwd"], database="test_user")
cur = db.cursor()

app = Flask(__name__)


@app.route("/")
def enter():
    return "Hello There!"


@app.route("/register", methods=['GET', 'POST'])
def register():
    resp = {}

    print("------->"+request.host)
    email = request.form['email']
    p_hash = request.form['p_hash']
    username = request.form['username']
    vcode = hashlib.sha256(str(str(time.time())+email).encode("utf-8")).hexdigest()

    if is_registered(email):
        resp['status'] = 0
        resp['message'] = 'User is already registered'
    elif is_pending(email):
        resp['status'] = 0
        resp['message'] = "Email Verification is already pending for this Email"
    elif is_username_taken(username):
        resp['status'] = 0
        resp['message'] = "Username is already taken"
    else:
        query= "insert into verify (username,p_hash,vcode,email) values ('%s','%s','%s','%s')" % (username, p_hash, vcode, email)
        print(query)
        cur.execute(query)
        db.commit()
        resp['status'] = 1
        resp['message'] = "Please Check Your Email For Verification"
    return json.dumps(resp)


@app.route("/verify", methods=['GET', 'POST'])
def verify():
    resp = {}
    if request.method=='GET':
        vcode = request.args.get('code')
    elif request.method=='POST':
        vcode = request.form['code']
    print(vcode)
    cur.execute("select * from verify where vcode='%s'" % vcode)
    res = cur.fetchall()
    if not len(res) == 0:
        print(res)
        username=res[0][1]
        email=res[0][2]
        p_hash=res[0][3]
        cur.execute("insert into users (p_hash,email,username) values ('%s','%s','%s')" % (p_hash, email, username))
        cur.execute("delete from verify where vcode='%s'" % vcode)
        db.commit()
        resp['status'] = 1
        resp['message'] = "Registration Successful"
        try:
            os.mkdir(username)
            f = open(os.path.join(os.getcwd(), "%s/data" % username))
        except OSError:
            print("Some Error occurred in creation of User directory")

    else:
        resp['status'] = 0
        resp['message'] = "Invalid code"
    return json.dumps(resp)


@app.route("/fetch", methods=['GET','POST'])
def fetch():
    resp = {}
    if request.method == 'GET':
        username = request.args.get('username')
        p_hash = request.args.get('p_hash')
        req = request.args.get('data')
    elif request.method == 'POST':
        username = request.form['username']
        p_hash = request.form['p_hash']
        req = request.form['data']
    if login(username, p_hash):
        resp['status'] = 1
        handle_fetch(req, username)

    else:
        resp['status'] = 0
        resp['message'] = "Invalid Credentials"
    return json.dumps(resp)


@app.route("/upload", methods=['GET', 'POST'])
def upload():
    resp={}
    data =""
    if request.method == 'GET':
        username = request.args.get('username')
        p_hash = request.args.get('p_hash')
        data=request.args.get('data')
    elif request.method == 'POST':
        username = request.form['username']
        p_hash = request.form['p_hash']
        data=request.form['data']
        handle_upload(data, username)
    if login(username,p_hash):
        resp['status'] = 1

    else:
        resp['status'] = 0
        resp['message'] = "Invalid Credentials"

    return json.dumps(resp)


def is_username_taken (username):
    cur.execute("select * from users where upper(username)=upper('%s')"%username)
    res = cur.fetchall()
    print(res)
    if len(res) != 0:
        return True
    else:
        return False


def is_registered(email):
    cur.execute("select * from users where email='%s'" % email)
    res = cur.fetchall()
    print(res)
    if len(res) != 0:
        return True
    else:
        return False


def is_pending(email):
    cur.execute("select * from verify where email='%s'" % email)
    res = cur.fetchall()
    if len(res) == 0:
        return False
    else:
        return True


def login (email, p_hash):
    cur.execute("select p_hash from users where email='%s'" % email)
    res=cur.fetchall()
    if res[0] == p_hash:
        return True
    else:
        return False


app.run(host="0.0.0.0",threaded=True)
