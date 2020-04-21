# import the necessary packages
import numpy as np
import imutils
import cv2

class SingleMotionDetector:
	def __init__(self, accumWeight=0.5):
		# store the accumulated weight factor
		self.accumWeight = accumWeight

		# initialize the background model
		self.bg = None

	def update(self, image):
		# if the background model is None, initialize it
		if self.bg is None:
			self.bg = image.copy().astype("float")
			return

		# update the background model by accumulating the weighted
		# average
		cv2.accumulateWeighted(image, self.bg, self.accumWeight)

	def detect(self, image, conf):
		# compute the absolute difference between the background model
		# and the image passed in, then threshold the delta image
		delta = cv2.absdiff(self.bg.astype("uint8"), image)
		thresh = cv2.threshold(delta, conf["delta_thresh"], 255, cv2.THRESH_BINARY)[1]

		# perform a series of erosions and dilations to remove small
		# blobs
		thresh = cv2.erode(thresh, None, iterations=2)
		thresh = cv2.dilate(thresh, None, iterations=2)

		# find contours in the thresholded image and initialize the
		# minimum and maximum bounding box regions for motion
		cnts = cv2.findContours(thresh.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
		cnts = imutils.grab_contours(cnts)
		(minX, minY) = (np.inf, np.inf)
		(maxX, maxY) = (-np.inf, -np.inf)

		# if no contours were found, return None
		if len(cnts) == 0:
			return None

		control = False
		# otherwise, loop over the contours
		for c in cnts:
    		# if the contour is too small, ignore it
			if cv2.contourArea(c) < conf["min_area"]:
				control = control and True
				continue
			# compute the bounding box of the contour and use it to
			# update the minimum and maximum bounding box regions
			control = True
			(x, y, w, h) = cv2.boundingRect(c)
			(minX, minY) = (min(minX, x), min(minY, y))
			(maxX, maxY) = (max(maxX, x + w), max(maxY, y + h))

		# otherwise, return a tuple of the thresholded image along
		# with bounding box
		if control == False:
			return None
		return (thresh, (minX, minY, maxX, maxY))
