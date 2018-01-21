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


