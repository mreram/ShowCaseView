package smartdevelop.ir.eram.showcaseviewlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Spannable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mohammad Reza Eram  on 20/01/2018.
 */

@SuppressLint("ViewConstructor")
class GuideMessageView extends LinearLayout {

    private static final int RADIUS_SIZE = 5;
    private static final int PADDING_SIZE = 10;
    private static final int BOTTOM_PADDING_SIZE = 30;
    private static final int DEFAULT_TITLE_TEXT_SIZE = 18;
    private static final int DEFAULT_CONTENT_TEXT_SIZE = 14;

    private final Paint mPaint;
    private final RectF mRect;

    private final TextView mTitleTextView;
    private final TextView mContentTextView;
    ArrayList<View> view;
    int[] location = new int[2];

    GuideMessageView(Context context) {
        super(context);

        float density = context.getResources().getDisplayMetrics().density;
        setWillNotDraw(false);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        mRect = new RectF();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        final int padding = (int) (PADDING_SIZE * density);
        final int paddingBottom = (int) (BOTTOM_PADDING_SIZE * density);

        mTitleTextView = new TextView(context);
        mTitleTextView.setPadding(padding, padding, padding, paddingBottom);
        mTitleTextView.setGravity(Gravity.LEFT);
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TITLE_TEXT_SIZE);
        mTitleTextView.setTextColor(Color.WHITE);
        addView(
                mTitleTextView,
                new LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        view = new ArrayList<>();
        mContentTextView = new TextView(context);
        mContentTextView.setTextColor(Color.WHITE);
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CONTENT_TEXT_SIZE);
        mContentTextView.setPadding(padding, paddingBottom, padding, padding);
        mContentTextView.setGravity(Gravity.LEFT);
        addView(
                mContentTextView,
                new LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
        view.add(mContentTextView);
    }

    public void setTitle(String title) {
        if (title == null) {
            removeView(mTitleTextView);
            return;
        }
        mTitleTextView.setText(title);
    }

    public void setContentText(String content) {
        mContentTextView.setText(content);
    }

    public void setContentSpan(Spannable content) {
        mContentTextView.setText(content);
    }

    public void setContentTypeFace(Typeface typeFace) {
        mContentTextView.setTypeface(typeFace);
    }

    public void setTitleTypeFace(Typeface typeFace) {
        mTitleTextView.setTypeface(typeFace);
    }

    public void setTitleTextSize(int size) {
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setContentTextSize(int size) {
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.getLocationOnScreen(location);

        mRect.set(
                getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom()
        );

        final int density = (int) getResources().getDisplayMetrics().density;
        final int radiusSize = RADIUS_SIZE * density;
        canvas.drawRoundRect(mRect, radiusSize, radiusSize, mPaint);
    }
}
