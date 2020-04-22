# USAGE
# python webstreaming.py 

# utils 
import threading
import argparse
import datetime
import json
import time
import warnings
import os
# auth
from pyauth.pyauthutils import check_login
from pyauth.pyauthutils import token_required
from pyauth.pyauthutils import register
# camera 
from pyimagesearch.motion_detection import SingleMotionDetector
from pyimagesearch.tempimage import TempImage
from imutils.video import VideoStream
import imutils
import cv2
# face detection
import pickle
import face_recognition
# flask 
from flask import Response
from flask import Flask
from flask import render_template
from flask import jsonify
from flask import request
# waitress (production server)
from waitress import serve
# servo motor
from pypwm.servo_motor import ServoMotor
# cloud storage 
from google.cloud import storage
from pyfirebase import FirebaseUtils


# initialize the output frame and a lock used to ensure thread-safe
# exchanges of the output frames (useful for multiple browsers/tabs
# are viewing tthe stream)
outputFrame = None
output_frame_lock = threading.Lock()
# Retrieve backup status of motion and angle
with open('pyconf/data.json') as json_file:
	data = json.load(json_file)
	motion_active = data["motion"]
	face_detection_active = data["face_detection"]
	angle = data["angle"]
# Motion and Face-detection locks
motion_lock = threading.Lock()
face_detection_lock = threading.Lock()
# Servo motor instance and lock
servo_motor = ServoMotor()
servo_lock = threading.Lock()
# initialize a flask object
app = Flask(__name__)

### Connect to storage
# Initialize bucket and conf file for firebase storage
bucket = None
firebase_conf = json.load(open("pyconf/firebase_conf.json"))
# Initialize FirebaseUtils istance
firebase_utils = FirebaseUtils(storage.Client.from_service_account_json('pyconf/firebase_storage.json').bucket(firebase_conf["bucket"]), firebase_conf)

# initialize the video stream and allow the camera sensor to
# warmup
#vs = VideoStream(usePiCamera=1).start()
vs = VideoStream(src=0).start()
time.sleep(2.0)
lastUploaded = datetime.datetime.now()

# Route index
@app.route("/",methods=['GET'])
@token_required
def index():
	response = {"status":"success","message":"Hello, World!"}
	
	return jsonify(response)
	
# Route signup
@app.route("/signup", methods=['POST'])
def signup():
	if 'password' in request.get_json(force=True) and 'house_id' in request.get_json(force = True):
		msg = register(request.json['house_id'], request.json['password'])
		if msg['status'] == 'error':
			return jsonify(msg) , 401
		else:
			return jsonify(msg)
	else:
		msg = {"status" : "error" , "message" : "Please, provide house name, house id and password to login."}
		return jsonify(msg) , 401

# Route login
@app.route("/login", methods=['POST'])
def login():
	if 'password' in request.get_json(force=True):
		msg = check_login(request.json['password'])
		if msg['status'] == 'error':
			return jsonify(msg) , 401
		else:
			return jsonify(msg)
	else:
		msg = {"status" : "error" , "message" : "Please, provide password to login."}
		return jsonify(msg) , 401

# Route stream page
@app.route("/stream",methods=['GET'])
@token_required
def stream():
	# return the rendered template
	return render_template("stream.html")

# Route stream
@app.route("/video_feed")
@token_required
def video_feed():
	# return the response generated along with the specific media
	# type (mime type)
	return Response(generate(), mimetype = "multipart/x-mixed-replace; boundary=frame")
	# return Response(generate(), mimetype = "image/jpeg")

# Generate frame continuosly
def generate():
	# grab global references to the output frame and lock variables
	global outputFrame, lock

	# loop over frames from the output stream
	while True:
		# wait until the lock is acquired
		with output_frame_lock:
			# check if the output frame is available, otherwise skip
			# the iteration of the loop
			if outputFrame is None:
				continue

			# encode the frame in JPEG format
			(flag, encodedImage) = cv2.imencode(".jpg", outputFrame)
			
			# ensure the frame was successfully encoded
			if not flag:
				continue

		# yield the output frame in the byte format
		yield(b'--frame\r\n' b'Content-Type: image/jpeg\r\n\r\n' + 
			bytearray(encodedImage) + b'\r\n')

# Motion and face detection
def compute_frames():
	global vs, outputFrame, output_frame_lock
	global motion_active, motion_lock
	global face_detection_active, face_detection_lock
	global firebase_conf, firebase_utils, ts
	
	motionCounter = 0
	# initialize the motion detector and the total number of frames
	# read thus far
	md = SingleMotionDetector(accumWeight=0.1)
	total = 0
	lastUploaded = datetime.datetime.now()
	
	# loop over frames from the video file stream
	while True:
		
		# grab the frame from the threaded video stream and resize it
		# to 500px (to speedup processing)
		frame = vs.read()
		frame = imutils.resize(frame, width=500)
		
		# FACE DETECTION FRAMES
		# convert the input frame from (1) BGR to grayscale (for face
		# detection) and (2) from BGR to RGB (for face recognition)
		gray_face = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
		rgb_face = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
		
		# MOTION DETECTION FRAME
		gray_motion = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
		gray_motion = cv2.GaussianBlur(gray_motion, (7, 7), 0)

		# grab the current timestamp and draw it on the frame
		timestamp = datetime.datetime.now()
		ts = timestamp.strftime("%A %d %B %Y %I:%M:%S%p")
		cv2.putText(frame, ts, (10, frame.shape[0] - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.35, (0, 0, 255), 1)
		
		## MOTION DETECTION STARTS
		with motion_lock:
			motion_status = motion_active
		if motion_status == True:
			# grab the current timestamp and draw it on the frame
			timestamp = datetime.datetime.now()
			text = "Unoccupied"
			ts = timestamp.strftime("%A %d %B %Y %I:%M:%S%p")
			cv2.putText(frame, ts, (10, frame.shape[0] - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.35, (0, 0, 255), 1)

			# if the total number of frames has reached a sufficient
			# number to construct a reasonable background model, then
			# continue to process the frame
			if total > conf["frame_count"]:
				# detect motion in the image
				motion = md.detect(gray_motion, conf)

				# cehck to see if motion was found in the frame
				if motion is None:
					text = "Unoccupied"
					cv2.putText(frame, "Room Status: {}".format(text), (10, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 2)
					motionCounter = 0
				else:
					text = "Occupied"
					# unpack the tuple and draw the box surrounding the
					# "motion area" on the output frame
					(thresh, (minX, minY, maxX, maxY)) = motion
					cv2.rectangle(frame, (minX, minY), (maxX, maxY), (0, 0, 255), 2)
					cv2.putText(frame, "Room Status: {}".format(text), (10, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 2)
					# Upload photo
					# check to see if enough time has passed between uploads
					if (timestamp - lastUploaded).seconds >= conf["min_upload_seconds"]:
						# increment the motion counter
						motionCounter += 1
					# check to see if the number of frames with consistent motion is
					# high enough
					if motionCounter >= conf["min_motion_frames"]:
						# check to see if firebase sohuld be used
						if firebase_conf["use_firebase"] == False:
							firebase_conf = json.load("pyconf/firebase_conf.json")
						if firebase_conf["use_firebase"]:
							firebase_utils.upload(frame,ts)
						# update the last uploaded timestamp and reset the motion
						# counter
						lastUploaded = timestamp
						motionCounter = 0	
		
		# update the background model and increment the total number
		# of frames read thus far
		md.update(gray_motion)
		total += 1
		## MOTION DETECTION ENDS
		
		## FACE DETECTION STARTS
		with face_detection_lock:
			face_detection_status = face_detection_active
		if face_detection_status == True:
			# put room status text
			cv2.putText(frame, "Face detection active", (10, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 2)
			# detect faces in the grayscale frame
			rects = detector.detectMultiScale(gray_face, scaleFactor=1.1, 
				minNeighbors=5, minSize=(30, 30),
				flags=cv2.CASCADE_SCALE_IMAGE)

			# OpenCV returns bounding box coordinates in (x, y, w, h) order
			# but we need them in (top, right, bottom, left) order, so we
			# need to do a bit of reordering
			boxes = [(y, x + w, y + h, x) for (x, y, w, h) in rects]

			# compute the facial embeddings for each face bounding box
			encodings = face_recognition.face_encodings(rgb_face, boxes)
			names = []
			known_face = False
			
			# loop over the facial embeddings
			for encoding in encodings:
				# attempt to match each face in the input image to our known
				# encodings
				matches = face_recognition.compare_faces(data["encodings"], encoding)
				name = "Unknown"

				# check to see if we have found a match
				if True in matches:
					known_face = True
					# find the indexes of all matched faces then initialize a
					# dictionary to count the total number of times each face
					# was matched
					matchedIdxs = [i for (i, b) in enumerate(matches) if b]
					counts = {}

					# loop over the matched indexes and maintain a count for
					# each recognized face face
					for i in matchedIdxs:
						name = data["names"][i]
						counts[name] = counts.get(name, 0) + 1

					# determine the recognized face with the largest number
					# of votes (note: in the event of an unlikely tie Python
					# will select first entry in the dictionary)
					name = max(counts, key=counts.get)
				# check if there is a known face in the frame
				# update the list of names
				names.append(name)
			
			# loop over the recognized faces
			for ((top, right, bottom, left), name) in zip(boxes, names):
				# draw the predicted face name on the image
				cv2.rectangle(frame, (left, top), (right, bottom),
					(0, 255, 0), 2)
				y = top - 15 if top - 15 > 15 else top + 15
				cv2.putText(frame, name, (left, y), cv2.FONT_HERSHEY_SIMPLEX,
					0.75, (0, 255, 0), 2)
			# upload photo
			if (timestamp - lastUploaded).seconds >= conf["min_upload_seconds"]:
				# increment the motion counter
				motionCounter += 1
			if firebase_conf["use_firebase"] == False:
				# Check if use_firebase condition is changed
				firebase_conf = json.load("pyconf/firebase_conf.json")
			if motionCounter >= conf["min_motion_frames"] and firebase_conf["use_firebase"] and boxes and not known_face:
				# Upload image
				firebase_utils.upload(frame,ts)	
				# Reset motion counter	
				motionCounter = 0
			# Reset motion counter if no face detected
			if not boxes:
				motionCounter = 0
		## FACE DETECTION ENDS
		
		# STREAM OUTPUT
		with output_frame_lock:
			outputFrame = frame.copy()

# Route set/get motion
@app.route("/motion",methods=['GET'])
@token_required
def setMotionStatus():
	global motion_active, motion_lock, face_detection_active, face_detection_lock
	if 'motion' in request.args:
		try:
			value = int(request.args['motion'])
		except ValueError:
			msg = { "status": "error","message":"Motion field invalid. Please specify 1 (to active the motion) or 0 (to disable it)"}
			return jsonify(msg)
		with motion_lock:
			motion_active = bool(value)
			if motion_active == True:
				# Disable motion 
				with face_detection_lock:
					face_detection_active = False
				# Update data.json
				with open('pyconf/data.json') as json_file:
					data = json.load(json_file)
					data["motion"] = True
					data["face_detection"] = False
				with open('pyconf/data.json', 'w') as outfile:
					json.dump(data, outfile)
				# return message
				msg = { "status": "success","message":"Motion activated"}
			else:
				with open('pyconf/data.json') as json_file:
					data = json.load(json_file)
					data["motion"] = False
				with open('pyconf/data.json', 'w') as outfile:
					json.dump(data, outfile)
				msg = { "status": "success","message":"Motion disabled"}
		return jsonify(msg)		
	else:
		if motion_active == True:
			msg = { "status": "success","message":"Motion active"}
		else:
			msg = { "status": "success","message":"Motion not active"}
		return jsonify(msg)

# Route set/get motion
@app.route("/face",methods=['GET'])
@token_required
def setFaceDetectionStatus():
	global face_detection_active, face_detection_lock, motion_active, motion_lock
	if 'detection' in request.args:
		try:
			value = int(request.args['detection'])
		except ValueError:
			msg = { "status": "error","message":"Detection field invalid. Please specify 1 (to active the face detection) or 0 (to disable it)"}
			return jsonify(msg)
		with face_detection_lock:
			face_detection_active = bool(value)
			if face_detection_active == True:
				# Disable motion
				with motion_lock:
					motion_active = False
				# Update data.json
				with open('pyconf/data.json') as json_file:
					data = json.load(json_file)
					data["face_detection"] = True
					data["motion"] = False
				with open('pyconf/data.json', 'w') as outfile:
					json.dump(data, outfile)
				# return message
				msg = { "status": "success","message":"Face detection activated"}
			else:
				with open('pyconf/data.json') as json_file:
					data = json.load(json_file)
					data["face_detection"] = False
				with open('pyconf/data.json', 'w') as outfile:
					json.dump(data, outfile)
				msg = { "status": "success","message":"Face detection disabled"}
		return jsonify(msg)		
	else:
		if face_detection_active == True:
			msg = { "status": "success","message":"Face detection active"}
		else:
			msg = { "status": "success","message":"Face detection not active"}
		return jsonify(msg)
		
# Route set/get angle
@app.route("/angle",methods=['GET'])
@token_required
def setAngle():
	global angle, servo_motor, servo_lock
	if 'angle' in request.args:
		try:
			value = int(request.args['angle'])
			if value < -90 or value > 90:
				raise ValueError() 
			angle = value
			## change angle
			with servo_lock:
				servo_motor.setAngle(angle)
			with open('pyconf/data.json') as json_file:
				data = json.load(json_file)
				data["angle"] = angle
			with open('pyconf/data.json', 'w') as outfile:
				json.dump(data, outfile)
			msg = { "status": "success","message":"{angle}".format(angle = str(angle))}
			return jsonify(msg)
		except ValueError:
			msg = { "status": "error","message":"angle field invalid. Please insert a number between -90 to 90"}
			return jsonify(msg)
	else:
		msg = { "status": "success","message":"{angle}".format(angle = str(angle))}
		return jsonify(msg)
		
# Route get snapshot
@app.route("/snapshot",methods=['GET'])
@token_required
def getSnapshot():
	global outputFrame, output_frame_lock
	timestamp = datetime.datetime.now()
	ts = timestamp.strftime("%A %d %B %Y %I:%M:%S%p")
	with output_frame_lock:
		frame = outputFrame.copy()	
	firebase_utils.upload(frame, ts, notification = False)
	msg = { "status": "success","message":"[{filename}] saved successfully".format(filename = ts)}
	return jsonify(msg)
	
# Main Thread
if __name__ == '__main__':
	
	# Get configuration file
	conf = json.load(open("pyconf/conf.json"))
	
	# filter warnings, load the configuration and initialize the Dropbox client
	warnings.filterwarnings("ignore")
	client = None
	
	# load the known faces and embeddings along with OpenCV's Haar
	# cascade for face detection
	data = pickle.loads(open(conf["encodings"], "rb").read())
	detector = cv2.CascadeClassifier(conf["cascade"])
	print("[SUCCESS] encodings + face detector loaded")

	# start a thread that will perform motion detection
	t = threading.Thread(target=compute_frames)
	t.daemon = True
	t.start()
	
	### run development server
	# os.environ["FLASK_ENV"] = "development"
	## start the flask app
	# app.run(host=conf["ip_address"], port=conf["port"], debug=True, threaded=True, use_reloader=False)
	### run production server
	serve(app, host = conf['ip_address'], port = conf['port'])
	

# release the video stream
vs.stop()