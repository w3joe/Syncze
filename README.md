# Introduction
The goal is to create a tracking watch with fall detection system such that the location of the watch can be seen on an android app at all time. We used google API geolocation as it allows the app to locate the watch even underground, something satelite geolocation may be unable to do. We wanted to create an open-source and cost-effective solution for basic tracking functionalities, to ensure that the wearer of this device is safe.

![image](https://user-images.githubusercontent.com/84664178/139240601-24b1171d-1ba2-4abe-9304-110b3bdbbe8d.png)

# Syncze Tracker Watch

<img width="836" alt="Screenshot 2022-03-06 152944" src="https://user-images.githubusercontent.com/84664178/156913532-74359ad1-b1e4-4a51-947e-8802b468b7a7.png">
<img width="683" alt="Screenshot 2022-03-06 152915" src="https://user-images.githubusercontent.com/84664178/156913536-9ba622fc-b187-4a5b-aaaa-49f35e44a879.png">
<img width="1304" alt="Screenshot 2022-03-06 153215" src="https://user-images.githubusercontent.com/84664178/156913627-293e6a4b-6142-49b1-a1cc-ed182b4a9f3a.png">
<img width="1301" alt="Screenshot 2022-03-06 153203" src="https://user-images.githubusercontent.com/84664178/156913628-7dd2f19e-ce71-4b0f-8eac-f86fd16ec1f0.png">


# Features
Geolocation (Tracking) <br/>
Fall detection <br/>
Geofence <br/>
App display <br/>


# Component used:
MPU6050 x1 <br/>
Esp32 Wroom 32 x1 <br/>
FT232RL x1 <br/>

# Uploading code to Esp32 Wroom 32
For this project we will be using arduino ide to code our esp32. As the esp32 is only a chip, we would need a medium to upload the code into it. That is where the FT232RL comes in. ![Screenshot 2021-10-27 202128](https://user-images.githubusercontent.com/85302236/139065392-ed45c164-de73-4d6c-bd9f-b7a398ceec3a.png) <br/>
In the arduino ide, change the development board to esp32 Dev Module.


# Geolocation 
For geolocation, all we need is our esp32 Wroom 32.
Google takes the input of our nearby WiFi routers and gives us the coordinates. For that, Google provides API and in that API we need to provide some inputs like details of nearby WiFi routers, detail of nearby cell towers etc. Before using that API you need to get your API key working. To create your own Google geolocation API key, and to learn how Google’s geolocation API works, go [here](https://developers.google.com/maps/documentation/geolocation/overview).<br>
If you just want to try out the geolocation, you can just watch [this video](https://www.youtube.com/watch?v=9CmGMYnHR-U&t=455s) <b> *Note: </b> Check the pin of the connection as the video is using a esp8266. <br/>







