import requests

class AlexaController:
	
	def __init__(self, baseUrl):
		self.baseUrl = baseUrl
		self.token = None
		
	def getToken(self):
		url = self.baseUrl+"login"
		params = '{"house_name":"<House_name>","password":"<password>"}'
		# sending get request and saving the response as response object 
		r = requests.post(url = url, data = params)
		if r.status_code == 200:
			# extracting data in json format 
			data = r.json()
			return data["auth-token"]
		else:
			return None
			
	def setMotionStatus(self, status):
		if(not self.token):
			self.token = self.getToken()
		# Active motion detection
		url = self.baseUrl+"motion"
		params = {"motion": status }
		headers = {'Authorization': self.token}
		r = requests.get(url = url, params = params, headers = headers)
		# If token expired, get new token and retry request
		if(r.status_code == 401):
			if(r.json()["message"] == "token expired"):
				self.token = self.getToken()
				headers2 = {'Authorization': self.token}
				r = requests.get(url = url, params = params, headers = headers2)
			elif(r.json()["message"] == "token invalid"):
				self.token = None
				return "Ripetere richiesta per favore"
			else:
				return "Riprova per favore, c'è stato un problema"
		# Check if response valid
		if(r.status_code == 200 and r.json()["status"] == "success"):
			if(status == 1):
				return "Rilevazione movimento attivata"
			elif(status == 0):
				return "Rilevazione movimento disattivata"
		else:
			return "Riprova per favore, c'è stato un problema"
		
	def setFaceStatus(self, status):
		if(not self.token):
			self.token = self.getToken()
		# Active motion detection
		url = self.baseUrl+"face"
		params = {"detection": status }
		headers = {'Authorization': self.token}
		r = requests.get(url = url, params = params, headers = headers)
		# If token expired, get new token and retry request
		if(r.status_code == 401):
			if(r.json()["message"] == "token expired"):
				self.token = self.getToken()
				headers2 = {'Authorization': self.token}
				r = requests.get(url = url, params = params, headers = headers2)
			elif(r.json()["message"] == "token invalid"):
				self.token = None
				return "Ripetere richiesta per favore"
			else:
				return "Riprova per favore, c'è stato un problema"
		# Check if response valid
		if(r.status_code == 200 and r.json()["status"] == "success"):
			if(status == 1):
				return "Riconoscimento del viso attivato"
			elif(status == 0):
				return "Riconoscimento del viso disattivato"
		else:
			return "Riprova per favore, c'è stato un problema"
	
	def setAngle(self, angle):
		if(not self.token):
			self.token = self.getToken()
		# Active motion detection
		url = self.baseUrl+"angle"
		params = {"angle": angle }
		headers = {'Authorization': self.token}
		r = requests.get(url = url, params = params, headers = headers)
		# If token expired, get new token and retry request
		if(r.status_code == 401):
			if(r.json()["message"] == "token expired"):
				self.token = self.getToken()
				headers2 = {'Authorization': self.token}
				r = requests.get(url = url, params = params, headers = headers2)
			elif(r.json()["message"] == "token invalid"):
				self.token = None
				return "Ripetere richiesta per favore"
			else:
				return "Riprova per favore, c'è stato un problema"
		# Check if response valid
		if(r.status_code == 200 and r.json()["status"] == "success"):
			return "Camera spostata a {angle} gradi".format(angle = angle)
		else:
			return "Riprova per favore, c'è stato un problema"

	def getSnapshot(self):
		if(not self.token):
			self.token = self.getToken()
		# Active motion detection
		url = self.baseUrl+"snapshot"
		headers = {'Authorization': self.token}
		r = requests.get(url = url, headers = headers)
		# If token expired, get new token and retry request
		if(r.status_code == 401):
			if(r.json()["message"] == "token expired"):
				self.token = self.getToken()
				headers2 = {'Authorization': self.token}
				r = requests.get(url = url, headers = headers2)
			elif(r.json()["message"] == "token invalid"):
				self.token = None
				return "Ripetere richiesta per favore"
			else:
				return "Riprova per favore, c'è stato un problema"
		# Check if response valid
		if(r.status_code == 200 and r.json()["status"] == "success"):
			return "Foto scattata"
		else:
			return "Riprova per favore, c'è stato un problema"