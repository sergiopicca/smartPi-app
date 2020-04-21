from google.cloud import storage
from pyfcm import FCMNotification
import cv2

class FirebaseUtils:
	
	def __init__(self, bucket, conf):
		self.bucket = bucket
		self.conf = conf
		self.push_service = FCMNotification(api_key=conf["api_key"])

	# Upload image on Dropbox
	def upload(self, frame,ts, notification = True):
		# Save frame on disk
		cv2.imwrite("images/snapshot.jpg", frame)
		# Create blob
		blob = self.bucket.blob('{base_path}/{timestamp}.jpg'.format(base_path = self.conf['base_path'], timestamp=ts))
		# Upload image on firebase
		blob.upload_from_filename("images/snapshot.jpg", content_type="image/jpg")
		# Trigger notification if required
		if(notification):
			# Create notification
			message_title = "Intrusion detected"
			message_body = "ATTENTION! INTRUSION!"
			# Send notification to topic
			result = self.push_service.notify_topic_subscribers(topic_name=self.conf["base_path"], message_body=message_body, message_title = message_title)
		
