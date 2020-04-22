import sqlite3
import hashlib
from getpass import getpass

if __name__ == '__main__':
	conn = sqlite3.connect('pyauth/pyauthentication.db')
	c = conn.cursor()
	
	c.execute("CREATE TABLE IF NOT EXISTS User (username varchar(32) PRIMARY KEY, password_digest varchar(64), house_id varchar(32));")
	username = input("Enter username: ")
	while True:
		password = getpass("Enter password: ")
		password_confirmation = getpass("Confirm password: ")
		if password == password_confirmation:
			break
		print("Retype password.\n")
		
	password_digest = hashlib.sha256(password.encode('utf-8')).hexdigest()
	query = "INSERT INTO User (username, password_digest) VALUES ('{username}', '{pass_dig}')".format(username = username, pass_dig = password_digest)
	c.execute(query)
	c.execute("SELECT * FROM User;")
	rows = c.fetchall()
	for row in rows:
		print(row)
	
	conn.commit()
	conn.close()
	
