# ShowCaseView
<p align="center">
<img src="./screenshots/Screenshot_2018-01-21-00-51-52.png" width="200"/>
<img src="./screenshots/Screenshot_2018-01-21-00-52-43.png" width="200"/>
<img src="./screenshots/Screenshot_2018-01-21-00-51-21.png" width="200"/>
</p>
Sample usage in your activity:

     new GuideView.Builder(this)
             .setTitle("Guide Title Text")
             .setContentText("Guide Description Text\n .....Guide Description Text\n .....Guide Description Text .....")
             .setGravity(GuideView.Gravity.CENTER)//optional
             .setTargetView(view)
             .setContentTextSize(12)//optional
             .setTitleTextSize(14)//optional
             .build()
             .show();



## Installation
	
maven:

```xml
<repositories>
   <repository>
     <id>jitpack.io</id>
     <url>https://jitpack.io</url>
   </repository>
</repositories>
```
	Step 2. Add the dependency
```xml
<dependency>
   <groupId>com.github.mreram</groupId>
   <artifactId>ShowCaseView</artifactId>
   <version>1.0.0</version>
</dependency>
```
gradle:
	
Add it in your root build.gradle at the end of repositories:
```groovy	
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```	
	Step 2. Add the dependency
```groovy	
compile 'com.github.mreram:ShowCaseView:1.0.0'
```
