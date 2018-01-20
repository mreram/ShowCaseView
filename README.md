# ShowCaseView

<img src="./screenshots/Screenshot_2018-01-21-00-51-52.png" width="300">
<img src="./screenshots/Screenshot_2018-01-21-00-52-43.png" width="300">
<img src="./screenshots/Screenshot_2018-01-21-00-51-21.png" width="300">

Sample usage in your activity:

    GuideView guideView = new GuideView(this, view);
    guideView.setTitle("Guide Title Text");
    guideView.setContentText("Guide Description Text\n .....Guide Description Text\n .....Guide Description Text .....");
    guideView.setGravity(GuideView.Gravity.CENTER); //optional - default is AUTO
    guideView.show();


