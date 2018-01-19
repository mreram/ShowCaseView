package ir.smartdevelop.eram.showcaseview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */

public class GuideView extends FrameLayout {
    private  float density;
    View target;
    GuideMessageView mMessageView;

    RectF rect;

    public GuideView(Context context, View view) {
        super(context);
        setWillNotDraw(false);
        this.target = view;
        density = context.getResources().getDisplayMetrics().density;

        int[] locationTarget = new int[2];
        target.getLocationOnScreen(locationTarget);
        rect = new RectF(locationTarget[0], locationTarget[1] - getStatusBarHeight(),
                locationTarget[0] + target.getWidth(), locationTarget[1] + target.getHeight() - getStatusBarHeight());

        mMessageView = new GuideMessageView(getContext());
        final int padding = (int) (5 * density);
        mMessageView.setPadding(padding, padding, padding, padding);
        mMessageView.setColor(Color.WHITE);

        addView(mMessageView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


        setMessageLocation(resolveMessageViewLocation());

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setMessageLocation(resolveMessageViewLocation());
                int[] locationTarget = new int[2];
                target.getLocationOnScreen(locationTarget);
                rect = new RectF(locationTarget[0], locationTarget[1] - getStatusBarHeight(),
                        locationTarget[0] + target.getWidth(), locationTarget[1] + target.getHeight() - getStatusBarHeight());
            }
        });
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint mPaint = new Paint();
        mPaint.setColor(0x55000000);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(canvas.getClipBounds(), mPaint);

        Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Xfermode XFERMODE_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        targetPaint.setXfermode(XFERMODE_CLEAR);
        canvas.drawRoundRect(rect, 5, 5, targetPaint);

    }


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rect.contains(x, y)) {
                    target.performClick();
                    dialog.dismiss();
                }
                return true;
        }
        return false;
    }

    void setMessageLocation(Point p) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mMessageView.getLayoutParams();
        params.leftMargin = p.x;
        params.topMargin = p.y;
        requestLayout();
    }

    private Point resolveMessageViewLocation() {
        int x=0;
        int y=0;
        if (rect.left > getWidth() / 2)
            x = (int) (rect.left / 2 - mMessageView.getWidth() / 2);
        else
            x = (int) (rect.left/2 + (getWidth() - rect.left) / 2 - mMessageView.getWidth() / 2);

        if (x + mMessageView.getWidth() > getWidth())
            x -= (x + mMessageView.getWidth() - getWidth());
        if (x < 0)
            x = 0;

        y= 0;
        if (rect.top > getHeight() / 2)
            y = (int) (rect.top / 2 - mMessageView.getHeight() / 2);
        else
            y = (int) ( (getHeight() - rect.top) / 2 - mMessageView.getHeight() / 2);

        if (y + mMessageView.getHeight() > getHeight())
            y -= (y + mMessageView.getHeight() - getHeight());
        if (y < 0)
            y = 0;

        return new Point(x, y);
    }

    CustomDialog dialog;
    public void show() {
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        this.setClickable(false);

        dialog = new CustomDialog(getContext(), R.style.dialog_help_style);

        dialog.setContentView(this);
        dialog.show();

    }

    public class CustomDialog  extends Dialog {

        public CustomDialog(Context context) {
            super(context);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);

        }
        CustomDialog(Context context,int resid) {
            super(context,resid);
        }
    }

    public void setTitle(String str) {
        mMessageView.setTitle(str);
    }

    public void setContentText(String str) {
        mMessageView.setContentText(str);
    }
}

