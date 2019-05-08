import mysql.connector as mysql
import json

secret = json.loads(open("secret.json").read())
db = mysql.connect(host="localhost", user=secret["user"], passwd=secret["passwd"], database="test_users")
cur = db.cursor()


def get_user(email):
    cur.execute("select * from users where email='%s'" % email)
    return cur.fetchall()


def add_user(name, email, tokenid):
    cur.execute("insert into users(username,email,p_hash) values('%s','%s','%s')" % (name, email, tokenid))
    db.commit()


def print_users():
    cur.execute("select * from users")
    print(cur.fetchall())


def del_user (email):
    cur.execute("delete from users where email='%s'" % email)


print_users()