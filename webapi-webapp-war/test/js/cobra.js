
var webSocketUrl = "wss://cobra-webapi.nuance.mobi/webapi-platform/websocket";

var applicationKey = "companyName_appName";
var verificationCode = "5796d9e101d5355f5dbf95a3681f6ca5317fd9e96e5ffacac5e1305361a4eb1c";

var synthesisText = "i will gladly pay you tuesday for a hamburger today";
var ttsVoiceName = "Carol";
var ttsLocale = "en-us";



var replacedAtRuntime = "## Replaced at runtime ##";

var originalCommands = {
startSession: {
	type: "command",
	command: {
		name: "startSession",
		id: replacedAtRuntime,
		parameters: {
			applicationKey: applicationKey,
			verificationCode: verificationCode
		}
	}
},
endSession: {
	type: "command",
	sessionId: replacedAtRuntime,
	command: {
		name: "endSession",
		id: replacedAtRuntime
	}
},

speechSynthesis: {
	type: "command",
	sessionId: replacedAtRuntime,
	command: {
		name: "speechSynthesis",
		id: replacedAtRuntime,
		parameters: {
			voice: ttsVoiceName,
			language: ttsLocale,
			data: synthesisText,
			type: "text"
		}
	}
},

speechRecognition: {
	type: "command",
	sessionId: replacedAtRuntime,
	command: {
		name: "speechRecognition",
		id: replacedAtRuntime,
		parameters: {
			engine: "x",
			language: "en-us"
		}
	}
}
}

var activeCommands = {};

function initializeCommand(key) {
	var s = localStorage.getItem(key);
	if (s) {
		try {
			s = JSON.parse(s);
		}
		catch (err) {
			s = null;
		}
	}
	if (!s) {
		s = originalCommands[key];
	}

	activeCommands[key] = s;
}
['startSession','endSession','speechSynthesis','speechRecognition'].forEach(initializeCommand);


function addCss() {
	var head = document.getElementsByTagName('head')[0];
	var style = document.createElement("style");
	head.appendChild(style);

	var styleSheet = style.sheet;
	var ri = 0;

	styleSheet.insertRule(`
* {
	margin: 0;
	padding: 0;
	box-sizing: border-box;
	cursor: default;
}`, ri++);

	styleSheet.insertRule(`
body {
	position: absolute;
	overflow-x: hidden;
	overflow-y: scroll;
	font-family: arial;
}`, ri++);

	styleSheet.insertRule(`
div.controls {
	position: fixed;
	width: 100%;
	z-index: 1;
	background: rgb(224,224,224);
	border-bottom: 1px solid grey;
}`, ri++);

	styleSheet.insertRule(`
div.hist {
	padding: 10px;
}`, ri++);

	styleSheet.insertRule(`
button, span#recoResult {
	padding: 3px;
	border: 1px solid grey;
	background-color: rgb(192,192,192);
}`, ri++);

	styleSheet.insertRule(`
button:hover:enabled {
	background-color: rgb(160,160,160);
}`, ri++);

	styleSheet.insertRule(`
input {
	padding: 4px;
	border: 1px solid grey;
	background-color: rgb(192,192,192);
	cursor: auto;
}`, ri++);

	styleSheet.insertRule(`
input.large {
	width: 420px;
}`, ri++);

	styleSheet.insertRule(`
canvas {
	width: 200px;
	height: 50px;
	border: 1px solid grey;
	background-color: rgb(224,224,224);
}`, ri++);

	styleSheet.insertRule(`
.right {
	float: right;
}`, ri++);

	styleSheet.insertRule(`
div.card {
	display: flex;
	align-items: flex-start;
	width: 100%;
	padding: 3px 5px;
}`, ri++);

	styleSheet.insertRule(`
div.card:nth-child(2n) {
	background-color: rgb(216,216,216);
}`, ri++);

	styleSheet.insertRule(`
div.card div {
	flex-shrink: 0;
}`, ri++);

	styleSheet.insertRule(`
div#recoResult {
	flex-shrink: 1;
}`, ri++);

	styleSheet.insertRule(`
div.card br {
	margin-bottom: 5px;
}`, ri++);

	styleSheet.insertRule(`
div.card span, div.card button, div.card canvas, div.card input {
	margin-right: 5px;
	vertical-align: middle;
}`, ri++);

	styleSheet.insertRule(`
span#recoResult {
	display: inline-block;
	background-color: unset;
	max-height: 50px;
	overflow-y: auto;
	margin: unset;
}`, ri++);

	styleSheet.insertRule(`
div.hist span.item {
	display: block;
	font-family: monospace;
	padding-left: 20px;
	text-indent: -20px;
}`, ri++);

	styleSheet.insertRule(`
div.hist span#autoscroll {
	position: fixed;
	bottom: 0;
	right: 0;
	cursor: default;
}`, ri++);

	styleSheet.insertRule(`
.tooltip {
	position: relative;
	display: inline-block;
}`, ri++);

	styleSheet.insertRule(`
.tooltip .tooltiptext {
	background-color: lightblue;
	padding: 3px 5px;
	margin: unset;
	position: absolute;
	right: 100%;
	z-index: 1;
	font-size: 12px;
	opacity: 0;
	transition: opacity 250ms;
	pointer-events: none;
}`, ri++);

	styleSheet.insertRule(`
.tooltip:hover .tooltiptext {
	opacity: 1;
	transition: opacity 350ms 1s;
}`, ri++);

	styleSheet.insertRule(`
div.modal {
	background-color: rgba(0,0,0,.4);
	bottom: 0;
	left: 0;
	position: fixed;
	right: 0;
	top: 0;
	z-index: 1;
	transition-property: opacity;
	transition-duration: 300ms;
}`, ri++);

	styleSheet.insertRule(`
.panel {
	position: fixed;
	right: 5px;
	top: 5px;
	z-index: 1;
}`, ri++);

	styleSheet.insertRule(`
div.panel {
	background-color: rgb(192,192,192);
	border: 1px solid black;
	box-shadow: -10px 10px 10px #444;
}`, ri++);

	styleSheet.insertRule(`
.visible {
	opacity: 1;
}`, ri++);

	styleSheet.insertRule(`
.hidden {
	opacity: 0;
	pointer-events: none;
}`, ri++);

	styleSheet.insertRule(`
.titlebar {
	background-color: rgb(160,160,160);
	display: inline-block;
	font-weight: bold;
	padding: 3px;
	text-align: center;
	width: 100%;
}`, ri++);

	styleSheet.insertRule(`
.titlebar span {
	vertical-align: middle;
}`, ri++);

	styleSheet.insertRule(`
.titlebar button {
	color: black;
	font-weight: bold;
	height: 24px;
	width: 24px;
}`, ri++);

	styleSheet.insertRule(`
.tabcontainer {
	padding: 5px 5px 0;
}`, ri++);

	styleSheet.insertRule(`
.tabs {
	border-bottom: 1px solid grey;
}`, ri++);

	styleSheet.insertRule(`
.tabs > button {
	border: 1px solid grey;
	border-bottom: 0;
	vertical-align: bottom;
	padding: 3px 6px;
}`, ri++);

	styleSheet.insertRule(`
.error {
	color: rgb(160,0,0);
}`, ri++);

	styleSheet.insertRule(`
.changed {
	color: rgb(0,0,160);
}`, ri++);

	styleSheet.insertRule(`
.tabs > button.selected {
	background-color: rgb(224,224,224);
	font-weight: bold;
	padding: 4px 6px;
	pointer-events: none;
	position: relative;
	top: 1px;
}`, ri++);

	styleSheet.insertRule(`
.tabcontainer > .contents {
	height: 300px;
	position: relative;
	width: 800px;
}`, ri++);

	styleSheet.insertRule(`
div.contents > div {
	background-color: rgb(224,224,224);
	border: 1px solid grey;
	border-top: 0;
	height: 100%;
	padding: 7px;
	position: absolute;
	width: 100%;
}`, ri++);

	styleSheet.insertRule(`
textarea {
	background-color: rgb(224,224,224);
	border: 1px solid grey;
	height: 100%;
	overflow: auto;
	padding: 3px;
	resize: none;
	white-space: pre;
	width: 100%;
}`, ri++);

	styleSheet.insertRule(`
.buttonrow {
	padding: 3px 0 5px;
	text-align: center;
}`, ri++);

	styleSheet.insertRule(`
.buttonrow button {
	margin: 0 3px;
}`, ri++);

}


function elem(parent, name, label) {
	var elem = document.createElement(name);
	parent.appendChild(elem);
	if (undefined !== label) {
		elem.innerHTML = label;
	}
	return elem;
}

function div(parent, cls) {
	var div = elem(parent, "div");
	if (undefined !== cls) {
		div.setAttribute('class', cls);
	}
	return div;
}

function span(parent, text, cls) {
	var span = elem(parent, "span", text);
	if (undefined !== cls) {
		span.setAttribute('class', cls);
	}
	return span;
}

function button(parent, label, cls) {
	var button = elem(parent, "button", label);
	if (undefined !== cls) {
		button.setAttribute('class', cls);
	}
	return button;
}

function input(parent, cls, storageKey, defaultValue) {
	var value = localStorage.getItem(storageKey);
	if (! value) {
		value = defaultValue;
	}
	var input = elem(parent, "input");
	input.setAttribute('class', cls);
	input.value = value;
	input.ondblclick = function() {
		localStorage.removeItem(storageKey);
		input.value = defaultValue;
	}
	return input;
}

addCss();

var body = document.getElementsByTagName("body")[0];

var controlsDiv = div(body, "controls");
var histDiv = div(body, "hist");

var commandsButton = button(body, "Commands");
commandsButton.setAttribute('class', 'panel');

var modal = div(body, "modal hidden");
var commandsPanel = div(modal, "panel");

commandsButton.onclick = function() {
	modal.classList.remove('hidden');
	modal.classList.add('visible');
}

var _div = div(commandsPanel, 'titlebar');
span(_div, "Command Templates");
var b = button(_div, "X", 'right');
b.onclick = function() {
	modal.classList.remove('visible');
	modal.classList.add('hidden');
}

var tabGroup = new TabGroup(commandsPanel);

var _div = div(commandsPanel, 'buttonrow');
var resetButton = button(_div, "Reset");
resetButton.onclick = function() {
	var item = tabGroup.getSelected();
	localStorage.removeItem(item.key);
	activeCommands[item.key] = originalCommands[item.key];
	item.update();
	validateContents(item);
}
var discardButton = button(_div, "Discard");
discardButton.onclick = function() {
	var item = tabGroup.getSelected();
	item.update();
	validateContents(item);
}
var commitButton = button(_div, "Commit");
commitButton.onclick = function() {
	var item = tabGroup.getSelected();
	var value = item.textarea.value;
	localStorage.setItem(item.key, value);
	var o = JSON.parse(value);
	activeCommands[item.key] = o;
	
	item.update();
	validateContents(item);
}


tabGroup.addItem("Start Session", 'startSession');
tabGroup.addItem("Speech Synthesis", 'speechSynthesis');
tabGroup.addItem("Speech Recognition", 'speechRecognition');

function textareaKeyDown(e) {
	if (e.key == 'Tab') {
		e.preventDefault();
		var s = this.selectionStart;
		this.value = this.value.substring(0,this.selectionStart) + "    " + this.value.substring(this.selectionEnd);
		this.selectionEnd = s + 4;
	}
}

function textareaInput(e) {
	var item = this.myItemRef;
	validateContents(item);
}

function TabGroup(parent) {
	var container = div(parent, 'tabcontainer');
	var buttons = div(container, 'tabs');
	var contentsDiv = div(container, 'contents');
	var tabGroup = this;
	var items = [];
	var selected;

	this.getSelected = function() { return selected; }

	this.addItem = function(title, key) {
		var itemContentsDiv = div(contentsDiv);
		var item = {
			key: key,
			button: button(buttons, title),
			contentsDiv: itemContentsDiv,
			textarea: elem(itemContentsDiv, "textarea"),
			index: items.length,
			error: false
		}

		item.update = function() {
			item.textarea.value = JSON.stringify(activeCommands[item.key], null, 4);
		}
		item.select = function() {
			selected = item;
			item.button.classList.add('selected');
			item.contentsDiv.hidden = false;
			validateContents(item);
		}
		item.deselect = function() {
			item.button.classList.remove('selected');
			item.contentsDiv.hidden = true;
		}
		item.button.onclick = function() {
			selected.deselect();
			item.select();
		}
		

		item.contentsDiv.hidden = true;

		item.textarea.spellcheck = false;
		item.textarea.onkeydown = textareaKeyDown;
		item.textarea.oninput = textareaInput;
		item.textarea.myItemRef = item;

		item.update();

		items.push(item);

		if (!selected) {
			item.select();
		}
	}

	this.error = function() {
		for (var i = 0; i < items.length; i++) {
			if (items[i].error) {
				return true;
			}
		}
		return false;
	}

}

function validateContents(item) {
	var value = item.textarea.value;

	item.button.classList.remove('changed');
	discardButton.disabled = false;
	commitButton.disabled = true;

	var noc = JSON.stringify(originalCommands[item.key]);
	var nac = JSON.stringify(activeCommands[item.key]);
	resetButton.disabled = noc == nac;

	try {
		var js = JSON.parse(value);

		item.error = false;
		item.button.classList.remove('error');
		item.textarea.classList.remove('error');

		var njs = JSON.stringify(js);
		if (njs != nac) {
			item.button.classList.add('changed');
			commitButton.disabled = false;
		}
		else {
			discardButton.disabled = true;
		}

		return true;
	}
	catch (err) {
		item.error = true;
		item.button.classList.add('error');
		item.textarea.classList.add('error');

		return false;
	}
}


var socketDiv = div(controlsDiv, "card");
var _div = div(socketDiv);
span(_div, "WebSocket");
var openSocketButton = button(_div, "Open");
var closeSocketButton = button(_div, "Close");
var _div = div(socketDiv, 'tooltip');
var webSocketUrlInput = input(_div, 'large', 'webSocketUrl', webSocketUrl);
span(_div, "WebSocket&nbsp;URL", 'tooltiptext');
var _div = div(socketDiv);
var socketStatusSpan = span(_div);


var sessionDiv = div(controlsDiv, "card");
var _div = div(sessionDiv);
span(_div, "Session");
var startSessionButton = button(_div, "Start");
var endSessionButton = button(_div, "End");
/*
var _div = div(sessionDiv);
var d2 = div(_div, 'tooltip');
var applicationKeyInput = input(d2, 'large', 'applicationKey', applicationKey);
span(d2, "applicationKey", 'tooltiptext');
elem(_div, "br");
var d2 = div(_div, 'tooltip');
var verificationCodeInput = input(d2, 'large', 'verificationCode', verificationCode);
span(d2, "verificationCode", 'tooltiptext');
*/
var _div = div(sessionDiv);
var sessionStatusSpan = span(_div);


var synthDiv = div(controlsDiv, "card");
var _div = div(synthDiv);
span(_div, "Speech Synthesis");
var startSynthButton = button(_div, "Start");
var stopSynthButton = button(_div, "Stop");
elem(_div, "br");
var synthStatusSpan = span(_div);
var _div = div(synthDiv);
var synthCanvas = elem(_div, "canvas");
/*
var _div = div(synthDiv);
var d2 = div(_div, 'tooltip');
var synthesisTextInput = input(d2, 'large', 'synthesisText', synthesisText);
span(d2, "Synthesis&nbsp;Text", 'tooltiptext');
elem(_div, "br");
var d2 = div(_div);
var d3 = div(d2, 'tooltip');
var ttsVoiceNameInput = input(d3, '', 'ttsVoiceName', ttsVoiceName);
span(d3, "TTS&nbsp;Voice&nbsp;Name", 'tooltiptext');
var d3 = div(d2, 'tooltip');
var ttsLocaleInput = input(d3, '', 'ttsLocale', ttsLocale);
span(d3, "TTS&nbsp;Locale", 'tooltiptext');
*/


var recoDiv = div(controlsDiv, "card");
var _div = div(recoDiv);
span(_div, "Speech Recognition");
var startRecoButton = button(_div, "Start");
var stopRecoButton = button(_div, "Stop");
var cancelRecoButton = button(_div, "Cancel");
elem(_div, "br");
var recoStatusSpan = span(_div);
var _div = div(recoDiv);
var recoCanvas = elem(_div, "canvas");
var _div = div(recoDiv, 'tooltip');
_div.setAttribute('id', 'recoResult');
var recoResultSpan = span(_div);
recoResultSpan.setAttribute('id', 'recoResult');
span(_div, "Recognition&nbsp;Result", 'tooltiptext');

histDiv.style.marginTop = controlsDiv.clientHeight + "px";


function setRecoResultText(text) {
	recoResultSpan.innerHTML = text;
	recoResultSpan.scrollTo(0, recoResultSpan.scrollTopMax);
}

if (0) {
	setRecoResultText("<b>" + synthesisText + " " + synthesisText + " " + synthesisText + " " + synthesisText + "</b>");
}

if ('scrollRestoration' in history) {
	history.scrollRestoration = 'manual';
}
else {
	window.onbeforeunload = function () {
		window.scrollTo(0, 0);
	}
}

var autoscroll = true;
function getAutoscrollText() {
	return autoscroll ? "\u25BC" : "\u25BD";
}

function updateSpan() {
	autoscrollSpan.innerHTML = getAutoscrollText();
}

function scroll() {
	if (autoscroll) {
		window.scrollTo(0, window.scrollMaxY);
	}
}

var autoscrollSpan = elem(histDiv, "span", getAutoscrollText());
autoscrollSpan.setAttribute("id", "autoscroll");

autoscrollSpan.onclick = function() {
	autoscroll = ! autoscroll;
	updateSpan();
	scroll();
}

window.onscroll = function(evt) {
	var oldautoscroll = autoscroll;
	autoscroll = window.scrollY >= window.scrollMaxY;
	if (oldautoscroll != autoscroll) {
		updateSpan();
	}
}


function formatDate(date) {
	var pad = function(i, n) {
		if (undefined === n) {
			n = 2;
		}
		i = "00" + i;
		return i.substr(i.length - n);
	}

	var r = date.getFullYear() + "-";
	r += pad(1 + date.getMonth()) + "-";
	r += pad(date.getDate()) + " ";

	var h = date.getHours();
	var ap = h > 11 ? "pm" : "am";
	r += pad(h % 12) + ":";
	r += pad(date.getMinutes()) + ":";
	r += pad(date.getSeconds()) + ".";
	r += pad(date.getMilliseconds(),3) + ap;

	return r;
}

function log(data) {
	console.log(data);
	var span = elem(histDiv, "span", "<b>" + formatDate(new Date()) + "</b> " + data);
	span.setAttribute('class', 'item');
	scroll();
}



window.AudioContext = window.AudioContext || window.webkitAudioContext;
if (!window.AudioContext) {
	throw "No WebAudio Support in this Browser";
}

navigator.getUserMedia = navigator.getUserMedia
	|| navigator.webkitGetUserMedia
	|| navigator.mozGetUserMedia
	|| navigator.msGetUserMedia;

if (!navigator.getUserMedia) {
	throw "No getUserMedia Support in this Browser";
}


var hexChars = ['0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'];
function int8toHex(i) {
	var yx = (i >> 4) & 0xf;
	var xy = i & 0xf;
	return hexChars[yx] + hexChars[xy];
}
function int16toHex(i) {
	var yxxx = (i >> 12) & 0xf;
	var xyxx = (i >> 8) & 0xf;
	var xxyx = (i >> 4) & 0xf;
	var xxxy = i & 0xf;
	return hexChars[xxyx] + hexChars[xxxy] + hexChars[yxxx] + hexChars[xyxx];	
}

function WaveformVisualizer(canvasElement, analyserNode) {
	var context = canvasElement.getContext("2d");
	var width = canvasElement.width;
	var height = canvasElement.height;
	var analyser = analyserNode;

	var bufferLength = analyser.frequencyBinCount;
	var dataArray = new Uint8Array(bufferLength);

	var factor = 1.0 / 128.0;
	var halfHeight = height * 0.5;
	var ooBufferLength = 1.0 / bufferLength;

	var stopped = false;

	var draw = this.draw = function() {
		if (stopped) {
			context.clearRect(0, 0, width, height);
			return;
		}

		context.fillStyle = '#c8c8c8'; // "rgb(200, 200, 200)";
		context.fillRect(0, 0, width, height);

		analyser.getByteTimeDomainData(dataArray);

		context.lineWidth = 2;
		context.strokeStyle = '#000000'; // "rgb(0, 0, 0)";

		context.beginPath();
		
		var sliceWidth = width * ooBufferLength;
		var x = 0;

		for (var i = 0; i < bufferLength; i++) {
			var v = dataArray[i] * factor;
			var y = v * halfHeight;

			if (i === 0) {
				context.moveTo(x, y);
			} else {
				context.lineTo(x, y);
			}
			x += sliceWidth;
		}

		context.lineTo(width, halfHeight);
		context.stroke();

		requestAnimationFrame(draw);
	}

	this.stop = function() {
		stopped = true;
	}
}

function BarGraphVisualizer(canvasElement, analyserNode) {
	var context = canvasElement.getContext("2d");
	var width = canvasElement.width;
	var height = canvasElement.height;
	var analyser = analyserNode;

	analyser.fftSize = 64;
	var bufferLength = analyser.frequencyBinCount;
	var dataArray = new Uint8Array(bufferLength);

	var oo256 = 1.0 / 256.0;
	var heightFactor =  height * oo256;
	var colorFactor = 155.0 * oo256;
	var barWidth = -1 + width / bufferLength;

	var stopped = false;

	var draw = this.draw = function() {
		if (stopped) {
			context.clearRect(0, 0, width, height);
			return;
		}

		context.fillStyle = '#c8c8c8'; // rgb(200, 200, 200)
		context.fillRect(0, 0, width, height);

		analyser.getByteFrequencyData(dataArray);

		var x = 0;

		for (var i = 0; i < bufferLength; i++) {
			var barHeight = dataArray[i] * heightFactor;
			var color = Math.floor(100 + dataArray[i] * colorFactor);

			context.fillStyle = '#' + int8toHex(color) + '3232'; // 'rgb(' + color + ', 50, 50)';
			context.fillRect(x, height - barHeight, barWidth, barHeight);

			x += barWidth + 1;
		}

		requestAnimationFrame(draw);
	}

	this.stop = function() {
		stopped = true;
	}
}

function AudioPlayer() {
	var audioContext = new window.AudioContext();
	
	var canvasContext = synthCanvas.getContext("2d");
	var canvasWidth = synthCanvas.width;
	var canvasHeight = synthCanvas.height;

	var audioSource = [];
	var startTime;

	var analyserNode = audioContext.createAnalyser();
	var visualizer = new WaveformVisualizer(synthCanvas, analyserNode);

	this.start = function() {
		analyserNode.connect(audioContext.destination);
		requestAnimationFrame(visualizer.draw);
	}

	var oo8000 = 1.0 / 8000.0;
	var oo32768 = 1.0 / 32768.0;

	var _this = this;

	this.onaudio = function(audio) {
		if (undefined === startTime) {
			startTime = audioContext.currentTime;
		}

		var audioToPlay = new Int16Array(audio);

		var audioBuffer = audioContext.createBuffer(1, audioToPlay.length, 8000);
		var channelData = audioBuffer.getChannelData(0);
		for (var i = 0; i < channelData.length; i++) {
			channelData[i] = audioToPlay[i] * oo32768;
		}

		var source = audioContext.createBufferSource();
		source.buffer = audioBuffer;
		source.connect(analyserNode);
		source.start(startTime);

		source.onended = function() {
			source.disconnect(analyserNode);
			audioSource.shift();
			if (0 == audioSource.length) {
				// This will happen prematurely if there is a buffer-underrun
				visualizer.stop();
				_this.onfinished();
			}
		}

		audioSource.push(source);

		startTime += audioToPlay.length * oo8000;

	}

	this.stop = function() {
		visualizer.stop();

		analyserNode.disconnect(audioContext.destination);
		for (var i = 0; i < audioSource.length; i++) {
			audioSource[i].stop();
		}
	}

	this.onfinished = function() {
	}

}

function AudioRecorder() {

	var desiredSampleRate = 8000;
	var mediaStream;

	var audioInput;
	var analyserNode;
	var recordingNode;

	var audioContext = new window.AudioContext();
	var resampler = new Resampler(audioContext.sampleRate, desiredSampleRate, 1, 8192);

	var visualizer;

	this.start = function () {
		log("audioContext.sampleRate = " + audioContext.sampleRate);

		navigator.getUserMedia(
			{ audio: true },

			// success
			function (stream) {
				mediaStream = stream;

				audioInput = audioContext.createMediaStreamSource(stream);
				analyserNode = audioContext.createAnalyser();
				recordingNode = audioContext.createScriptProcessor(8192, 1, 1);

				visualizer = new BarGraphVisualizer(recoCanvas, analyserNode);
				requestAnimationFrame(visualizer.draw);

				recordingNode.onaudioprocess = function(evt) {
					var data = evt.inputBuffer.getChannelData(0);
					var ch = resampler.resampler(data);
					//log("audio process -- data len: " + data.length + ", ch len: " + ch.length);

					var encoded = new Int16Array(ch.length);
					for (var i = 0; i < ch.length; i++) {
						var s = Math.max(-1, Math.min(1, ch[i]));
						encoded[i] = s <= -1.0 ? 0x8000 : ( s >= 1.0 ? 0x7FFF : s * 0x8000 );
					}
					
					if (0) {
						var str = "";
						for (var i = 0; i < encoded.length; i++) {
							str += int16toHex(encoded[i]);
						}
						console.log(str);
					}

					webSocket.send(encoded.buffer);
				}

				audioInput.connect(analyserNode);
				analyserNode.connect(recordingNode);
				recordingNode.connect(audioContext.destination);
			},

			// error
			function (error) {
				log("getUserMedia error: " + error);
			}
		);
	}

	this.stop = function() {
		visualizer.stop();

		var tracks = mediaStream.getTracks();
		for (var i = 0; i < tracks.length; i++) {
			log("stopping track: " + i);
			tracks[i].stop();
		}

		audioInput.disconnect(analyserNode);
		analyserNode.disconnect(recordingNode);
		recordingNode.disconnect(audioContext.destination);
	}

}


function StopWatch() {
	var getTime = function() {
		return new Date().getTime();
	}

	var start = this.start = getTime()

	var elapsedMillis = this.elapsedMillis = function() {
		return getTime() - start;
	}

	this.elapsed = function() {
		return Math.floor(elapsedMillis() / 1000);
	}

}

function formatMillis(ms) {
	if (ms > 999) {
		var s = ms / 1000;
		return s + "s";
	}

	return ms + "ms";
}

function formatSeconds(s) {
	if (s > 60) {
		var m = Math.floor(s / 60);
		return m + "m " + formatSeconds(s % 60);
	}

	return s + "s";
}

function isOfType(type, obj) {
	if (obj === undefined || obj === null)
		return false;

	return type === Object.prototype.toString.call(obj).slice(8, -1);
}

var webSocket;
var commandId = 0;
var sessionId;
var synthPlayer;
var recorder;

function stopRecorder(message) {
	if (undefined !== recorder) {
		recorder.stop();
		recorder = undefined;

		recoStatusSpan.innerHTML += message;
	}
}

openSocketButton.onclick = function() {
	localStorage.setItem('webSocketUrl', webSocketUrlInput.value);

	var timer;

	socketStatusSpan.innerHTML = "Opening WebSocket...";

	var stopWatch = new StopWatch();

	var url = webSocketUrlInput.value;
	webSocket  = new WebSocket(url);
	webSocket.binaryType = "arraybuffer"; // for receiving audio

	webSocket.onopen = function() {
		log("WebSocket connection opened in " + formatMillis(stopWatch.elapsedMillis()) + " to " + url);
		socketStatusSpan.innerHTML = "Open";

		stopWatch = new StopWatch();

		timer = setInterval(function() {
			socketStatusSpan.innerHTML = "Open " + formatSeconds(stopWatch.elapsed());
		}, 1000);
		webSocketStartTime = new Date().getTime();
		
	}
	webSocket.onclose = function() {
		log("WebSocket connection closed, lived: " + formatSeconds(stopWatch.elapsed()));
		clearInterval(timer);
		socketStatusSpan.innerHTML = "Closed";
	}
	webSocket.onmessage = function(event) {
		if (isOfType("ArrayBuffer", event.data)) {
			log("WebSocket Received binary");
			if (synthPlayer) {
				synthPlayer.onaudio(event.data);
			}
		}
		else {
			log("WebSocket Received: " + event.data);

			var data = JSON.parse(event.data);

			if (data.type == "result") {
				if (data.command.name == "startSession" && data.result.status == "SUCCESS") {
					sessionId = data.sessionId;
					sessionStatusSpan.innerHTML = "SessionID: " + sessionId;
				}
				if (data.command.name == "endSession" && data.result.status == "SUCCESS") {
					sessionStatusSpan.innerHTML = "Session closed";
				}
			}

			if (data.command.name == "speechRecognition") {
				if (data.type == "event") {
					if (data.event.name == "startOfSpeech") {
						recoStatusSpan.innerHTML += "SoS...";
					}
					else if (data.event.name == "endOfSpeech") {
						recoStatusSpan.innerHTML += "EoS...";
					}
				}
				if (data.type == "result" && data.result.status == "SUCCESS") {
					var pre = "";
					var suf = "";
					if (data.result.final) {
						pre = "<b>";
						suf = "</b>";

						stopRecorder("Stopped");
					}
					
					setRecoResultText(pre + data.result.data.transcriptions[0] + suf);
				}
			}
		}
	}
}

function sendMessage(messageObject) {
	webSocket.send(JSON.stringify(messageObject));
}

closeSocketButton.onclick = function() {
	socketStatusSpan.innerHTML = "Closing WebSocket...";
	webSocket.close();
	webSocket = undefined;
}

function prepareMessage(message, commandId, sessionId) {
	// deep copy
	var s = JSON.stringify(message);
	var msg = JSON.parse(s);

	// undefined sessionId deletes the field
	msg.sessionId = sessionId;
	msg.command.id = "" + commandId;

	return msg;
}

startSessionButton.onclick = function() {
	var cmd = prepareMessage(activeCommands.startSession, commandId++);
	sendMessage(cmd);
}

endSessionButton.onclick = function() {
	var cmd = prepareMessage(activeCommands.endSession, commandId++, sessionId);
	sendMessage(cmd);
}



var synthCommandId;

startSynthButton.onclick = function() {
	synthCommandId = commandId++;

	var cmd = prepareMessage(activeCommands.speechSynthesis, synthCommandId, sessionId);
	sendMessage(cmd);

	synthStatusSpan.innerHTML = "Playing...";

	synthPlayer = new AudioPlayer();
	synthPlayer.onfinished = function() {
		synthStatusSpan.innerHTML += "Finished";
	}
	synthPlayer.start();
}

stopSynthButton.onclick = function() {
	var dir = {
		type: "directive",
		command: {
			name: "speechSynthesis"
		},
		directive: {
			name: "stop"
		}
	}
	var dir = prepareMessage(dir, synthCommandId, sessionId);
	sendMessage(dir);

	synthStatusSpan.innerHTML += "Stopped...";
	synthPlayer.stop();
	synthPlayer = undefined;
}


var recoCommandId;

startRecoButton.onclick = function() {
	log("start pressed");

	recoCommandId = commandId++;

	var cmd = prepareMessage(activeCommands.speechRecognition, recoCommandId, sessionId);
	sendMessage(cmd);
	
	recoStatusSpan.innerHTML = "Recording...";

	recorder = new AudioRecorder();
	recorder.start();
}

stopRecoButton.onclick = function() {
	log("stop pressed");

	stopRecorder("Stopped");

	var dir = {
		type: "directive",
		command: {
			name: "speechRecognition"
		},
		directive: {
			name: "stop"
		}
	}
	var dir = prepareMessage(dir, recoCommandId, sessionId);
	sendMessage(dir);
}

cancelRecoButton.onclick = function() {
	log("cancel pressed");
	
	stopRecorder("Cancelled");

	var dir = {
		type: "directive",
		command: {
			name: "speechRecognition"
		},
		directive: {
			name: "cancel"
		}
	}
	var dir = prepareMessage(dir, recoCommandId, sessionId);
	sendMessage(dir);
}



