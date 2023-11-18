# Thesis

Hardware

There is a c++ code written with Arduino IDE that was created to control a Hydroponic system.
This system had two water canisters simulating the fluid absorbtion by the plants,
by letting the water pour out from one canister to the other and from the other cannister I could pump back the water with a water pump.
The correct water level control was achieved with two floating magnetic switch, if the water level was under the lower one the canister should be filled,
but if the upper one is switched, that means the water level is high enough, so the water pump should stop.
I had a water resistant thermometer that mesured the water temperatur every hour.
All of this was controlled with a NodeMCU ESP8266 microcontroller.
The microcontroller connected to the WiFi and sent email warning about the important happening like, low water level, too low or high temperature.
It also was connected to a Googgle Firebase live database where it uploaded the temperature data, the actual water level and if the water pump was on or off.

Firebase

The Firebase live databes had the hourly temp data stored with dates. For example  2022.01.12. was a perent node,
it had 12 children for the hours and those children were a key and value pair with the key being the hour and the value being the temperature.
It had the automatic mode flag, the water pump state and water level with a boolean. High and low water temperature limits for warnings.
The users for the android apps were administered in this database.
At the moment the database is suspened, not used.

Android app

After a login by the Google Firebase authentication we are presented with a menu.
The first one is for the actual data and water pump control, we can set an automatic water control mode too here.
We can choose a date with a datepicker and we are presented with a chart visualizing the temperature data for that day.

The charts were provided by jjoe64's GraphView

Link: https://github.com/jjoe64/GraphView

We can set the water temperature limits after that those are stored in the fire base where the microcontroler can read them.
We can change our password.
Logout.
There are push notifications about the important events.
