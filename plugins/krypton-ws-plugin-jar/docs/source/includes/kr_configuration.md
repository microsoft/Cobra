# Plugin configuration
> See the WebAPI Server Configuration guide to configure your WebAPI server.

In addition to the WebAPI configuration file (config/webapi.json), and your tenant configuration file (e.g. companyName_applicationName.json), your Krypton-Websocket Plugin should have its own configuration file (e.g. **kryptonws**.json) and it should contain the following:

```
{
    "activePluginsList":
    [
        {
            "jarName": "krypton-ws-plugin-.*jar"
        }
    ]
}
```
_where "krypton-ws-plugin-.*jar" is a regex that matches the plugin jar file name (e.g. plugins/krypton-ws-plugin-1.2-SNAPSHOT.jar)_

Your plugin websocket URL should look like:
wss://<HOST>/webapi-platform/websocket/**kryptonws**/kryptonws

_where **kryptonws** (the first occurrence in the URL) matches the name of your plugin configuration file_

<h3 id='parameter'>Parameters</h3>
<table>
	<thead>
		<tr>
			<th>Name</th>
			<th>Type</th>
			<th>Default value</th>
			<th>Description</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>kryptonWsCloseWsSession</td>
			<td><code>Boolean</code></td>
			<td>false</td>
			<td>Indicates if we close the session.</td>
		</tr>
		<tr>
			<td>kryptonWsLogAudio</td>
			<td><code>Boolean</code></td>
			<td>false</td>
			<td>Indicates if we log audio bytes.</td>
		</tr>
		<tr>
			<td>kryptonWsIgnoreStrayAudio</td>
			<td><code>Boolean</code></td>
			<td>true</td>
			<td>Indicates if we ignore stray audio bytes (or return errors).</td>
		</tr>
		<tr>
			<td>kryptonWsAsyncWebapiCalls</td>
			<td><code>Boolean</code></td>
			<td>true</td>
			<td>Indicates if we call WebAPI from another thread.</td>
		</tr>
		<tr>
			<td>kryptonWsDatapackOnlyRecognitionPattern</td>
			<td><code>String</code></td>
			<td>.*/datapack-only/.*</td>
			<td>The pattern to use to check if a Load URI should not actually be loaded.</td>
		</tr>
		<tr>
			<td>kryptonWsEnableMetrics</td>
			<td><code>Boolean</code></td>
			<td>false</td>
			<td>Indicates if we return metrics.</td>
		</tr>
		<tr>
			<td>kryptonWsThwartNoAudioTimerSeconds</td>
			<td><code>Integer</code> Seconds</td>
			<td>10</td>
			<td>Time interval between sending a few silence packets to thwart hosted Krypton's no-audio timers when startRecognitionTimers is false. Used for long playing prompts while recognition is active (bargein) or active but muted (non-bargein prompt). A value less than 1 disables this feature.</td>
		</tr>
		<tr>
			<td>websocketMaxIdleTimeout</td>
			<td><code>Integer</code> Milliseconds</td>
			<td>300000</td>
			<td>The timeout for an idle websocket connection (in milliseconds). Must be set in plugin config (e.g. kryptonws.json) or WebAPI config (e.g. webapi.json). Value set in tenant config will be ignored.</td>
		</tr>
	</tbody>
</table>
