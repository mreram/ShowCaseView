package ir.smartdevelop.eram.showcaseview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;

public class MainActivity extends AppCompatActivity {

    private View view;
    private ShowCaseTask showCaseTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = findViewById(R.id.txt);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Clicked!", Toast.LENGTH_LONG).show();
            }
        });

        showCaseTask = new ShowCaseTask();
        view.postDelayed(showCaseTask, 1000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (showCaseTask != null)
            view.removeCallbacks(showCaseTask);
    }

    class ShowCaseTask implements Runnable {

        @Override
        public void run() {
            new GuideView.Builder(MainActivity.this)
                    .setTitle("Guide Title Text")
                    .setContentText("Guide Description Text\n .....Guide Description Text\n .....Guide Description Text .....")
                    .setGravity(GuideView.Gravity.AUTO)
                    .setTargetView(view)
                    .build()
                    .show();
        }
    }
}
