#include <ESP8266WiFi.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <FirebaseESP8266.h>
#include <ESP_Mail_Client.h>

/* Készítő: Némethy Viktor QBSH4K */

#define WIFI_SSID "" /*wifi nevének helye */
#define WIFI_PASSWORD "" /* wifi jelszavának helye */

#define SMTP_HOST "smtp.gmail.com" /* az email küldéshez használt SMTP szerver */
#define SMTP_PORT 465 /* az email küldéshez használt SMTP port */

#define AUTHOR_EMAIL "PeteJay555@gmail.com" /* levél küldő email cím */
#define AUTHOR_PASSWORD "Blah34??" /* levél küldő email cím jelszava */

#define RECIPIENT_EMAIL "PeteJay555@gmail.com" /* a leveleket fogadó fél címe */

/* Adatbázis adatok megadása */
#define FIREBASE_HOST "hidrodatabase-default-rtdb.firebaseio.com" /* az adatbázis címe */
#define FIREBASE_AUTH "Oj59VL14NKEKiXMYn02jVRCUoNPlYlc6SrAA6hDb" /* az adatbázishoz tartozó titkos kulcs  */

/* Data objektum definiálása */
FirebaseData fbdo;

/* NTP kliens definiálása az idő eléréséhez */
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");

/* Általános célú bemenet (GPIO) ahova a DS18B20 szenzor csatlakoztatva van */
const int oneWireBus = 14;     

/* oneWire instance létrehozása, hogy kommunikálhassunk bármilyen OneWire eszközzel */
OneWire oneWire(oneWireBus);

/* oneWire referencia átadása a Dallas Temperature szenzornak */
DallasTemperature sensors(&oneWire);


int pumpOn = 0;
int pumpStart = 0;
int pumpAuto = 0;
int switchStatePrevSW1 = 0;
int switchStateSW1 = 0;
int switchStatePrevSW2 = 0;
int switchStateSW2 = 0;
int currentHourPrev;
int monthDayPrev;
float tempHigh = 30;
float tempLow = 20;
String lowLevel = "The water level is low! - Sent from ESP board";
String lowTemp = "The temperature is too low! - Sent from ESP board";
String highTemp = "The temperature is too high! - Sent from ESP board";
String OutStr;
String Hours[24] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" };

void setup() {

  pinMode(D0,OUTPUT);
  digitalWrite(D0,HIGH);
  pinMode(D1,INPUT);
  pinMode(D2,INPUT);

  Serial.begin(115200);
  
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.println("WIFI Connected!");

  /* Az NTPClient inicializálása az idő eléréséért */
  timeClient.begin();
  timeClient.setTimeOffset(3600);

  /* DS18B20 szenzor indítása */
  sensors.begin();

  /* Csatlakozás a Firebase-hez */
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  
  switchStatePrevSW1 = digitalRead(D1);
  switchStatePrevSW2 = digitalRead(D2);
  Firebase.setInt(fbdo, "/Water_Level", switchStatePrevSW2);
  
}

void sendMail(String txtMsg){

  /* SMTP Session objektum az email küldésre */
  SMTPSession smtp;

  /* A session config data deklarálása */
  ESP_Mail_Session session;

  /* Session config beállítása */
  session.server.host_name = SMTP_HOST;
  session.server.port = SMTP_PORT;
  session.login.email = AUTHOR_EMAIL;
  session.login.password = AUTHOR_PASSWORD;
  session.login.user_domain = "";

  /* Message osztály deklarálása */
  SMTP_Message message;

  /* Az üzenet fejlécének beállítása */
  message.sender.name = "ESP";
  message.sender.email = AUTHOR_EMAIL;
  message.subject = "ESP Warning";
  message.addRecipient("Owner", RECIPIENT_EMAIL);
  
  /* Nyers text üzenet küldése */  
  message.text.content = txtMsg.c_str();
  
  /* Csatlakozás a szerverre a session config-al */
  smtp.connect(&session);

  /* Email küldésének kezdése és a session lezárása */
  MailClient.sendMail(&smtp, &message);
    
}

void loop() {

  timeClient.update();

  unsigned long epochTime = timeClient.getEpochTime();
 
   /* Az idő struktúra lekérdezése */
  struct tm *ptm = gmtime ((time_t *)&epochTime);

  int currentHour = ptm->tm_hour;
  int monthDay = ptm->tm_mday;
  int currentMonth = ptm->tm_mon+1;
  int currentYear = ptm->tm_year+1900;

  if (currentHourPrev != currentHour) {
    
    /* Adat olvasása a szenzorból Celsius fokban  */
    sensors.requestTemperatures();
    float temperatureC = sensors.getTempCByIndex(0);
    Firebase.getInt(fbdo, "/Low_Temp");
    tempLow = fbdo.intData();

    Firebase.getInt(fbdo, "/High_Temp");
    tempHigh = fbdo.intData();
    
    if (temperatureC < tempLow){
      sendMail(lowTemp);
    }

    if (temperatureC > tempHigh){
      sendMail(highTemp);
    }

    /* Adatküldés a Firebase adatbázisba */
    OutStr ="Date/" + String(currentYear) + "-" + String(currentMonth) + "-" + String(monthDay)+ "/" + String(currentHour);

    Firebase.setFloat(fbdo, OutStr, round(temperatureC));
  
    currentHourPrev = currentHour;
  }

  /* A kapcsolók állapotának beolvasása */
  switchStateSW1 = digitalRead(D1);
  switchStateSW2 = digitalRead(D2);

  /* Alacsony folyadékszint ellenőrzése */
  if (switchStateSW2 != switchStatePrevSW2) {
        
    Firebase.setInt(fbdo, "/Water_Level", switchStateSW2);
       
    if (switchStateSW2 == 0 && switchStatePrevSW2 == 1 && switchStateSW1 ==0) {    
      pumpStart = 1;
      sendMail(lowLevel);      
    }     
    switchStatePrevSW2 = switchStateSW2;        
  }

  /* Magas folyadékszint ellenőrzése, pumpa leállítása */
  if (switchStateSW1 != switchStatePrevSW1) {
    if (switchStateSW1 == 1 && switchStatePrevSW1 ==0 && switchStateSW2 ==1) {
      pumpStart = 0;
      digitalWrite(D0,HIGH);
      Firebase.setInt(fbdo, "/Pump_Status", 0);
      Firebase.setInt(fbdo, "/Pump_On", 0);    
    }
    switchStatePrevSW1 = switchStateSW1;   
  }

  if (pumpStart == 1){
    if (Firebase.getInt(fbdo, "/Pump_On")) {     
      pumpOn = fbdo.intData();      
    }  

    if (Firebase.getInt(fbdo, "/Auto_Mode")) {     
      pumpAuto = fbdo.intData();      
    }  

  }

  /* A pumpa indítása */
  if ((pumpStart == 1 && pumpOn == 1) || (pumpStart == 1 && pumpAuto == 1)) {   
    digitalWrite(D0,LOW);
    Firebase.setInt(fbdo, "/Pump_Status", 1);
  }
      
}
