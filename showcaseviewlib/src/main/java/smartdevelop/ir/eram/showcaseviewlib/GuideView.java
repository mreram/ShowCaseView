package smartdevelop.ir.eram.showcaseviewlib;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import android.os.Build;
import android.text.Spannable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 * Edited by Kobus Swart on 27/07/2018
 */

public class GuideView extends LinearLayout {

    private static final String TAG = "GuideViewActions";
    private TextView textViewNext, textViewPrevious;
    private TextView txtSkip;
    private int arrowHeight = 300;
    private int arrowXPercentOffset = 2;
    private int rightArrowAdjust = 100;
    private boolean hideLeftArrow = false;
    private boolean hideRightArrow = false;
    private boolean showArrows = false;
    private boolean isCircle = false;
    private int circleRadius = 0;
    private boolean isClickable = false;
    private int backgroundColor = 0xdd000000;
    private float density;
    private int messageXOffset = 0;
    private int messageYOffset = 0;
    private int showcasePadding = 0;
    private View target;
    private RectF rect;
    private GuideMessageView mMessageView;
    private boolean isTop;
    private Gravity mGravity;
    private DismissType dismissType;
    int marginGuide;
    private boolean mIsShowing;
    private GuideListener mGuideListener;
    private ArrowClickListener arrowClickListener;
    private SkipTapped skipTapped;
    int xMessageView = 0;
    int yMessageView = 0;

    final int ANIMATION_DURATION = 400;
    final Paint emptyPaint = new Paint();
    final Paint paintLine = new Paint();
    final Paint paintCircle = new Paint();
    final Paint paintCircleInner = new Paint();
    final Paint mPaint = new Paint();
    final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Xfermode XFERMODE_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private float INDICATOR_HEIGHT;

    public interface GuideListener {
        void onDismiss(View view);
    }

    public interface SkipTapped {
        void onSkipTapped();
    }
    public interface ArrowClickListener {
        void onArrowClicked(Direction direction);
    }
    public enum Gravity {
        auto, center
    }

    public enum Direction{
        next,prev
    }

    public enum DismissType {
        outside, anywhere, targetView
    }

    private GuideView(final Context context, View view) {
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

        Log.d(TAG, "MessageView added");
        addView(mMessageView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


        LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams textSkipParam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textSkipParam.gravity = android.view.Gravity.BOTTOM;

        txtSkip = new TextView(getContext());
        String textSkip = "SKIP";
        txtSkip.setText(textSkip);
        txtSkip.setTextColor(Color.WHITE);
        txtSkip.setTextSize(15f);

        addView(txtSkip, textSkipParam);

        textViewNext = new TextView(getContext());
        textViewNext.setBackground(getResources().getDrawable(R.drawable.right_arrow));
        Log.d(TAG, "TextViewNext added");

        addView(textViewNext, textParam);

        textViewPrevious = new TextView(getContext());
        textViewPrevious.setBackground(getResources().getDrawable(R.drawable.left_arrow));
        Log.d(TAG, "TextView Previous added");
        addView(textViewPrevious, textParam);

        textViewNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arrowClickListener != null) {
                    arrowClickListener.onArrowClicked(Direction.next);
                }
            }
        });

        textViewPrevious.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arrowClickListener != null) {
                    arrowClickListener.onArrowClicked(Direction.prev);
                }
            }
        });

        txtSkip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (skipTapped != null) {
                    skipTapped.onSkipTapped();
                }
            }
        });


        setMessageLocation(resolveMessageViewLocation());

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setMessageLocation(resolveMessageViewLocation());
                int[] locationTarget = new int[2];
                target.getLocationOnScreen(locationTarget);

                int totalHeight = getHeight();
                int totalWidth = getWidth();

                setTextViewNextPosition(totalWidth, totalHeight);

                if (isCircle) {
                    if (target.getWidth() > target.getHeight()) {
                        circleRadius = target.getWidth();
                    } else if (target.getHeight() > target.getWidth()) {
                        circleRadius = target.getHeight();
                    } else {
                        circleRadius = target.getWidth();
                    }
                }

                rect = new RectF(locationTarget[0] - showcasePadding, locationTarget[1] - showcasePadding,
                        locationTarget[0] + target.getWidth() + showcasePadding, locationTarget[1] + target.getHeight() + showcasePadding);
            }
        });
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
        if (messageYOffset == 0) {
            INDICATOR_HEIGHT = 30;
        } else {
            INDICATOR_HEIGHT = (float) messageYOffset;
        }
        if (target != null) {
            Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(bitmap);

            float lineWidth = 3 * density;
            float strokeCircleWidth = 3 * density;
            float circleSize = 6 * density;
            float circleInnerSize = 5f * density;


            mPaint.setColor(backgroundColor);
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

            float startYLineAndCircle = (isTop ? rect.bottom : rect.top) + marginGuide;

            float x = (rect.left / 2 + rect.right / 2);
            float stopY = (yMessageView + INDICATOR_HEIGHT * density);

            tempCanvas.drawLine(x, startYLineAndCircle, x,
                    stopY
                    , paintLine);

            tempCanvas.drawCircle(x, startYLineAndCircle, circleSize, paintCircle);
            tempCanvas.drawCircle(x, startYLineAndCircle, circleInnerSize, paintCircleInner);


            targetPaint.setXfermode(XFERMODE_CLEAR);
            targetPaint.setAntiAlias(true);

            tempCanvas.drawRoundRect(rect, circleRadius, circleRadius, targetPaint);
            canvas.drawBitmap(bitmap, 0, 0, emptyPaint);
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
        float x = event.getX();
        float y = event.getY();


        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isClickable) {
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
                        if (rect.contains(x, y)) {
                            target.performClick();
                            dismiss();
                        }
                        break;

                }
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

    void setTextViewNextPosition(int x, int y) {

        if (showArrows) {

            int yPosition = ((y / 100) * arrowHeight);
            int xPercentageValue = ((x / 100) * arrowXPercentOffset);
            float txtNextxPoint = (float) (x - (xPercentageValue + rightArrowAdjust));
            float txtNextyPoint = (float) (y - yPosition);

            textViewNext.setY(txtNextyPoint);
            textViewNext.setX(txtNextxPoint);

            float txtPrevxPoint = (float) xPercentageValue;
            float txtPrevyPoint = (float) (y - yPosition);

            textViewPrevious.setY(txtPrevyPoint);
            textViewPrevious.setX(txtPrevxPoint);

            if(hideLeftArrow) {
                textViewPrevious.setVisibility(GONE);
            }
        }else {
            textViewNext.setVisibility(GONE);
            textViewPrevious.setVisibility(GONE);
        }
    }

    void setMessageLocation(Point p) {

        //System.out.println("Message Y Offset: " + String.valueOf(messageYOffset));
        //System.out.println("Message X Offset: " + String.valueOf(messageXOffset));
        int xI = messageXOffset;
        int yI = Math.abs(messageYOffset);

        int newXPoint = p.x;
        int newYPoint = p.y;

        //System.out.println("Passed point X: " + String.valueOf(newXPoint));
        //System.out.println("Passed point Y: " + String.valueOf(newYPoint));


        float y;
        if(messageYOffset < 0){
            y = (float)(newYPoint - yI);
            //System.out.println("Calculated point Y: " + String.valueOf(y));
        }else {
            y = (float)(newYPoint + yI);
            //System.out.println("Calculated point Y: " + String.valueOf(y));
        }
        float x = (float)(newXPoint + (messageXOffset));

        //System.out.println("Calculated point : " + String.valueOf(x));

        mMessageView.setX(x);
        mMessageView.setY(y);

        mMessageView.invalidate();
        requestLayout();
    }


    private Point resolveMessageViewLocation() {

        if (mGravity == Gravity.center) {
            xMessageView = (int) (rect.left - mMessageView.getWidth() / 2 + target.getWidth() / 2);
        } else
            xMessageView = (int) (rect.right) - mMessageView.getWidth();

        if (isLandscape()) {
            xMessageView -= getNavigationBarSize();
        }

        if (xMessageView + mMessageView.getWidth() > getWidth())
            xMessageView = getWidth() - mMessageView.getWidth();
        if (xMessageView < 0)
            xMessageView = 0;


        //set message view bottom
        if (rect.top + (INDICATOR_HEIGHT * density) > getHeight() / 2) {
            isTop = false;
            yMessageView = (int) (rect.top - mMessageView.getHeight() - INDICATOR_HEIGHT * density);
        }
        //set message view top
        else {
            isTop = true;
            yMessageView = (int) (rect.top + target.getHeight() + INDICATOR_HEIGHT * density);
        }

        if (yMessageView < 0)
            yMessageView = 0;


        return new Point(xMessageView, yMessageView);
    }


    public void show() {
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setClickable(isClickable);
        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).addView(this);
        AlphaAnimation startAnimation = new AlphaAnimation(0.0f, 1.0f);
        startAnimation.setDuration(ANIMATION_DURATION);
        startAnimation.setFillAfter(true);
        this.startAnimation(startAnimation);
        mIsShowing = true;

    }

    private void setTitle(String str) {
        mMessageView.setTitle(str);
    }

    private void setContentText(String str) {
        mMessageView.setContentText(str);
    }

    private void setArrows(boolean state) {
        showArrows = state;
    }

    public void setClickable(boolean state) {
        isClickable = state;
    }

    private void setContentSpan(Spannable span) {
        mMessageView.setContentSpan(span);
    }

    private void setTitleTypeFace(Typeface typeFace) {
        mMessageView.setTitleTypeFace(typeFace);
    }

    private void setContentTypeFace(Typeface typeFace) {
        mMessageView.setContentTypeFace(typeFace);
    }

    public void setArrowXPercentOffset(int value){
        arrowXPercentOffset = value;
    }
    public void setArrowHeight(int value){
        arrowHeight = value;
    }
    public void setRightArrowAdjust(int value){
        rightArrowAdjust = value;
    }

    private void setTitleTextSize(int size) {
        mMessageView.setTitleTextSize(size);
    }


    private void setContentTextSize(int size) {
        mMessageView.setContentTextSize(size);
    }

    private void setShowcasePadding(int padding) {
        showcasePadding = padding;
    }

    private void setXOffset(int xOffset) {
        messageXOffset = xOffset;
        System.out.println("Message X Offset: " + String.valueOf(messageXOffset));
    }

    private void setYOffset(int yOffset) {
        messageYOffset = yOffset;
        System.out.println("Message Y Offset: " + String.valueOf(messageYOffset));

    }

    private void setHideLeftArrow(boolean state){hideLeftArrow = state;}
    private void setHideRightArrow(boolean state){hideRightArrow = state;}

    private void setBackGroundColor(int color) {
        backgroundColor = color;
    }

    private void setMessageBoxBackground(int color) {
        mMessageView.setColor(color);
    }

    private void setTitleTextColor(int color) {
        mMessageView.setTitleColor(color);
    }

    private void setDescriptiveTextColor(int color) {
        mMessageView.setDescriptiveColor(color);
    }

    private void setIsCircle(boolean value) {
        isCircle = value;
    }

    private void setCornerRadius(int value) {
        circleRadius = value;
    }


    public static class Builder {
        private View targetView;
        private boolean isCircle;
        private int arrowHeight;
        private int arrowXPercentOffset;
        private int rightArrowAdjust;
        private boolean showArrow;
        private boolean hideLeftArrow;
        private boolean hideRightArrow;
        private boolean isClickable;
        private int messageBoxColor;
        private int titleColor;
        private int descriptionColor;
        private int messageXOffset;
        private int messageYOffset;
        private int showcasePadding;
        private int backgroundColor;
        private int cornerRadius;
        private String title, contentText;
        private Gravity gravity;
        private DismissType dismissType;
        private Context context;
        private int titleTextSize;
        private int contentTextSize;
        private Spannable contentSpan;
        private Typeface titleTypeFace, contentTypeFace;
        private GuideListener guideListener;
        private ArrowClickListener arrowClickListener;
        private SkipTapped skipTapped;
        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTargetView(View view) {
            this.targetView = view;
            return this;
        }

        public Builder setArrows(boolean state) {
            this.showArrow = state;
            return this;
        }

        public Builder hideLeftArrow(boolean state){
            this.hideLeftArrow = state;
            return this;
        }

        public Builder hideRightArrow(boolean state){
            this.hideRightArrow = state;
            return this;
        }

        public Builder setClickable(boolean state) {
            this.isClickable = state;
            return this;
        }

        public Builder setArrowClickListener(ArrowClickListener arrowClickListener) {
            this.arrowClickListener = arrowClickListener;
            return this;
        }
        public Builder setOnSkipTapped(SkipTapped skipTapped) {
            this.skipTapped = skipTapped;
            return this;
        }
        public Builder setCircleView(boolean isCircle) {
            this.isCircle = isCircle;
            return this;
        }

        public Builder setMessageBoxBackground(int color) {
            this.messageBoxColor = color;
            return this;
        }

        public Builder setCornerRadius(int radius) {
            this.cornerRadius = radius;
            return this;
        }

        public Builder setArrowXPercentOffset(int value){
            this.arrowXPercentOffset = value;
            return this;
        }
        public Builder setArrowHeight(int value){
            this.arrowHeight = value;
            return this;
        }
        public Builder setRightArrowAdjust(int value){
            this.rightArrowAdjust = value;
            return this;
        }

        public Builder setTitleTextColor(int color) {
            this.titleColor = color;
            return this;
        }

        public Builder setDescriptionTextColor(int color) {
            this.descriptionColor = color;
            return this;
        }

        public Builder setBackgroundColor(int color) {
            this.backgroundColor = color;
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

        public Builder setMessageXOffset(int messageXOffset) {
            this.messageXOffset = messageXOffset;
            return this;
        }

        public Builder setMessageYOffset(int messageYOffset) {
            this.messageYOffset = messageYOffset;
            return this;
        }

        public Builder setShowcasePadding(int showcasePadding) {
            this.showcasePadding = showcasePadding;
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

        public Builder setGuideListener(GuideListener guideListener) {
            this.guideListener = guideListener;
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
            if (arrowClickListener != null) {
                guideView.arrowClickListener = arrowClickListener;
            }
            if(skipTapped != null){
                guideView.skipTapped = skipTapped;
            }
            if(hideLeftArrow){
                guideView.setHideLeftArrow(hideLeftArrow);
            }
            if(hideRightArrow){
                guideView.setHideRightArrow(hideRightArrow);
            }
            if (showcasePadding != 0) {
                guideView.setShowcasePadding(showcasePadding);
            }
            System.out.println("Message X Offset in BUILDER: " + String.valueOf(messageXOffset));
            if (messageXOffset != 0) {
                guideView.setXOffset(messageXOffset);
            }
            System.out.println("Message Y Offset in BUILDER: " + String.valueOf(messageYOffset));
            if (messageYOffset != 0) {
                System.out.println("NEW MESSAGE Y OFFSET IS SET");
                guideView.setYOffset(messageYOffset);
            }
            if (backgroundColor != 0) {
                guideView.setBackGroundColor(backgroundColor);
            }
            if (messageBoxColor != 0) {
                guideView.setMessageBoxBackground(messageBoxColor);
            }
            if (titleColor != 0) {
                guideView.setTitleTextColor(titleColor);
            }
            if (arrowHeight != 0) {
                guideView.setArrowHeight(arrowHeight);
            }
            if (rightArrowAdjust != 0) {
                guideView.setRightArrowAdjust(rightArrowAdjust);
            }
            if (arrowXPercentOffset != 0) {
                guideView.setArrowXPercentOffset(arrowXPercentOffset);
            }
            if (descriptionColor != 0) {
                guideView.setDescriptiveTextColor(descriptionColor);
            }
            if (isCircle) {
                guideView.setIsCircle(isCircle);
            }

            if (showArrow) {
                guideView.setArrows(showArrow);
            }
            if (isClickable) {
                guideView.setClickable(isClickable);
            }
            if (cornerRadius != 0 && !isCircle) {
                guideView.setCornerRadius(cornerRadius);
            }

            return guideView;
        }


    }
}

