import sys
from io import BytesIO
import uuid
import httplib

class Part(object):
    
    """Represent a part in a multipart messsage"""
    def __init__(self, name, contentType, data, paramName=None):
        super(Part, self).__init__()
        self.name = name
        self.paramName = paramName
        self.contentType = contentType
        self.data = data

    def encode(self):
        body = BytesIO()
        
        if self.paramName:
            body.write('Content-Disposition: form-data; name="%s"; paramName="%s"\r\n' % (self.name, self.paramName))
        else:
            body.write('Content-Disposition: form-data; name="%s"\r\n' % (self.name,))
        
        body.write("Content-Type: %s\r\n" % (self.contentType,))
        body.write("\r\n")
        body.write(self.data)

        return body.getvalue()

class Request(object):
    """A handy class for creating a request"""
    def __init__(self):    
        super(Request, self).__init__()
        self.parameters = []

    def add_json_parameter(self, name, paramName, data):
        self.parameters.append(Part(name=name, paramName=paramName, contentType="application/json; charset=utf-8", data=data))

    def add_audio_parameter(self, name, paramName, data):
        self.parameters.append(Part(name=name, paramName=paramName, contentType="audio/x-wav;codec=pcm;bit=16;rate=16000", data=data))

    def encode(self):
        boundary = uuid.uuid4().hex
        body = BytesIO()
        
        for parameter in self.parameters:
            body.write("--%s\r\n" % (boundary,))
            body.write(parameter.encode())
            body.write("\r\n")
        
        body.write("--%s--\r\n" % (boundary,))

        return body.getvalue(), boundary

host = "mtldev01.nuance.com"
port = 8911

uri = "/NmspServlet/"

RequestData = """{
	"appKey": "!!! YOUR APP KEY !!!",
	"appId": "!!! YOUR APP ID !!!",
	"uId": "!!! YOUR APP UID !!!",
	"inCodec": "PCM_16_16K",
	"outCodec": "PCM_16_16K",
	"cmdName": "DRAGON_NLU_ASR_CMD",
	"appName": "PythonSample",
	"appVersion": "1",
	"language": "eng_USA",
	"carrier": "carrier",
	"deviceModel": "deviceModel",
	"cmdDict": {
		"dictation_type": "Dictation",
		"dictation_language": "eng-USA",
		"locale": "canada",
		"application_name": "Testing Python Script",
		"organization_id": "NUANCE",
		"phone_OS": "4.0",
		"phone_network": "wifi",
		"audio_source": "SpeakerAndMicrophone",
		"location": "<+45.5086699, -73.5539925> +/- 99.00m",
		"application_session_id": "1234567890",
		"utterance_number": "5",
		"ui_langugage": "en",
		"phone_submodel": "nmPhone2,1",
		"application_state_id": "45"        
	}
}"""

REQUEST_INFO = """{
	"start": 0,
	"end": 0,
	"text": "",
	"enable_intermediate_response": 1,
    "appserver_data":
    {
    }
}"""

request = Request()
request.add_json_parameter("RequestData", None, RequestData)
request.add_json_parameter("DictParameter", "REQUEST_INFO", REQUEST_INFO)
inputFile = open((sys.argv[1]), "rb")
while True:
	audioChunk = inputFile.read(100000)
	if audioChunk != '':
		request.add_audio_parameter("ConcludingAudioParameter", "AUDIO_INFO", audioChunk)
	else:
		break
		
body, boundary = request.encode()

# If using HTTPs use: h = httplib.HTTPSConnection(host, port)
h = httplib.HTTPConnection(host, port)
h.set_debuglevel(0)
headers = {
	"Content-Type": "multipart/form-data; boundary=%s" % (boundary,),
	"Connection": "Keep-Alive",	
}
h.request('POST', uri, body, headers)
res = h.getresponse()
print res.read() 
