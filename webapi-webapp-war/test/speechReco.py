
import threading
import time
import json
import array

import audioPackets
import deferred

try:
	import websocket
except ImportError:
	print ("Failed to import 'websocket', install with...")
	print ("  # pip install websocket")
	exit()



startSessionCommand = {
	"type": "command",
	"command": {
		"name": "startSession",
		"id": "1",
		"parameters": {
			"applicationKey": "companyName_appName",
			"verificationCode": "5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c"
		}
	}
}

endSessionCommand = {
	"type": "command",
	"command": {
		"name": "endSession",
	}
}

speechRecoCommand = {
	"type": "command",
	"command": {
		"name": "speechRecognition",
		"parameters": {
			"language": "en-us"
			# "engine": "mrec"
		}
	}
}

stopSpeechRecoDirective = {
	"type": "directive",
	"command": {
		"name": "speechRecognition",
	},
	"directive": {
		"name": "stop"
	}
}

ws = websocket.WebSocket()
ws.connect("wss://cobra-webapi.nuance.mobi/webapi-platform/websocket")

exitEvent = threading.Event()

sessionId = None
commandId = 0
deferredMap = {}

def recv():
	global deferredMap
	
	while True:
		message = ws.recv()
		print ("message: %s" % message)

		js = json.loads(message)
		cid = js['command']['id']
		d = deferredMap["k" + cid]

		if js['type']=="event":
			d.update(js)
		elif js['result']['final']:
			if js['result']['status'] == "success":
				d.resolve(js)
			else:
				d.reject(js)
		else:
			d.update(js)

t = threading.Thread(target=recv)
t.daemon = True
t.start()


def send(js):
	str = json.dumps(js)
	print ("Sending: %s" % str)

	ws.send(str)


def sendCommand(cmd):
	global deferredMap
	global commandId

	commandId += 1
	cid = "%i" % commandId

	d = deferred.Deferred()
	deferredMap["k" + cid] = d

	if sessionId:
		cmd['sessionId'] = sessionId
	cmd['command']['id'] = cid
	
	send(cmd)

	return d

def sendDirective(dir):
	if sessionId:
		dir['sessionId'] = sessionId
	dir['command']['id'] = "%i" % commandId

	send(dir)


class StartSession():
	@staticmethod
	def send():
		d = sendCommand(startSessionCommand)
		d.add(StartSession.res, StartSession.rej)

	@staticmethod
	def res(data):
		print ("Resolving StartSession")
		global sessionId
		sessionId = data['sessionId']
		print ("SessionID: %s" % sessionId)
		SpeechReco.send()

	@staticmethod
	def rej(data):
		print ("Rejecting StartSession")
		global exitEvent
		exitEvent.set()


	
class SpeechReco():
	hexValues = {
		'0':0, '1':1, '2':2, '3':3, '4':4, '5':5, '6':6, '7':7,
		'8':8, '9':9, 'a':10, 'b':11, 'c':12, 'd':13, 'e':14, 'f':15
	}

	@staticmethod
	def computeSpeechPacket(line):
		packet = array.array('B')
		i = 0
		while i < len(line):
			l = SpeechReco.hexValues[line[i]]
			i += 1
			h = SpeechReco.hexValues[line[i]]
			i += 1

			v = 0
			v |= l
			v <<= 4
			v |= h
			
			packet.append(v)
		return packet

	@staticmethod
	def _send():
		d = sendCommand(speechRecoCommand)
		d.add(SpeechReco.res, SpeechReco.rej, SpeechReco.upd)
		for i, packet in enumerate(audioPackets.data):
			time.sleep(0.2)

			audio = SpeechReco.computeSpeechPacket(packet)
			print ("packet %i len %i" % (i, len(audio)))
			#ws.send(audio, opcode=websocket.ABNF.OPCODE_BINARY)
			ws.send_binary(audio)

		time.sleep(.2)
		sendDirective(stopSpeechRecoDirective)

	@staticmethod
	def send():
		t = threading.Thread(target=SpeechReco._send)
		t.daemon = True
		t.start()
	

	@staticmethod
	def res(data):
		print ("Resolving SpeechReco")
		EndSession.send()

	@staticmethod
	def rej(data):
		print ("Rejecting SpeechReco")
		EndSession.send()

	@staticmethod
	def upd(data):
		print ("SpeechReco Update")

class EndSession():
	@staticmethod
	def send():
		d = sendCommand(endSessionCommand)
		d.add(EndSession.res, EndSession.res)
		
	@staticmethod
	def res(data):
		print ("Resolving EndSession")
		global exitEvent
		exitEvent.set()

StartSession.send()

try:
	while not exitEvent.wait(.1):
		pass
except BaseException:
	print ("")

ws.shutdown()

exit()




