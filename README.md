##EasyFlashlight
An easy to use API for Flashlight operations using Android's Camera2 API.

For a quick demo check out the [Simple Sample Demo](https://raw.githubusercontent.com/PDDStudio/easyflashlight/master/sample.apk)

###Include via Gradle
```
dependencies {
	//other dependencies here
	compile 'com.pddstudio:easyflashlight:0.1.0'
}
```

###Include into your Code
In your Activity's onCreate() method you simply initialize EasyFlashlight.
```java

@Override
protected void onCreate(Bundle savedInstanceState) {
	//other code above...
	EasyFlashlight.init(this);
	//other code below...
}

```

###Check Flashlight availability and permission (Android M)
Before interacting with the EasyFlashlight API you should make sure that the Flashlight is present and that you're allowed to use it.

EasyFlashlight will handle the most stuff itself, so all you should do is to request the value before interacting with the flashlight

```java
if(EasyFlashlight.getInstance().canAccessFlashlight) {
	//now you can interact safely with the Flashlight API
}
```
On Android M the user can deny the camera permission, which ends up with that you can't interact with the Flashlight API anymore. To avoid unwanted crashes you should add this call to your Activity's code:

```java
 @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //For Android M Support, call this in your onRequestPermissionsResult method
        EasyFlashlight.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
```

###Interacting with the Flashlight
Because this library is called *EasyFlashlight* and a Flashlight can't do more than either light on or light off, there are only two methods in this API.

To turn on the Flashlight:

```java
EasyFlashlight.getInstance().turnOn();
```

To turn off the Flashlight:

```java
EasyFlashlight.getInstance().turnOff();
```

###Author
* Patrick J
* [Google+ Profile](http://google.com/+PatrickJung42)
* Email: patrick.pddstudio[-at-]gmail.com

###License
    Copyright 2015 Patrick J

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.