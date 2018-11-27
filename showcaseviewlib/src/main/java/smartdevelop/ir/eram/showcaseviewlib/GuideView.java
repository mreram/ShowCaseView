package smartdevelop.ir.eram.showcaseviewlib;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.os.Build;
import android.text.Spannable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */

public class GuideView extends FrameLayout {

    private static final String TAG = "GuideView";

    private static final int INDICATOR_HEIGHT              = 30;
    private static final int MESSAGE_VIEW_PADDING          = 5;
    private static final int ANIMATION_DURATION            = 300;
    private static final int CIRCLE_SIZE_INDICATOR         = 6;
    private static final int LINE_INDICATOR_WIDTH_SIZE     = 3;
    private static final int STROKE_CIRCLE_INDICATOR_SIZE  = 3;
    private static final float CIRCLE_INNER_INDICATOR_SIZE = 5f;
    private static final int RADIUS_SIZE_TARGET_RECT       = 15;
    private static final int MARGIN_INDICATOR              = 15;

    private static final int BACKGROUND_COLOR              = 0x99000000;
    private static final int CIRCLE_INNER_INDICATOR_COLOR  = 0xffcccccc;
    private static final int CIRCLE_INDICATOR_COLOR        = Color.WHITE;
    private static final int LINE_INDICATOR_COLOR          = Color.WHITE;


    private final Paint selfPaint = new Paint();
    private final Paint paintLine = new Paint();
    private final Paint paintCircle = new Paint();
    private final Paint paintCircleInner = new Paint();
    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Xfermode X_FER_MODE_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private View target;
    private RectF targetRect;
    private Rect selfRect = new Rect();

    private float density;
    private boolean isTop;
    private boolean mIsShowing;
    private int yMessageView = 0;

    private GuideListener mGuideListener;
    private Gravity mGravity;
    private DismissType dismissType;
    private GuideMessageView mMessageView;


    private GuideView(Context context, View view) {
        super(context);
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        this.target = view;

        density = context.getResources().getDisplayMetrics().density;

        int[] locationTarget = new int[2];
        target.getLocationOnScreen(locationTarget);
        targetRect = new RectF(locationTarget[0],
                locationTarget[1],
                locationTarget[0] + target.getWidth(),
                locationTarget[1] + target.getHeight());

        mMessageView = new GuideMessageView(getContext());
        final int padding = (int) (MESSAGE_VIEW_PADDING * density);
        mMessageView.setPadding(padding, padding, padding, padding);
        mMessageView.setColor(Color.WHITE);

        addView(mMessageView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setMessageLocation(resolveMessageViewLocation());

        ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);

                setMessageLocation(resolveMessageViewLocation());
                int[] locationTarget = new int[2];
                target.getLocationOnScreen(locationTarget);

                targetRect = new RectF(locationTarget[0],
                        locationTarget[1],
                        locationTarget[0] + target.getWidth(),
                        locationTarget[1] + target.getHeight());

                selfRect.set(getPaddingLeft(),
                        getPaddingTop(),
                        getWidth() - getPaddingRight(),
                        getHeight() - getPaddingBottom());


                getViewTreeObserver().addOnGlobalLayoutListener(this);
            }
        };
        getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }


    private int getNavigationBarSize() {
        Resources resources = getContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private boolean isLandscape() {
        int display_mode = getResources().getConfiguration().orientation;
        return display_mode != Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (target != null) {

            float lineWidth = LINE_INDICATOR_WIDTH_SIZE * density;
            float strokeCircleWidth = STROKE_CIRCLE_INDICATOR_SIZE * density;
            float circleSize = CIRCLE_SIZE_INDICATOR * density;
            float circleInnerSize = CIRCLE_INNER_INDICATOR_SIZE * density;

            selfPaint.setColor(BACKGROUND_COLOR);
            selfPaint.setStyle(Paint.Style.FILL);
            selfPaint.setAntiAlias(true);
            canvas.drawRect(selfRect, selfPaint);

            paintLine.setStyle(Paint.Style.FILL);
            paintLine.setColor(LINE_INDICATOR_COLOR);
            paintLine.setStrokeWidth(lineWidth);
            paintLine.setAntiAlias(true);

            paintCircle.setStyle(Paint.Style.STROKE);
            paintCircle.setColor(CIRCLE_INDICATOR_COLOR);
            paintCircle.setStrokeCap(Paint.Cap.ROUND);
            paintCircle.setStrokeWidth(strokeCircleWidth);
            paintCircle.setAntiAlias(true);

            paintCircleInner.setStyle(Paint.Style.FILL);
            paintCircleInner.setColor(CIRCLE_INNER_INDICATOR_COLOR);
            paintCircleInner.setAntiAlias(true);

            int marginGuide = (int) (isTop ? MARGIN_INDICATOR * density : -MARGIN_INDICATOR * density);

            float startYLineAndCircle = (isTop ? targetRect.bottom : targetRect.top) + marginGuide;

            float x = (targetRect.left / 2 + targetRect.right / 2);
            float stopY = (yMessageView + INDICATOR_HEIGHT * density);

            canvas.drawLine(x,
                    startYLineAndCircle,
                    x,
                    stopY,
                    paintLine);

            canvas.drawCircle(x, startYLineAndCircle, circleSize, paintCircle);
            canvas.drawCircle(x, startYLineAndCircle, circleInnerSize, paintCircleInner);

            targetPaint.setXfermode(X_FER_MODE_CLEAR);
            targetPaint.setAntiAlias(true);

            canvas.drawRoundRect(targetRect, RADIUS_SIZE_TARGET_RECT, RADIUS_SIZE_TARGET_RECT, targetPaint);
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void dismiss() {

        AlphaAnimation startAnimation = new AlphaAnimation(1f, 0f);
        startAnimation.setDuration(ANIMATION_DURATION);
        startAnimation.setFillAfter(true);
        this.startAnimation(startAnimation);
        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).removeView(this);
        mIsShowing = false;
        if (mGuideListener != null) {
            mGuideListener.onDismiss(target);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, event.toString());

        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (dismissType) {

                case outside:
                    if (!isViewContains(mMessageView, x, y)) {
                        dismiss();
                    }
                    break;

                case anywhere:
                    dismiss();
                    break;

                case targetView:
                    if (targetRect.contains(x, y)) {
                        target.performClick();
                        dismiss();
                    }
                    break;

            }
            return true;
        }
        return false;
    }

    private boolean isViewContains(View view, float rx, float ry) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        int w = view.getWidth();
        int h = view.getHeight();

        return !(rx < x || rx > x + w || ry < y || ry > y + h);
    }

    void setMessageLocation(Point p) {
        mMessageView.setX(p.x);
        mMessageView.setY(p.y);
        postInvalidate();
    }


    private Point resolveMessageViewLocation() {

        int xMessageView = 0;
        if (mGravity == Gravity.center) {
            xMessageView = (int) (targetRect.left - mMessageView.getWidth() / 2 + target.getWidth() / 2);
        } else
            xMessageView = (int) (targetRect.right) - mMessageView.getWidth();

        if (isLandscape()) {
            xMessageView -= getNavigationBarSize();
        }

        if (xMessageView + mMessageView.getWidth() > getWidth())
            xMessageView = getWidth() - mMessageView.getWidth();
        if (xMessageView < 0)
            xMessageView = 0;


        //set message view bottom
        if (targetRect.top + (INDICATOR_HEIGHT * density) > getHeight() / 2) {
            isTop = false;
            yMessageView = (int) (targetRect.top - mMessageView.getHeight() - INDICATOR_HEIGHT * density);
        }
        //set message view top
        else {
            isTop = true;
            yMessageView = (int) (targetRect.top + target.getHeight() + INDICATOR_HEIGHT * density);
        }

        if (yMessageView < 0)
            yMessageView = 0;


        return new Point(xMessageView, yMessageView);
    }


    public void show() {
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        this.setClickable(false);

        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).addView(this);
        AlphaAnimation startAnimation = new AlphaAnimation(0.0f, 1.0f);
        startAnimation.setDuration(ANIMATION_DURATION);
        startAnimation.setFillAfter(true);
        this.startAnimation(startAnimation);
        mIsShowing = true;

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
        private DismissType dismissType;
        private Context context;
        private Spannable contentSpan;
        private Typeface titleTypeFace, contentTypeFace;
        private GuideListener guideListener;
        private int titleTextSize;
        private int contentTextSize;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTargetView(View view) {
            this.targetView = view;
            return this;
        }

        /**
         * gravity GuideView
         *
         * @param gravity it should be one type of Gravity enum.
         **/
        public Builder setGravity(Gravity gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * defining a title
         *
         * @param title a title. for example: submit button.
         **/
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * defining a description for the target view
         *
         * @param contentText a description. for example: this button can for submit your information..
         **/
        public Builder setContentText(String contentText) {
            this.contentText = contentText;
            return this;
        }

        /**
         * setting spannable type
         *
         * @param span a instance of spannable
         **/
        public Builder setContentSpan(Spannable span) {
            this.contentSpan = span;
            return this;
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         **/
        public Builder setContentTypeFace(Typeface typeFace) {
            this.contentTypeFace = typeFace;
            return this;
        }

        /**
         * adding a listener on show case view
         *
         * @param guideListener a listener for events
         **/
        public Builder setGuideListener(GuideListener guideListener) {
            this.guideListener = guideListener;
            return this;
        }

        /**
         * setting font type face
         *
         * @param typeFace a instance of type face (font family)
         **/
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

        /**
         * this method defining the type of dismissing function
         *
         * @param dismissType should be one type of DismissType enum. for example: outside -> Dismissing with click on outside of MessageView
         */
        public Builder setDismissType(DismissType dismissType) {
            this.dismissType = dismissType;
            return this;
        }

        public GuideView build() {
            GuideView guideView = new GuideView(context, targetView);
            guideView.mGravity = gravity != null ? gravity : Gravity.auto;
            guideView.dismissType = dismissType != null ? dismissType : DismissType.targetView;

            guideView.setTitle(title);
            if (contentText != null)
                guideView.setContentText(contentText);
            if (titleTextSize != 0)
                guideView.setTitleTextSize(titleTextSize);
            if (contentTextSize != 0)
                guideView.setContentTextSize(contentTextSize);
            if (contentSpan != null)
                guideView.setContentSpan(contentSpan);
            if (titleTypeFace != null) {
                guideView.setTitleTypeFace(titleTypeFace);
            }
            if (contentTypeFace != null) {
                guideView.setContentTypeFace(contentTypeFace);
            }
            if (guideListener != null) {
                guideView.mGuideListener = guideListener;
            }

            return guideView;
        }


    }
}

