package smartdevelop.ir.eram.showcaseviewlib;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.text.Spannable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */

public class GuideView extends FrameLayout {

    private static final int DEFAULT_RADIUS = 15;
    private static final int DEFAULT_BACKGROUND_COLOR = 0xdd000000;
    private static final float DEFAULT_INDICATOR_HEIGHT = 100;

    private final float density;
    private final View target;
    private final int radius;
    private final int backgroundColor;
    private final Bitmap indicatorDrawable;
    private float indicatorHeight;
    private RectF rect;
    private GuideMessageView mMessageView;
    private boolean isTop;
    private Gravity mGravity;
    private DismissType dismissType;
    int marginGuide;
    private boolean mIsShowing;
    private GuideListener mGuideListener;
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

    public interface GuideListener {
        void onDismiss(View view);
    }

    public enum Gravity {
        auto, center
    }

    public enum DismissType {
        outside, anywhere, targetView
    }

    private GuideView(Context context, View view, int radius, int backgroundColor) {
        super(context);
        setWillNotDraw(false);

        this.target = view;
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        // TODO Remove this manual input on image drawable
        this.indicatorDrawable = BitmapFactory.decodeResource(getResources(), R.drawable.pointer);
        indicatorHeight = indicatorDrawable != null ? indicatorDrawable.getHeight() : DEFAULT_INDICATOR_HEIGHT;

        density = context.getResources().getDisplayMetrics().density;

        int[] locationTarget = new int[2];
        target.getLocationOnScreen(locationTarget);
        rect = new RectF(locationTarget[0], locationTarget[1],
                locationTarget[0] + target.getWidth(),
                locationTarget[1] + target.getHeight());

        mMessageView = new GuideMessageView(getContext());
        final int padding = (int) (5 * density);
        mMessageView.setRadius(radius);
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
            Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(bitmap);

            // Paint background
            mPaint.setColor(backgroundColor);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);
            tempCanvas.drawRect(canvas.getClipBounds(), mPaint);

            // Paint pointer

            marginGuide = (int) (isTop ? 15 * density : -15 * density);
            final float startYTipOfIndicator = (isTop ? rect.bottom : rect.top) + marginGuide;
            final float x = (rect.left / 2 + rect.right / 2);

            if (indicatorDrawable != null) {
                // Draw Indicator using Drawable
                final int left = (int) x - indicatorDrawable.getWidth();
                final int top = isTop ? (int) startYTipOfIndicator : yMessageView + mMessageView.getHeight();
                final int right = (int) x + indicatorDrawable.getWidth();
                final int bottom = isTop ? yMessageView : (int) startYTipOfIndicator;
                Rect destRect = new Rect(left, top, right, bottom);

                tempCanvas.drawBitmap(indicatorDrawable, new Rect(0,0, indicatorDrawable.getWidth(), indicatorDrawable.getHeight()), destRect, null);
            } else {
                // Draw Indicator using default arrow
                float lineWidth = 3 * density;
                float strokeCircleWidth = 3 * density;
                float circleSize = 6 * density;
                float circleInnerSize = 5f * density;

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

                float stopY = (yMessageView + indicatorHeight * density);

                tempCanvas.drawLine(x, startYTipOfIndicator, x, stopY, paintLine);
                tempCanvas.drawCircle(x, startYTipOfIndicator, circleSize, paintCircle);
                tempCanvas.drawCircle(x, startYTipOfIndicator, circleInnerSize, paintCircleInner);
            }

            // Paint target
            targetPaint.setXfermode(XFERMODE_CLEAR);
            targetPaint.setAntiAlias(true);
            tempCanvas.drawRoundRect(rect, radius, radius, targetPaint);

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
        if (rect.top + (indicatorHeight * density) > getHeight() / 2) {
            isTop = false;
            yMessageView = (int) (rect.top - mMessageView.getHeight() - indicatorHeight * density);
        }
        //set message view top
        else {
            isTop = true;
            yMessageView = (int) (rect.top + target.getHeight() + indicatorHeight * density);
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

    public void setTitleTextColor(int color) {
        mMessageView.setTitleTextColor(color);
    }

    public void setContentText(String str) {
        mMessageView.setContentText(str);
    }

    public void setContentTextColor(int color) {
        mMessageView.setContentTextColor(color);
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

    public void setBorder(int color, float width) {
        mMessageView.setBorder(color, width);
    }


    public static class Builder {
        private Integer radius;
        private View targetView;
        private Integer backgroundColor;
        private String title, contentText;
        private Gravity gravity;
        private DismissType dismissType;
        private Context context;
        private int titleTextColor;
        private int titleTextSize;
        private int contentTextColor;
        private int contentTextSize;
        private Spannable contentSpan;
        private Typeface titleTypeFace, contentTypeFace;
        private GuideListener guideListener;
        private Integer borderColor;
        private Float borderWidth;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder setTargetView(View view) {
            this.targetView = view;
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
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

        public Builder setTitleTextColor(int color) {
            this.titleTextColor = color;
            return this;
        }

        public Builder setContentText(String contentText) {
            this.contentText = contentText;
            return this;
        }

        public Builder setContentTextColor(int color) {
            this.contentTextColor = color;
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

        public Builder setBorder(int color, float width) {
            this.borderColor = color;
            this.borderWidth = width;
            return this;
        }

        public GuideView build() {
            GuideView guideView = new GuideView(context, targetView,
                    radius != null ? radius : DEFAULT_RADIUS,
                    backgroundColor != null ? backgroundColor : DEFAULT_BACKGROUND_COLOR);
            guideView.mGravity = gravity != null ? gravity : Gravity.auto;
            guideView.dismissType = dismissType != null ? dismissType : DismissType.targetView;

            guideView.setTitle(title);
            if (contentText != null)
                guideView.setContentText(contentText);
            if (titleTextSize != 0)
                guideView.setTitleTextSize(titleTextSize);
            if (titleTextColor != 0)
                guideView.setTitleTextColor(titleTextColor);
            if (contentTextSize != 0)
                guideView.setContentTextSize(contentTextSize);
            if (contentTextColor != 0)
                guideView.setContentTextColor(contentTextColor);
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
            if (borderColor != null && borderWidth != null) {
                guideView.setBorder(borderColor, borderWidth);
            }

            return guideView;
        }
    }
}

