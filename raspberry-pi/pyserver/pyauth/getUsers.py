import sqlite3

if __name__ == '__main__':
	conn = sqlite3.connect('pyauth/pyauthentication.db')
	c = conn.cursor()
	
	c.execute("SELECT * FROM User;")
	rows = c.fetchall()
	for row in rows:
		print(row)
	
	conn.commit()
	conn.close()