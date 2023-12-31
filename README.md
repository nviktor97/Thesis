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

![system](https://github.com/nviktor97/Thesis/assets/69107380/1f4b2cca-59df-43f2-a706-3ad4ef01403b)


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


![sign](https://github.com/nviktor97/Thesis/assets/69107380/5f9683d3-7c2e-4b1c-b6b1-31ae93659590)

![menus](https://github.com/nviktor97/Thesis/assets/69107380/bd3cdd0d-35e4-48e4-94fa-228daaae5248)

![menu](https://github.com/nviktor97/Thesis/assets/69107380/115bd89b-ff32-409b-9342-2f40ecaaefe1)

![datepicker](https://github.com/nviktor97/Thesis/assets/69107380/2e3a39c3-fc59-463c-8178-c9305aa0d1e3)

![chart](https://github.com/nviktor97/Thesis/assets/69107380/54cd02ec-db25-49c0-90ed-6b5e08bac1ec)
