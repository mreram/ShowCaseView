package ir.smartdevelop.eram.showcaseview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;

public class MainActivity extends AppCompatActivity {

    private GuideView mGuideView;
    private GuideView.Builder builder;

    int currentTargetViewId;
    View currentTargetView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final View view1 = findViewById(R.id.view1);
        currentTargetViewId = R.id.view1;

        builder = new GuideView.Builder(MainActivity.this)
                .setTitle("Guide Title Text")
                .setContentText("Guide Description Text\n .....Guide Description Text\n .....Guide Description Text .....")
                .setGravity(GuideView.Gravity.center)
                .setDismissType(GuideView.DismissType.outside)
                .setMessageBoxBackground(Color.WHITE)
                .setBackgroundColor(0x88002244)
                .setCircleView(false)
                .setArrows(true)
                .setArrowClickListener(new GuideView.ArrowClickListener() {
                    @Override
                    public void onArrowClicked(GuideView.Direction direction) {
                        mGuideView.dismiss();
                        switch (currentTargetViewId) {
                            case R.id.view1:
                                currentTargetViewId = direction == GuideView.Direction.next ? R.id.view2 : R.id.view1;
                                currentTargetView = findViewById(currentTargetViewId);
                                break;
                            case R.id.view2:
                                currentTargetViewId = direction == GuideView.Direction.next ? R.id.view3 : R.id.view1;
                                break;
                            case R.id.view3:
                                currentTargetViewId = direction == GuideView.Direction.next ? R.id.view4 : R.id.view2;
                                break;
                            case R.id.view4:
                                currentTargetViewId = direction == GuideView.Direction.next ? R.id.view5 : R.id.view3;
                                break;
                            case R.id.view5:
                                currentTargetViewId = direction == GuideView.Direction.next ? -1 : R.id.view4;
                                break;
                        }

                        if (currentTargetViewId != -1) {
                            currentTargetView = findViewById(currentTargetViewId);
                            builder.setCircleView(false);
                            mGuideView = builder.setTargetView(currentTargetView).build();
                            mGuideView.show();
                        }
                    }
                })
                .setClickable(true)
                .setCornerRadius(10)
                .setTargetView(view1);

        mGuideView = builder.build();
        mGuideView.show();
    }

}
