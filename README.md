## Smart Medicine Cap Notification Application
Purdue University, Fort Wayne
Senior design project; Computer Engineering Technology 
Credit: Alekzander Green, Mike Hurtle, Dr. Hongli Luo

### Senior design project
For Mike and I's (Alekzander Green) senior design project, we decided to develop a smart medicine cap that is able to interface with an Android application in order to help individuals that have trouble remembering to take their medicine in a timely manner. We decided to develop this project after seeing many family members struggling with this very problem in addition to occasionally struggling with this problem ourselves. Additionally, this information can be used to tell if a medicine bottle is being opened at unexpected times (e.g., children/elderly accidentally taking medicine after already taking it or medicine is generally being abused).

This repository contains the Android notification application that interfaces with the smart medicine cap.

### Notification application
The notification application is capable of telling when the medicine cap is opened and when it is closed. Given this information, it is able to make assumptions about when medicine is taken. This could be considered naive, however, other ways of getting this information are considerably more complex given the timeframe. This information can be used to keep a log of when medicine is taken, set off alarms when medicine is not taken by a certain time, and much more.

#### Relevant technologies
- Java
- Android studio
- Android ble, alarms, views, viewmodels, observers, services, fragments, fragment stacks, adapters, sms, intents
- Room databases
- Bluetooth LE

#### Relevant skills
- Java programming
- Android programming
- Advanced Android programming techniques
- Working with BLE
