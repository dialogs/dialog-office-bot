Configuration 

The bot requires a configuration file to works, it is expected to be in the same location as the executable file and named "confgi.json", but you can provide the path to the file as single parameter on execution time.

The file content should be a correctly formed json string, using the format below

{
	dialogsConfig: {
		host: "grpc-test.transmit.im",
		port: 9443,
		token: "cf532fa71fe992b53da0ad47566d5aba84a76e94"
	},
	jiraServiceDeskConfig: {
		host: "https://domain.com",
		port: 443,
		context: "",
		serviceDeskId: 2,
		username: "jiraUser",
		password: "jiraPassword"
	}
}

jiraServiceDeskConfig.host requires the protocol to be used ( http | https )
jiraServiceDeskConfig.port should match the protocol provided on the host field
jiraServiceDeskConfig.context if your end point is not in the root of the domain, you can add context to the path 
jiraServiceDeskConfig.authorDomain this field will be used as domain for the author request ( email field )

Running the jar

Java 1.8 is the minimum version to execute the file.
JSDBot include a "fat-jar" ready to run, in build/libs directory, just include the "config.json" in the same directory o provide the path to your config file as argument 

java -jar JSDBot-all.jar C:/path/to/my/config.json

Using the bot

As long as you do not have an active request the bot will take all your text input to find request types and providing you the resulting matches, non text input will be returned as unexpected parameter.

When you create a request, clicking some provided button you will be asked to fill the fields one by one , giving you the field name and restricting the values when required. If your input is incorrectly formated or doesn't match the expected value you will be informed with a response so you will be able to fix your input.

You can cancel the request at any time by clicking the cancel button, and the bot will delete your current request

When you submit a successful request, you will be given the URL to access the request, and the bot will use your entry again to find the types of requests.

Every interaction with the bot will give you feedback in some way, to avoid waiting for a responses which crashed. So if you get a message like "Sorry, something goes wrong ...", it means something unexpected happens and the stack will be printed on the bot output ( console ), other error messages will be prompted to help the user to fill the fields.