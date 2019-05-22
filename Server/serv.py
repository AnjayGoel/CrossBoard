from flask import Flask, render_template
from flask import request
from flask_httpauth import HTTPBasicAuth
import mysql.connector as mysql
import hashlib
import os
from methods import *

secret = json.loads(open("secret.json").read())
db = mysql.connect(host="localhost", user=secret["user"], passwd=secret["passwd"], database="test_user")
cur = db.cursor()

app = Flask(__name__)
auth = HTTPBasicAuth()


@app.route("/")
@auth.login_required
def enter():
    return "Hello There!"



@app.route("/login", methods=["POST"])
@auth.login_required
def login_u():
    resp = {}
    email = auth.username()
    resp['status'] = 1
    resp['message'] = "Login Successful"
    cur.execute("select username from users where email='%s'" % email)
    res = cur.fetchall()
    resp['username'] = res[0][0]
    print(resp)
    return json.dumps(resp)


@app.route("/register", methods=['GET', 'POST'])
def register():
    resp = {}
    email = request.form['email']
    p_hash = request.form['p_hash']
    username = request.form['username']
    vcode = hashlib.sha256(str(str(time.time()) + email).encode("utf-8")).hexdigest()

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
        query = "insert into verify (username,p_hash,vcode,email) values ('%s','%s','%s','%s')" % (
        username, p_hash, vcode, email)
        print(query)
        send_mail("admin@crossboard", email, render_template("email_verification_template.html", code=vcode, name=username),"Email Verification")
        cur.execute(query)
        db.commit()
        resp['status'] = 1
        resp['message'] = "Please Check Your Email For Verification"
    return json.dumps(resp)


@app.route("/verify", methods=['GET', 'POST'])
def verify():
    resp = {}
    if request.method == 'GET':
        vcode = request.args.get('code')
    elif request.method == 'POST':
        vcode = request.form['code']
    print(vcode)
    cur.execute("select * from verify where vcode='%s'" % vcode)
    res = cur.fetchall()
    if not len(res) == 0:
        print(res)
        username = res[0][1]
        email = res[0][2]
        p_hash = res[0][3]
        cur.execute("insert into users (p_hash,email,username) values ('%s','%s','%s')" % (p_hash, email, username))
        cur.execute("delete from verify where vcode='%s'" % vcode)
        db.commit()
        resp['status'] = 1
        resp['message'] = "Registration Successful"
        add_user(username, email)

    else:
        resp['status'] = 0
        resp['message'] = "Invalid code"
    return json.dumps(resp)


@app.route("/get", methods=['GET', 'POST'])
@auth.login_required
def fetch():
    time=0
    email = auth.username()
    if request.method == 'GET':
        time = int(request.args.get('time'))
    elif request.method == 'POST':
        time = int(request.form['time'])
    
    resp = handle_fetch (email,time)
    print (pretty(resp))
    return pretty(resp)


@app.route("/post", methods=['GET', 'POST'])
@auth.login_required
def upload():
    resp = {}
    data = ""
    email = auth.username()
    if request.method == 'GET':
        data = request.args.get('data')
    elif request.method == 'POST':
        data = request.form['data']
    str = handle_upload(email, data)
    return pretty(str)


def is_username_taken(username):
    cur.execute("select * from users where upper(username)=upper('%s')" % username)
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


@auth.verify_password
def login(email, p_hash):
    cur.execute("select p_hash from users where email='%s'" % email)
    res = cur.fetchall()
    if len(res) == 0 or res[0][0] != p_hash:
        return False
    else:
        return True


@auth.error_handler
def auth_error():
    return pretty('{"status":0,"messsage":"Invalid Credentials"}')


app.run(host="0.0.0.0", threaded=True)
