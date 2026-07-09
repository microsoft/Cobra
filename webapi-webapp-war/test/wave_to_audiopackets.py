import wave
import time as ti

# This script converts a wav file (FILE_TO_CONVERT) to a series of comma delimited HEX strings audio packets of size BUFFER_SIZE to STDOUT
# Append the output to a <file> to percist it

BUFFER_SIZE = 1024
FILE_TO_CONVERT = './eng_USA_audio.wav'

wf = wave.open(r'%s' %FILE_TO_CONVERT,'rb')   

data = wf.readframes(BUFFER_SIZE)
print(len(data))
packet_count = 1
print('data = [')
while data!=b'':
	data = wf.readframes(BUFFER_SIZE)
	hexadecimal_string_from_bytes = data.hex()
	if packet_count > 1:
		print(', ')
	print('\"' + hexadecimal_string_from_bytes + '\"')
	ti.sleep(0.04)
	packet_count = packet_count + 1
print(']')
