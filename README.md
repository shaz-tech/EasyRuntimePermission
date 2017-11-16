# EasyRuntimePermission
**Simple listener-based API to handle runtime permissions.**

## Configuration
#### Add to your gradle 
[![](https://jitpack.io/v/shaz-tech/EasyRuntimePermission.svg)](https://jitpack.io/#shaz-tech/EasyRuntimePermission)
```groovy
    allprojects {
            repositories {
                ...
                maven { url 'https://jitpack.io' }
            }
     }
        
     dependencies {
        	 compile 'com.github.shaz-tech:EasyRuntimePermission:1.0'
     }
```

## Setup
### Add to your Activity that's all
```java
    @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            RuntimePermissionHandler.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        }
```

## Usage
### Call
```java
    /*Call when required to do some tast related to permission*/
    RuntimePermissionHandler.requestPermission(YOUR_REQUEST_CODE, CURRENT_ACTIVITY, PERMISSION_LISTENER_INSTANCE, BUNCH_OF_PERMISSIONS);
```
### Handling
```java
    /*call for processing only from onAllowed of PERMISSION_LISTENER_INSTANCE*/
    @Override
    public void onAllowed(int requestCode, @NonNull String[] permissions) {
          switch (requestCode) {
              case YOUR_REQUEST_CODE:
                   //TODO Do your stuffs from here
                   break;
                }
            }
```

## With Xiaomi support
Since Xiaomi manipulates something around runtime permission mechanism Google's recommended way doesn't work well. But don't worry, EasyRuntimePermission supports it!

## Contact
Pull requests are more than welcome.
Please fell free to contact me if there is any problem when using the library.
- **Email**: me.shahbazakhtar@gmail.com
- **LinkedIn**: https://www.linkedin.com/in/shaz0017/
- **Facebook**: https://www.facebook.com/shaz2417
- **Twitter**: https://twitter.com/Shaz2417



License
--------

      Copyright [2017] [Shahbaz Akhtar]
      
      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at
      
          http://www.apache.org/licenses/LICENSE-2.0
      
      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.      
