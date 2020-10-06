package ir.smartdevelop.eram.showcaseview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import smartdevelop.ir.eram.showcaseviewlib.Targetable;

public class CircleView extends View implements Targetable {

    private static final int DEFAULT_CIRCLE_COLOR = Color.RED;
    private int _circleColor = DEFAULT_CIRCLE_COLOR;
    private Paint _paint;
    private Path _guidePath = new Path();
    private RectF _boundingRect = new RectF();

    public CircleView(Context context)
    {
        super(context);
        init(context, null);
    }

    public CircleView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        _paint = new Paint();
        _paint.setAntiAlias(true);
    }

    public void setCircleColor(int circleColor)
    {
        this._circleColor = circleColor;
        invalidate();
    }

    public int getCircleColor()
    {
        return _circleColor;
    }

    private int usableWidth() {
        int w = getWidth();

        int pl = getPaddingLeft();
        int pr = getPaddingRight();

        return w - (pl + pr);
    }

    private int usableHeight() {
        int h = getHeight();

        int pt = getPaddingTop();
        int pb = getPaddingBottom();

        return h - (pt + pb);
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int pl = getPaddingLeft();
        int pt = getPaddingTop();

        int usableWidth = this.usableWidth();
        int usableHeight = this.usableHeight();

        int halfUsableWidth = usableWidth / 2;
        int halfUsableHeight = usableHeight / 2;

        int radius = Math.min(usableWidth, usableHeight) / 2;
        int cx = pl + halfUsableWidth;
        int cy = pt + halfUsableHeight;

        _paint.setColor(_circleColor);

        canvas.drawCircle(cx, cy, radius, _paint);

        int[] locationTarget = new int[2];
        getLocationOnScreen(locationTarget);
        int centerX = pl + halfUsableWidth + locationTarget[0];
        int centerY = pt + halfUsableHeight + locationTarget[1];
        _guidePath.reset();
        _guidePath.addCircle(centerX, centerY, radius, Path.Direction.CW);

        _boundingRect.left = locationTarget[0];
        _boundingRect.top = locationTarget[1];
        _boundingRect.right = locationTarget[0] + getWidth();
        _boundingRect.bottom = locationTarget[1] + getHeight();

    }

    @Override
    public Path guidePath() {
        return _guidePath;
    }

    @Override
    public RectF boundingRect() {
        return _boundingRect;
    }
}
