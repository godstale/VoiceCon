#include <user_config.h>
#include <SmingCore/SmingCore.h>
#include <Libraries/Adafruit_SSD1306/Adafruit_SSD1306.h>


// If you want, you can define WiFi settings globally in Eclipse Environment Variables
#ifndef WIFI_SSID
	#define WIFI_SSID "your_SSID" // Put you SSID and Password here
	#define WIFI_PWD "your_password"
#endif

// LED
#define LED_PIN 12 // GPIO12 (D6)

// OLED Display
//* For I2C mode:
// Default I2C pins 0 and 2. Pin 4 - optional reset
// I2C mapping : SCL - D3(GPIO0), SDA - D4(GPIO2)
// Default I2C address is 0x3c!! Check Adafruit_SSD1306.h
Adafruit_SSD1306 display(4);

// Forward declarations
void startMqttClient();
void onMessageReceived(String topic, String message);
void displayTemp(String s_temp);
void displayText(String s_msg);

Timer procTimer;

// MQTT client
// For quickly check you can use: http://www.hivemq.com/demos/websocket-client/ (Connection= test.mosquitto.org:8080)
MqttClient mqtt("your_server_url", 1883, onMessageReceived);


// MQTT : Publish our message
void publishMessage()
{
	// Do what you want
}

// MQTT : Callback for messages, arrived from MQTT server
void onMessageReceived(String topic, String message)
{
	if(message.indexOf("LED") > -1
			|| message.indexOf("led") > -1 ) {
		if(message.indexOf("on") > -1) {
			displayText("Voice command received!! - LED on");
			digitalWrite(LED_PIN, HIGH);
		} else if(message.indexOf("off") > -1) {
			displayText("Voice command received!! - LED off");
			digitalWrite(LED_PIN, LOW);
		} else {
			displayText("Message received but cannot find LED command.");
		}
	}
}

// Run MQTT client
void startMqttClient()
{
	if(!mqtt.setWill("last/will","The connection from this device is lost:(", 1, true)) {
		debugf("Unable to set the last will and testament. Most probably there is not enough memory on the device.");
	}
	mqtt.connect("ESP8266_LED");
	mqtt.subscribe("messagebox");
	displayTemp("Start MQTT client...");
}

// Will be called when WiFi station was connected to AP
void connectOk()
{
	Serial.println("I'm CONNECTED");
	displayTemp("I'm CONNECTED to AP");

	// Run MQTT client
	startMqttClient();

	// Start publishing loop
	procTimer.initializeMs(60*1000, publishMessage).start();
}

// Will be called when WiFi station timeout was reached
void connectFail()
{
	Serial.println("I'm NOT CONNECTED. Need help :(");
	displayTemp("I'm NOT CONNECTED. Need help !!");
	// .. some you code for device configuration ..
}

void init()
{
	pinMode(LED_PIN, OUTPUT);	// Set LED pin as OUTPUT

	Serial.begin(SERIAL_BAUD_RATE); // 115200 by default
	Serial.systemDebugOutput(true); // Debug output to serial

	display.begin(SSD1306_SWITCHCAPVCC);
	display.clearDisplay();

	WifiStation.config(WIFI_SSID, WIFI_PWD);
	WifiStation.enable(true);
	WifiAccessPoint.enable(false);

	// Run our method when station was connected to AP (or not connected)
	WifiStation.waitConnection(connectOk, 20, connectFail); // We recommend 20+ seconds for connection timeout at start
}

void displayTemp(String s_temp) {
	display.fillRect(0, 0, 128, 32, BLACK);
	display.setTextSize(1);
	display.setTextColor(WHITE);
	display.setCursor(5,10);
	display.println(s_temp);
	display.display();
}

void displayText(String s_msg) {
	display.fillRect(0, 32, 128, 32, BLACK);
	display.setTextSize(1);
	display.setTextColor(WHITE);
	display.setCursor(5,40);
	display.println(s_msg);
	display.display();
}
