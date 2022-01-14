package smartdevelop.ir.eram.showcaseviewlib;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.os.Build;
import android.text.Spannable;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;
import smartdevelop.ir.eram.showcaseviewlib.config.PointerType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */

@SuppressLint("ViewConstructor")
public class GuideView extends FrameLayout {

    private static final int INDICATOR_HEIGHT = 40;
    private static final int MESSAGE_VIEW_PADDING = 5;
    private static final int SIZE_ANIMATION_DURATION = 700;
    private static final int APPEARING_ANIMATION_DURATION = 400;
    private static final int CIRCLE_INDICATOR_SIZE = 6;
    private static final int LINE_INDICATOR_WIDTH_SIZE = 3;
    private static final int STROKE_CIRCLE_INDICATOR_SIZE = 3;
    private static final int RADIUS_SIZE_TARGET_RECT = 15;
    private static final int MARGIN_INDICATOR = 15;

    private static final int BACKGROUND_COLOR = 0x99000000;
    private static final int CIRCLE_INNER_INDICATOR_COLOR = 0xffcccccc;
    private static final int CIRCLE_INDICATOR_COLOR = Color.WHITE;
    private static final int LINE_INDICATOR_COLOR = Color.WHITE;

    private final Paint selfPaint = new Paint();
    private final Paint paintLine = new Paint();
    private final Paint paintCircle = new Paint();
    private final Paint paintCircleInner = new Paint();
    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Xfermode X_FER_MODE_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private final View target;
    private RectF targetRect;
    private final Rect selfRect = new Rect();

    private final float density;
    private float stopY;
    private boolean isTop;
    private boolean mIsShowing;
    private int yMessageView = 0;

    private float startYLineAndCircle;
    private float circleIndicatorSize = 0;
    private float circleIndicatorSizeFinal;
    private float circleInnerIndicatorSize = 0;
    private float lineIndicatorWidthSize;
    private int messageViewPadding;
    private float marginGuide;
    private float strokeCircleWidth;
    private float indicatorHeight;
    private int messageBoxColor = Color.WHITE;
    private int messageTitleColor = Color.BLACK;
    private int messageContentTextColor = Color.BLACK;
    private int circleInnerIndicatorColor = 0xffcccccc;
    private int circleIndicatorColor = Color.WHITE;
    private int lineIndicatorColor = Color.WHITE;

    private boolean isPerformedAnimationSize = false;

    private GuideListener mGuideListener;
    private Gravity mGravity;
    private DismissType dismissType;
    private PointerType pointerType;
    private final GuideMessageView mMessageView;
    private final TextView skipButton;
    private View lastTargetView;
    private boolean enableSkipButton = false;
    private static final String SKIP_BUTTON_TEXT = "SKIP";
    private static final int MARGIN_SIZE = 10;
    FrameLayout.LayoutParams skipParams;

    private GuideView(Context context, View view) {
        super(context);
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        this.target = view;
        density = context.getResources().getDisplayMetrics().density;
        init();

        if (view instanceof Targetable) {
            targetRect = ((Targetable) view).boundingRect();
        } else {
            int[] locationTarget = new int[2];
            target.getLocationOnScreen(locationTarget);
            targetRect = new RectF(
                locationTarget[0],
                locationTarget[1],
                locationTarget[0] + target.getWidth(),
                locationTarget[1] + target.getHeight()
            );
        }

        mMessageView = new GuideMessageView(getContext());
        skipButton = new TextView(context);
        skipButton.setText(SKIP_BUTTON_TEXT);
        skipButton.setTextColor(Color.WHITE);
        skipButton.setGravity(android.view.Gravity.CENTER);
        skipButton.setPadding(
                messageViewPadding,
                messageViewPadding,
                messageViewPadding,
                messageViewPadding
        );
        skipParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        ((LayoutParams)skipParams).setMargins(0,0,10,140);
        ((LayoutParams)skipParams).gravity = android.view.Gravity.RIGHT | android.view.Gravity.BOTTOM;

        mMessageView.setPadding(
            messageViewPadding,
            messageViewPadding,
            messageViewPadding,
            messageViewPadding
        );
        mMessageView.setColor(messageBoxColor);

        addView(
            mMessageView,
            new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        );
        skipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(enableSkipButton && lastTargetView != null)
                    dismiss(lastTargetView);
            }
        });
        setMessageLocation(resolveMessageViewLocation());

        ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                setMessageLocation(resolveMessageViewLocation());

                if (target instanceof Targetable) {
                    targetRect = ((Targetable) target).boundingRect();
                } else {
                    int[] locationTarget = new int[2];
                    target.getLocationInWindow(locationTarget);
                    targetRect = new RectF(
                        locationTarget[0],
                        locationTarget[1],
                        locationTarget[0] + target.getWidth(),
                        locationTarget[1] + target.getHeight()
                    );
                }

                selfRect.set(
                    getPaddingLeft(),
                    getPaddingTop(),
                    getWidth() - getPaddingRight(),
                    getHeight() - getPaddingBottom()
                );

                marginGuide = (int) (isTop ? marginGuide : -marginGuide);
                startYLineAndCircle = (isTop ? targetRect.bottom : targetRect.top) + marginGuide;
                stopY = yMessageView + indicatorHeight;
                startAnimationSize();
                getViewTreeObserver().addOnGlobalLayoutListener(this);
            }
        };
        getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }

    private void startAnimationSize() {
        if (!isPerformedAnimationSize) {
            final ValueAnimator circleSizeAnimator = ValueAnimator.ofFloat(
                0f,
                circleIndicatorSizeFinal
            );
            circleSizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    circleIndicatorSize = (float) circleSizeAnimator.getAnimatedValue();
                    circleInnerIndicatorSize = (float) circleSizeAnimator.getAnimatedValue() - density;
                    postInvalidate();
                }
            });

            final ValueAnimator linePositionAnimator = ValueAnimator.ofFloat(
                stopY,
                startYLineAndCircle
            );
            linePositionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    startYLineAndCircle = (float) linePositionAnimator.getAnimatedValue();
                    postInvalidate();
                }
            });

            linePositionAnimator.setDuration(SIZE_ANIMATION_DURATION);
            linePositionAnimator.start();
            linePositionAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    circleSizeAnimator.setDuration(SIZE_ANIMATION_DURATION);
                    circleSizeAnimator.start();
                    isPerformedAnimationSize = true;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }
    }

    private void init() {
        lineIndicatorWidthSize = LINE_INDICATOR_WIDTH_SIZE * density;
        marginGuide = MARGIN_INDICATOR * density;
        indicatorHeight = INDICATOR_HEIGHT * density;
        messageViewPadding = (int) (MESSAGE_VIEW_PADDING * density);
        strokeCircleWidth = STROKE_CIRCLE_INDICATOR_SIZE * density;
        circleIndicatorSizeFinal = CIRCLE_INDICATOR_SIZE * density;
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

    private int checkOrientation(){
        try{
            Display display = ((WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            int rotation = display.getRotation();
            return rotation;
        }
        catch (Exception e){
            return Surface.ROTATION_0;
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        mMessageView.setColor(messageBoxColor);
        mMessageView.setTitleColor(messageTitleColor);
        mMessageView.setContentTextColor(messageContentTextColor);
        if (target != null) {

            selfPaint.setColor(BACKGROUND_COLOR);
            selfPaint.setStyle(Paint.Style.FILL);
            selfPaint.setAntiAlias(true);
            canvas.drawRect(selfRect, selfPaint);

            paintLine.setStyle(Paint.Style.FILL);
            paintLine.setColor(lineIndicatorColor);
            paintLine.setStrokeWidth(lineIndicatorWidthSize);
            paintLine.setAntiAlias(true);

            paintCircle.setStyle(Paint.Style.STROKE);
            paintCircle.setColor(circleIndicatorColor);
            paintCircle.setStrokeCap(Paint.Cap.ROUND);
            paintCircle.setStrokeWidth(strokeCircleWidth);
            paintCircle.setAntiAlias(true);

            paintCircleInner.setStyle(Paint.Style.FILL);
            paintCircleInner.setColor(circleInnerIndicatorColor);
            paintCircleInner.setAntiAlias(true);

            final float x = (targetRect.left / 2 + targetRect.right / 2);

            switch (pointerType) {
                case circle:
                    canvas.drawLine(x,startYLineAndCircle,x,stopY,paintLine);
                    canvas.drawCircle(x, startYLineAndCircle, circleIndicatorSize, paintCircle);
                    canvas.drawCircle(x, startYLineAndCircle, circleInnerIndicatorSize, paintCircleInner);
                    break;
                case arrow:
                    canvas.drawLine(x,startYLineAndCircle,x,stopY,paintLine);
                    Path path = new Path();
                    if (isTop) {
                        path.moveTo(x, startYLineAndCircle - (circleIndicatorSize * 2));
                        path.lineTo(x + circleIndicatorSize, startYLineAndCircle);
                        path.lineTo(x - circleIndicatorSize, startYLineAndCircle);
                        path.close();
                    } else {
                        path.moveTo(x, startYLineAndCircle + (circleIndicatorSize * 2));
                        path.lineTo(x + circleIndicatorSize, startYLineAndCircle);
                        path.lineTo(x - circleIndicatorSize, startYLineAndCircle);
                        path.close();
                    }
                    canvas.drawPath(path, paintCircle);
                    break;
                case none:
                    //draw no line and no pointer
                    break;
            }
            targetPaint.setXfermode(X_FER_MODE_CLEAR);
            targetPaint.setAntiAlias(true);

            if (target instanceof Targetable) {
                canvas.drawPath(((Targetable) target).guidePath(), targetPaint);
            } else {
                canvas.drawRoundRect(
                    targetRect,
                    RADIUS_SIZE_TARGET_RECT,
                    RADIUS_SIZE_TARGET_RECT,
                    targetPaint
                );
            }
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void dismiss(View view) {
        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).removeView(this);
        mIsShowing = false;
        if (mGuideListener != null) {
            mGuideListener.onDismiss(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (dismissType) {

                case outside:
                    if (!isViewContains(mMessageView, x, y)) {
                        dismiss(target);
                    }
                    break;

                case anywhere:
                    dismiss(target);
                    break;

                case targetView:
                    if (targetRect.contains(x, y)) {
                        target.performClick();
                        dismiss(target);
                    }
                    break;

                case selfView:
                    if (isViewContains(mMessageView, x, y)) {
                        dismiss(target);
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

    private void setMessageLocation(Point p) {
        mMessageView.setX(p.x);
        mMessageView.setY(p.y);
        postInvalidate();
    }

    public void updateGuideViewLocation() {
        requestLayout();
    }

    private Point resolveMessageViewLocation() {

        int xMessageView;
        if (mGravity == Gravity.center) {
            xMessageView = (int) (targetRect.left - mMessageView.getWidth() / 2 + target.getWidth() / 2);
        } else {
            xMessageView = (int) (targetRect.right) - mMessageView.getWidth();
        }

        if (isLandscape()) {
            xMessageView -= getNavigationBarSize();
        }

        if (xMessageView + mMessageView.getWidth() > getWidth()) {
            xMessageView = getWidth() - mMessageView.getWidth();
        }
        if (xMessageView < 0) {
            xMessageView = 0;
        }

        //set message view bottom
        if ((targetRect.top + (indicatorHeight)) > getHeight() / 2f) {
            isTop = false;
            yMessageView = (int) (targetRect.top - mMessageView.getHeight() - indicatorHeight);
        }
        //set message view top
        else {
            isTop = true;
            yMessageView = (int) (targetRect.top + target.getHeight() + indicatorHeight);
        }

        if (yMessageView < 0) {
            yMessageView = 0;
        }

        return new Point(xMessageView, yMessageView);
    }

    public void show() {
        this.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        this.setClickable(false);

        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).addView(this);
        AlphaAnimation startAnimation = new AlphaAnimation(0.0f, 1.0f);
        startAnimation.setDuration(APPEARING_ANIMATION_DURATION);
        startAnimation.setFillAfter(true);
        this.startAnimation(startAnimation);
        mIsShowing = true;
        if(enableSkipButton) {

        switch(checkOrientation()){
            case Surface.ROTATION_0:
                ((LayoutParams)skipParams).setMargins(0,0,MARGIN_SIZE,getNavigationBarSize());
                ((LayoutParams)skipParams).gravity = android.view.Gravity.RIGHT | android.view.Gravity.BOTTOM;
                break;

            case Surface.ROTATION_90:
                ((LayoutParams)skipParams).setMargins(MARGIN_SIZE,0,0,0);
                ((LayoutParams)skipParams).gravity = android.view.Gravity.LEFT | android.view.Gravity.BOTTOM;
                break;

            case Surface.ROTATION_270:
                ((LayoutParams)skipParams).setMargins(0,0,MARGIN_SIZE,0);
                ((LayoutParams)skipParams).gravity = android.view.Gravity.RIGHT | android.view.Gravity.BOTTOM;
                break;
        }

            addView(skipButton, skipParams);
        }
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
        private PointerType pointerType;
        private final Context context;
        private Spannable contentSpan;
        private Typeface titleTypeFace, contentTypeFace;
        private GuideListener guideListener;
        private int titleTextSize;
        private int contentTextSize;
        private float lineIndicatorHeight;
        private float lineIndicatorWidthSize;
        private float circleIndicatorSize;
        private float circleInnerIndicatorSize;
        private float strokeCircleWidth;
        private int messageBoxColor;
        private int messageBoxAndLineAndPointerColor;
        private int lineAndPointerColor;
        private int pointerColor;
        private int lineColor;
        private boolean enableSkipButton;
        private View lastTargetView;
        private int messageTitleColor;
        private int messageContentTextColor;

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

        /**
         * changing line height indicator
         *
         * @param height you can change height indicator (Converting to Dp)
         */
        public Builder setIndicatorHeight(float height) {
            this.lineIndicatorHeight = height;
            return this;
        }

        /**
         * changing line width indicator
         *
         * @param width you can change width indicator
         */
        public Builder setIndicatorWidthSize(float width) {
            this.lineIndicatorWidthSize = width;
            return this;
        }

        /**
         * changing circle size indicator
         *
         * @param size you can change circle size indicator
         */
        public Builder setCircleIndicatorSize(float size) {
            this.circleIndicatorSize = size;
            return this;
        }

        /**
         * changing inner circle size indicator
         *
         * @param size you can change inner circle indicator size
         */
        public Builder setCircleInnerIndicatorSize(float size) {
            this.circleInnerIndicatorSize = size;
            return this;
        }

        /**
         * changing stroke circle size indicator
         *
         * @param size you can change stroke circle indicator size
         */
        public Builder setCircleStrokeIndicatorSize(float size) {
            this.strokeCircleWidth = size;
            return this;
        }

        /**
         * this method defining the type of pointer
         *
         * @param pointerType should be one type of PointerType enum. for example: arrow -> To show arrow pointing to target view
         */
        public Builder setPointerType(PointerType pointerType) {
            this.pointerType = pointerType;
            return this;
        }

        /**
         * the defined messageBoxColor overrides any defined messageBoxColor in the default or provided style
         *
         * @param messageBoxColor color of messageBox
         * @return builder
         */
        public Builder setMessageBoxColor(int messageBoxColor) {
            this.messageBoxColor = messageBoxColor;
            return this;
        }


        /**
         * the defined messageBoxAndLineAndPointerColor overrides any defined messageBoxAndLineAndPointerColor in the default or provided style
         *
         * @param messageBoxAndLineAndPointerColor color of messageBox
         * @return builder
         */
        public Builder setColorOfMessageBoxAndLineAndPointer(int messageBoxAndLineAndPointerColor) {
            this.messageBoxAndLineAndPointerColor = messageBoxAndLineAndPointerColor;
            return this;
        }
        /**
         * the defined LineAndPointerColor overrides any defined lineAndPointerColor in the default or provided style
         *
         * @param lineAndPointerColor color of messageBox
         * @return builder
         */
        public Builder setLineAndPointerColor(int lineAndPointerColor) {
            this.lineAndPointerColor = lineAndPointerColor;
            return this;
        }
        /**
         * the defined LineColor overrides any defined lineColor in the default or provided style
         *
         * @param lineColor color of messageBox
         * @return builder
         */
        public Builder setLineColor(int lineColor) {
            this.lineColor = lineColor;
            return this;
        }
        /**
         * the defined setPointerColor overrides any defined pointerColor in the default or provided style
         *
         * @param pointerColor color of messageBox
         * @return builder
         */
        public Builder setPointerColor(int pointerColor) {
            this.pointerColor = pointerColor;
            return this;
        }
        /**
         * the defined setMessageTitleColor overrides any defined messageTitleColor in the default or provided style
         *
         * @param messageTitleColor color of messageBox
         * @return builder
         */
        public Builder setMessageTitleColor(int messageTitleColor) {
            this.messageTitleColor = messageTitleColor;
            return this;
        }
        /**
         * the defined setMessageContentTextColor overrides any defined messageContentTextColor in the default or provided style
         *
         * @param messageContentTextColor color of messageBox
         * @return builder
         */
        public Builder setMessageContentTextColor(int messageContentTextColor) {
            this.messageContentTextColor = messageContentTextColor;
            return this;
        }
        /**
         * the defined enableSkipButton overrides any defined enableSkipButton in the default
         *
         * @param lastTargetView Last target view
         * @return builder
         */
        public Builder enableSkipButton(View lastTargetView) {
            this.enableSkipButton = true;
            this.lastTargetView = lastTargetView;
            return this;
        }

        public GuideView build() {
            GuideView guideView = new GuideView(context, targetView);
            guideView.mGravity = gravity != null ? gravity : Gravity.auto;
            guideView.dismissType = dismissType != null ? dismissType : DismissType.targetView;
            guideView.pointerType = pointerType != null ? pointerType : PointerType.circle;
            float density = context.getResources().getDisplayMetrics().density;
            guideView.enableSkipButton = enableSkipButton;
            guideView.setTitle(title);
            if (contentText != null) {
                guideView.setContentText(contentText);
            }
            if (titleTextSize != 0) {
                guideView.setTitleTextSize(titleTextSize);
            }
            if (contentTextSize != 0) {
                guideView.setContentTextSize(contentTextSize);
            }
            if (contentSpan != null) {
                guideView.setContentSpan(contentSpan);
            }
            if (titleTypeFace != null) {
                guideView.setTitleTypeFace(titleTypeFace);
            }
            if (contentTypeFace != null) {
                guideView.setContentTypeFace(contentTypeFace);
            }
            if (guideListener != null) {
                guideView.mGuideListener = guideListener;
            }
            if (lineIndicatorHeight != 0) {
                guideView.indicatorHeight = lineIndicatorHeight * density;
            }
            if (lineIndicatorWidthSize != 0) {
                guideView.lineIndicatorWidthSize = lineIndicatorWidthSize * density;
            }
            if (circleIndicatorSize != 0) {
                guideView.circleIndicatorSize = circleIndicatorSize * density;
            }
            if (circleInnerIndicatorSize != 0) {
                guideView.circleInnerIndicatorSize = circleInnerIndicatorSize * density;
            }
            if (strokeCircleWidth != 0) {
                guideView.strokeCircleWidth = strokeCircleWidth * density;
            }
            if (messageBoxColor != 0) {
                guideView.messageBoxColor = messageBoxColor;
            }
            if (messageBoxAndLineAndPointerColor != 0) {
                guideView.lineIndicatorColor = messageBoxAndLineAndPointerColor;
                guideView.circleIndicatorColor = messageBoxAndLineAndPointerColor;
                guideView.circleInnerIndicatorColor = messageBoxAndLineAndPointerColor;
                guideView.messageBoxColor = messageBoxAndLineAndPointerColor;
            }
            if (lineAndPointerColor != 0) {
                guideView.lineIndicatorColor = lineAndPointerColor;
                guideView.circleIndicatorColor = lineAndPointerColor;
                guideView.circleInnerIndicatorColor = lineAndPointerColor;
            }
            if (lineColor != 0) {
                guideView.lineIndicatorColor = lineColor;
            }
            if (pointerColor != 0) {
                guideView.circleIndicatorColor = pointerColor;
                guideView.circleInnerIndicatorColor = pointerColor;
            }
            if (messageTitleColor != 0) {
                guideView.messageTitleColor = messageTitleColor;
            }
            if (messageContentTextColor != 0) {
                guideView.messageContentTextColor = messageContentTextColor;
            }
            if (enableSkipButton) {
                guideView.lastTargetView = lastTargetView;
            }

            return guideView;
        }
    }
}

