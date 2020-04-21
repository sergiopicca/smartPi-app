import sqlite3
import hashlib
import jwt
import datetime
import os
import sys
from functools import wraps
from flask import request
from flask import jsonify
import json

key = hashlib.sha256(str(datetime.datetime.utcnow()).encode('ASCII') + str(int.from_bytes(os.urandom(32),sys.byteorder)).encode('ASCII')).hexdigest()

def check_login(password):
	global key
	# pass digest
	password_digest = hashlib.sha256(password.encode('utf-8')).hexdigest()
	# open db
	try:
		conn = sqlite3.connect('pyauth/pyauthentication.db')
		c = conn.cursor()
		# Check credentials
		c.execute(" SELECT * FROM User WHERE password_digest = '{password_digest}'".format(password_digest = password_digest))
		if not c.fetchall():
			raise sqlite3.Error()
		conn.close()
		# Create token
		token = jwt.encode({'user' : hashlib.sha1(password.encode('UTF-8')).hexdigest(), 'exp' : datetime.datetime.utcnow() + datetime.timedelta(hours = 24)} , key).decode('UTF-8')
		# token = jwt.encode({'user' : hashlib.sha1(password.encode('UTF-8')).hexdigest(), 'exp' : datetime.datetime.utcnow() + datetime.timedelta(seconds = 10)} , key).decode('UTF-8')
		
		return {"status":"success" , "auth-token" : token}
	except sqlite3.Error:
		conn.close()
		return {"status":"error", "message":"Invalid credential"}

def register(house_id, password):
	global key
	# pass digest
	password_digest = hashlib.sha256(password.encode('utf-8')).hexdigest()
	# open db
	try:
		conn = sqlite3.connect('pyauth/pyauthentication.db')
		c = conn.cursor()
		# Check credentials
		c.execute(" SELECT password_digest FROM User WHERE password_digest = '{password_digest}'".format(password_digest = password_digest))
		if not c.fetchall():
			raise sqlite3.Error()
		# Check if house_id already exist
		c.execute("SELECT house_id FROM User".format(password_digest = password_digest))
		if not c.fetchall()[0][0]:
			# Save the house id
			c.execute("UPDATE User SET house_id = '{house_id}'".format(house_id = house_id, password_digest = password_digest))
			conn.commit()
			# Active the firebase upload
			with open('pyconf/firebase_conf.json') as json_file:
				data = json.load(json_file)
				data["use_firebase"] = True
				data["base_path"] = house_id
			with open('pyconf/firebase_conf.json', 'w') as output:
				json.dump(data, output)
		# Close connection to db
		conn.close()
		# Create token
		token = jwt.encode({'user' : hashlib.sha1(password.encode('UTF-8')).hexdigest(), 'exp' : datetime.datetime.utcnow() + datetime.timedelta(hours = 24)} , key).decode('UTF-8')
		# token = jwt.encode({'user' : hashlib.sha1(password.encode('UTF-8')).hexdigest(), 'exp' : datetime.datetime.utcnow() + datetime.timedelta(seconds = 10)} , key).decode('UTF-8')
		return {"status":"success" , "auth-token" : token}
	except sqlite3.Error as e:
		# print(e)
		conn.close()
		return {"status":"error", "message":"Invalid credential"}

def token_required(f):
	@wraps(f)
	def decorated(*args, **kwargs):
		global key
		token = request.headers.get('Authorization')
		if not token:
			return jsonify({"status":"error" , "message":"token missing"}), 401
		try:
			decoded = jwt.decode(token, key)
		except Exception as e:
			# print(str(e))
			if str(e) == 'Signature has expired':
				return jsonify({"status":"error" , "message":"token expired"}), 401
			else:
				return jsonify({"status":"error" , "message":"token invalid"}), 401
		return f(*args, **kwargs)
	
	return decorated
