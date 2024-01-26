package ir.smartdevelop.eram.showcaseview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;
import smartdevelop.ir.eram.showcaseviewlib.config.PointerType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class MainActivity extends AppCompatActivity {

    View view1;
    View view2;
    View view3;
    View view4;
    View view5;
    View view6;
    private GuideView mGuideView;
    private GuideView.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);
        view3 = findViewById(R.id.view3);
        view4 = findViewById(R.id.view4);
        view5 = findViewById(R.id.view5);
        view6 = findViewById(R.id.view6);

        builder = new GuideView.Builder(this)
                .setTitle("Guide Title Text")
                .setContentText("Guide Description Text\n .....Guide Description Text\n .....Guide Description Text .....")
                .setGravity(Gravity.center)
                .setDismissType(DismissType.anywhere)
                .setPointerType(PointerType.circle)
                .setTargetView(view1)
                .setGuideListener(view -> {
                    switch (view.getId()) {
                        case R.id.view1:
                            builder.setTargetView(view2).build();
                            break;
                        case R.id.view2:
                            builder.setTargetView(view3).build();
                            break;
                        case R.id.view3:
                            builder.setTargetView(view4).build();
                            break;
                        case R.id.view4:
                            builder.setTargetView(view5).setContentTextColor(Color.GREEN).build();
                            break;
                        case R.id.view5:
                            builder.setTargetView(view6).setTitleTextColor(Color.RED)
                                    .setMessageBackgroundColor(Color.BLACK).build();
                            break;
                        case R.id.view6:
                            return;
                    }
                    mGuideView = builder.build();
                    mGuideView.show();
                });

        mGuideView = builder.build();
        mGuideView.show();

        updatingForDynamicLocationViews();
    }

    private void updatingForDynamicLocationViews() {
        view4.setOnFocusChangeListener((view, b) -> mGuideView.updateGuideViewLocation());
    }

}
