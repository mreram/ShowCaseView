package parvane.ir.eram.showcaseviewlib;

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


    private float density;
    private View target;
    private RectF rect;
    private GuideMessageView mMessageView;
    private boolean isTop;
    private Gravity mGravity;
    int marginGuide;

    final Paint paintLine = new Paint();
    final Paint paintCircle = new Paint();
    final Paint paintCircleInner = new Paint();
    final Paint mPaint = new Paint();
    final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Xfermode XFERMODE_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);


    public enum Gravity {
        AUTO, CENTER
    }

    public GuideView(Context context, View view) {
        super(context);
        setWillNotDraw(false);
        mGravity = Gravity.AUTO;
        this.target = view;
        density = context.getResources().getDisplayMetrics().density;

        int[] locationTarget = new int[2];
        target.getLocationOnScreen(locationTarget);
        rect = new RectF(locationTarget[0], locationTarget[1] - getStatusBarHeight(),
                locationTarget[0] + target.getWidth(),
                locationTarget[1] + target.getHeight() - getStatusBarHeight());

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
        float lineWidth = 3 * density;
        float strokeCircleWidth = 3 * density;
        float circleSize = 6 * density;
        float circleInnerSize = 4.5f * density;


        mPaint.setColor(0x99000000);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(canvas.getClipBounds(), mPaint);

        paintLine.setStyle(Paint.Style.FILL);
        paintLine.setColor(Color.WHITE);
        paintLine.setStrokeWidth(lineWidth);

        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setColor(Color.WHITE);
        paintCircle.setStrokeCap(Paint.Cap.ROUND);
        paintCircle.setStrokeWidth(strokeCircleWidth);

        paintCircleInner.setStyle(Paint.Style.FILL);
        paintCircleInner.setColor(0xffcccccc);

        marginGuide = (int) (isTop ? 15 * density : -15 * density);

        float yLineAndCircle = (isTop ? rect.bottom : rect.top) + marginGuide;
        float xLine = (rect.left / 2 + rect.right / 2);
        float cx = (target.getLeft() / 2 + target.getRight() / 2);

        canvas.drawLine(xLine, yLineAndCircle, xLine,
                y + 50 * density
                , paintLine);

        canvas.drawCircle(cx, yLineAndCircle, circleSize, paintCircle);
        canvas.drawCircle(cx, yLineAndCircle, circleInnerSize, paintCircleInner);


        targetPaint.setXfermode(XFERMODE_CLEAR);
        canvas.drawRoundRect(rect, 15, 15, targetPaint);

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
        mMessageView.setX(p.x);
        mMessageView.setY(p.y);
        requestLayout();
    }

    int x = 0;
    int y = 0;

    private Point resolveMessageViewLocation() {

        //set message view right
        if (rect.left > getWidth() / 2) {
            if (mGravity == Gravity.CENTER) {
                x = getWidth() / 2 - mMessageView.getWidth() / 2;
            } else
                x = (int) (rect.right) - mMessageView.getWidth();
        }
        //set message view left
        else if (rect.left < getWidth() / 2) {
            if (mGravity == Gravity.CENTER) {
                x = getWidth() / 2 - mMessageView.getWidth() / 2;
            } else
                x = (int) (rect.right) - mMessageView.getWidth();
        }

        if (x + mMessageView.getWidth() > getWidth())
            x -= (getWidth() - mMessageView.getWidth());
        if (x < 0)
            x = 0;

        //set message view bottom
        if (rect.top > getHeight() / 2) {
            isTop = false;
            y = (int) (rect.top - mMessageView.getHeight() - 50 * density);
        }
        //set message view top
        else {
            isTop = true;
            y = (int) (rect.bottom + mMessageView.getHeight() - 50 * density);
        }

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

    public class CustomDialog extends Dialog {

        CustomDialog(Context context, int resId) {
            super(context, resId);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void setGravity(Gravity gravity) {
        this.mGravity = gravity;
    }

    public void setTitle(String str) {
        mMessageView.setTitle(str);
    }

    public void setContentText(String str) {
        mMessageView.setContentText(str);
    }
}

