package smartdevelop.ir.eram.showcaseviewlib;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.text.Spannable;
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


    private static final float INDICATOR_HEIGHT = 50;

    private float density;
    private View target;
    private RectF rect;
    private GuideMessageView mMessageView;
    private boolean isTop;
    private Gravity mGravity;
    int marginGuide;

    final Paint emptyPaint = new Paint();
    final Paint paintLine = new Paint();
    final Paint paintCircle = new Paint();
    final Paint paintCircleInner = new Paint();
    final Paint mPaint = new Paint();
    final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Xfermode XFERMODE_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);


    public enum Gravity {
        AUTO, CENTER
    }

    private GuideView(Context context, View view) {
        super(context);
        setWillNotDraw(false);

        this.target = view;

        density = context.getResources().getDisplayMetrics().density;

        int[] locationTarget = new int[2];
        target.getLocationOnScreen(locationTarget);
        rect = new RectF(locationTarget[0], locationTarget[1],
                locationTarget[0] + target.getWidth(),
                locationTarget[1] + target.getHeight());

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
                rect = new RectF(locationTarget[0], locationTarget[1],
                        locationTarget[0] + target.getWidth(), locationTarget[1] + target.getHeight());
            }
        });
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (target != null) {
            Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(bitmap);

            float lineWidth = 3 * density;
            float strokeCircleWidth = 3 * density;
            float circleSize = 6 * density;
            float circleInnerSize = 4.5f * density;


            mPaint.setColor(0x99000000);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);
            tempCanvas.drawRect(canvas.getClipBounds(), mPaint);

            paintLine.setStyle(Paint.Style.FILL);
            paintLine.setColor(Color.WHITE);
            paintLine.setStrokeWidth(lineWidth);
            paintLine.setAntiAlias(true);

            paintCircle.setStyle(Paint.Style.STROKE);
            paintCircle.setColor(Color.WHITE);
            paintCircle.setStrokeCap(Paint.Cap.ROUND);
            paintCircle.setStrokeWidth(strokeCircleWidth);
            paintCircle.setAntiAlias(true);

            paintCircleInner.setStyle(Paint.Style.FILL);
            paintCircleInner.setColor(0xffcccccc);
            paintCircleInner.setAntiAlias(true);

            marginGuide = (int) (isTop ? 15 * density : -15 * density);

            float yLineAndCircle = (isTop ? rect.bottom : rect.top) + marginGuide;
            float xLine = (rect.left / 2 + rect.right / 2);
            float cx = (target.getLeft() / 2 + target.getRight() / 2);

            tempCanvas.drawLine(xLine, yLineAndCircle, xLine,
                    y + INDICATOR_HEIGHT * density
                    , paintLine);

            tempCanvas.drawCircle(cx, yLineAndCircle, circleSize, paintCircle);
            tempCanvas.drawCircle(cx, yLineAndCircle, circleInnerSize, paintCircleInner);


            targetPaint.setXfermode(XFERMODE_CLEAR);
            targetPaint.setAntiAlias(true);

            tempCanvas.drawRoundRect(rect, 15, 15, targetPaint);
            canvas.drawBitmap(bitmap, 0, 0, emptyPaint);

        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void dismiss() {
        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).removeView(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rect.contains(x, y)) {
                    target.performClick();
                    dismiss();
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

        if (mGravity == Gravity.CENTER) {
            x = getWidth() / 2 - mMessageView.getWidth() / 2;
        } else
            x = (int) (rect.right) - mMessageView.getWidth();


        if (x + mMessageView.getWidth() > getWidth())
            x -= (getWidth() - mMessageView.getWidth());
        if (x < 0)
            x = 0;

        //set message view bottom
        if (rect.top + (INDICATOR_HEIGHT * density) > getHeight() / 2) {
            isTop = false;
            y = (int) (rect.top - mMessageView.getHeight() - INDICATOR_HEIGHT * density);
        }
        //set message view top
        else {
            isTop = true;
            y = (int) (rect.bottom + mMessageView.getHeight() - INDICATOR_HEIGHT * density);
        }

        if (y < 0)
            y = 0;


        return new Point(x, y);
    }


    public void show() {
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        this.setClickable(false);

        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).addView(this);


    }

    public void setTitle(String str) {
        mMessageView.setTitle(str);
    }

    public void setContentText(String str) {
        mMessageView.setContentText(str);
    }


    public void setContentSpan(Spannable span) {
        mMessageView.setContentSpan(span);
    }

    public void setTitleTypeFace(Typeface typeFace) {
        mMessageView.setTitleTypeFace(typeFace);
    }

    public void setContentTypeFace(Typeface typeFace) {
        mMessageView.setContentTypeFace(typeFace);
    }


    public void setTitleTextSize(int size) {
        mMessageView.setTitleTextSize(size);
    }


    public void setContentTextSize(int size) {
        mMessageView.setContentTextSize(size);
    }



    public static class Builder {
        private View targetView;
        private String title, contentText;
        private Gravity gravity;
        private Context context;
        private int titleTextSize;
        private int contentTextSize;
        private Spannable contentSpan;
        private Typeface titleTypeFace,contentTypeFace;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTargetView(View view) {
            this.targetView = view;
            return this;
        }

        public Builder setGravity(Gravity gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContentText(String contentText) {
            this.contentText = contentText;
            return this;
        }

        public Builder setContentSpan(Spannable span) {
            this.contentSpan = span;
            return this;
        }

        public Builder setContentTypeFace(Typeface typeFace) {
            this.contentTypeFace = typeFace;
            return this;
        }

        public Builder setTitleTypeFace(Typeface typeFace) {
            this.titleTypeFace = typeFace;
            return this;
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        public Builder setContentTextSize(int size) {
            this.contentTextSize = size;
            return this;
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        public Builder setTitleTextSize(int size) {
            this.titleTextSize = size;
            return this;
        }

        public GuideView build() {
            GuideView guideView = new GuideView(context, targetView);
            guideView.mGravity = gravity != null ? gravity : Gravity.AUTO;
            guideView.setTitle(title);
            if (contentText != null)
                guideView.setContentText(contentText);
            if (titleTextSize != 0)
                guideView.setTitleTextSize(titleTextSize);
            if (contentTextSize != 0)
                guideView.setContentTextSize(contentTextSize);
            if (contentSpan != null)
                guideView.setContentSpan(contentSpan);
            if(titleTypeFace!=null){
                guideView.setTitleTypeFace(titleTypeFace);
            }
            if(contentTypeFace!=null){
                guideView.setContentTypeFace(contentTypeFace);
            }
            return guideView;
        }


    }
}

