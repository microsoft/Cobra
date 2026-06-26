import websocket
import json
import ssl

ws = websocket.WebSocket(sslopt={"cert_reqs": ssl.CERT_NONE})
ws.connect("wss://cobra-webapi.nuance.mobi/webapi-platform/websocket")

command = {
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

str = json.dumps(command)
print "Sending: " + str
ws.send(str)
print "Sent"
print "Receiving..."
result = ws.recv()
print "Received: " + result
ws.close()
