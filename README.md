# Lynx
Professional Networking with Just a Handshake.  
Second-place winner at the 2015 Berkeley #CampusInWatch hackathon!

## Description
**Lynx** is a physical social network built for the InWatchZ smartwatch. Lynx defines and listens for a physical handshake from the wrist, triggering a callback the Bluetooth libraries. On a high level, Lynx seeks to automate professional networking by programmatically exchanging contact information. This is particularly useful for smart watches, providing an unintrusive way to organize and keep in touch with professional connections.

Lynx is composed of three components: the handshake, Bluetooth, and UI.
The handshake detector uses gyroscopes and the accelerometer to define a handshake past a threshold for angle and acceleration.
This sends a callback to the Bluetooth module, which "heats up" the current device for 15 seconds and scans for other "hot" devices. Once one is found within a reasonable distance (using signal strength), the device attempts to establish a connection and sends a predefined string.
The UI is self-explanatory; it helps to integrate the above features and gives the user a way to interact with the app.

## Screenshots
![lynx_layouts-01](https://cloud.githubusercontent.com/assets/5403584/6320271/0774d232-ba8e-11e4-9ef0-6f1b7cf69a1f.png)
![lynx_layouts-02](https://cloud.githubusercontent.com/assets/5403584/6320272/0784f554-ba8e-11e4-9005-9477b29c68b5.png)
![lynx_layouts-04](https://cloud.githubusercontent.com/assets/5403584/6320273/0789e50a-ba8e-11e4-97ac-7f2b5668d832.png)
![lynx_layouts-05](https://cloud.githubusercontent.com/assets/5403584/6320292/578f6516-ba8e-11e4-859c-1bf76e1b4b4b.png)
![lynx_layouts-06](https://cloud.githubusercontent.com/assets/5403584/6320274/078d5406-ba8e-11e4-902f-bc942eaaaa65.png)

## Contributing
1. Fork it!
2. Create your feature branch (`git checkout -b my-new-feature`).
3. Commit your changes (`git commit -am 'Add some feature'`).
4. Push to the branch (`git push origin my-new-feature`).
5. Create new Pull Request.
6. Darren eats you alive on code review.

## Authors
Written by Diane, Zekun, and Darren at the 2015 #CampusInWatch hackathon at UC Berkeley.  
If you want to support us, please hire us! Or just contribute by pushing code!
