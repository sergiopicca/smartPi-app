import RPi.GPIO as GPIO
from time import sleep

class ServoMotor:
	def __init__(self):
		self.pin = 3
		

	def setAngle(self, angle):
		GPIO.setmode(GPIO.BOARD)
		GPIO.setup(self.pin, GPIO.OUT)
		pwm=GPIO.PWM(self.pin, 50)
		#
		pwm.start(0)
		duty = (angle+90) / 18 + 2
		GPIO.output(self.pin, True)
		pwm.ChangeDutyCycle(duty)
		sleep(1)
		GPIO.output(self.pin, False)
		pwm.ChangeDutyCycle(0)
		# Clean up
		pwm.stop()
		GPIO.cleanup()
		
		

	

