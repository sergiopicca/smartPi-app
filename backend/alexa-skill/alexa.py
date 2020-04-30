from flask import Flask
from flask_ask import Ask, statement
# Alexa skill utils
from pyutils import AlexaController
# Waitress (production server)
from waitress import serve


BASE_URL = "http://localhost:3877/" 

alexaController = AlexaController(BASE_URL)

app = Flask(__name__)
ask = Ask(app, '/')

@ask.intent('MotionIntent')
def motion(status):
	global alexaController
	if(status in ["attivare", "active"]):
		res = alexaController.setMotionStatus(1)
		return statement(res)
	elif(status) in ["disattivare", "disable" ]:
		return statement(alexaController.setMotionStatus(0))
	else:
		return statement("Riprova per favore, c'è stato un problema")
		
@ask.intent('FaceIntent')
def face(status):
	global alexaController
	if(status in ["attivare", "active"]):
		return statement(alexaController.setFaceStatus(1))
	elif(status) in ["disattivare", "disable" ]:
		return statement(alexaController.setFaceStatus(0))
	else:
		return statement("Riprova per favore, c'è stato un problema")
	
@ask.intent('AngleIntent')
def angle(number):
	global alexaController
	try:
		angle = int(number)
		if(angle < -90 or angle > 90):
			raise ValueError
		return statement(alexaController.setAngle(angle))
	except ValueError:
		return statement("Angolo non valido")
	
@ask.intent('SnapshotIntent')
def snapshot():
	global alexaController
	return statement(alexaController.getSnapshot())

if __name__ == '__main__':
	serve(app, port=5000)
	

	
	