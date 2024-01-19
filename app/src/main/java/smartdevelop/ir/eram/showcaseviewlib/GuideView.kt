package smartdevelop.ir.eram.showcaseviewlib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Xfermode
import android.text.Spannable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import org.northshore.showcaselib.databinding.LayoutGuideMessageViewBinding
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 * The view responsible for showing the Guide message and highlighting the targeted view.
 */
@SuppressLint("ViewConstructor")
class GuideView(context: Context, private var targetView: View?, var model: GuideViewModel) :
    FrameLayout(context) {
    private val selfPaint = Paint()
    private val paintArrow = Paint()
    private val paintArrowInner = Paint()
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val targetBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val xFerModeClear: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val arrowPath = Path()
    private var targetRect: RectF? = null
    private val backgroundRect = Rect()
    private var density: Float = 0.0f
    private var isTop = false
    private var yPointer = 0f
    private var pointerSize = 0f
    private var strokeWidth = 0f
    lateinit var mGuideListener: GuideListener
    private lateinit var binding: LayoutGuideMessageViewBinding
    private var scaleFactor: Float = 2f

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        initValues()
        bind()

        val lp = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            android.view.Gravity.CENTER_HORIZONTAL
        )
        addView(binding.root, lp)

        addGlobalLayoutListener()
    }

    /**
     * Init static values of the view
     */
    private fun initValues() {
        val dm = context.resources.displayMetrics
        density = dm.density
        scaleFactor = if (context.resources.configuration.fontScale > 1f) 4f else 2f
        strokeWidth = STROKE_CIRCLE_INDICATOR_SIZE * density
        pointerSize = CIRCLE_INDICATOR_SIZE * density

        selfPaint.color = BACKGROUND_COLOR
        selfPaint.style = Paint.Style.FILL
        selfPaint.isAntiAlias = true

        paintArrow.style = Paint.Style.FILL
        paintArrow.color = CIRCLE_INDICATOR_COLOR
        paintArrow.strokeCap = Paint.Cap.ROUND
        paintArrow.strokeWidth = strokeWidth
        paintArrow.isAntiAlias = true
        paintArrowInner.style = Paint.Style.FILL
        paintArrowInner.color = CIRCLE_INNER_INDICATOR_COLOR
        paintArrowInner.isAntiAlias = true

        targetPaint.xfermode = xFerModeClear
        targetPaint.isAntiAlias = true
        targetBorderPaint.style = Paint.Style.STROKE
        targetBorderPaint.strokeWidth = strokeWidth
        targetBorderPaint.color = Color.WHITE
    }

    /**
     * Init binding of the view
     */
    private fun bind() {
        binding = LayoutGuideMessageViewBinding.inflate(LayoutInflater.from(context))
        binding.next.setOnClickListener { v: View ->
            dismiss()
            mGuideListener.onNext(v)
        }
        binding.back.setOnClickListener { v: View ->
            dismiss()
            mGuideListener.onBack(v)
        }
        binding.backFromFinal.setOnClickListener { v: View ->
            dismiss()
            mGuideListener.onBack(v)
        }
        binding.exit.setOnClickListener {
            dismiss()
            mGuideListener.onTourFished()
        }
        binding.close.setOnClickListener { v: View ->
            dismiss()
            if (model.isFinalScreen) {
                mGuideListener.onTourFished()
            } else {
                mGuideListener.onExit(v)
            }
        }
        binding.guidedTourClose.setOnClickListener { v: View ->
            dismiss()
            mGuideListener.onExit(v)
        }
        binding.guideTourExternalLink.setOnClickListener {
            dismiss()
            mGuideListener.onExternalLink()
        }
    }

    /**
     * Add LayoutListener to the view.
     */
    private fun addGlobalLayoutListener() {
        val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            if (targetView != null) {
                if (targetView is Targetable) {
                    targetRect = (targetView as Targetable).boundingRect()
                } else {
                    val locationTarget = IntArray(2)
                    targetView!!.getLocationOnScreen(locationTarget)
                    targetRect = RectF(
                        locationTarget[0] - TARGET_PADDING * density,
                        locationTarget[1] - TARGET_PADDING * density,
                        locationTarget[0] + targetView!!.width + TARGET_PADDING * density,
                        locationTarget[1] + targetView!!.height + TARGET_PADDING * density
                    )
                    if (isLandscape) {
                        targetRect?.offset(-statusBarHeight.toFloat(), 0f)
                    }
                }
                isTop = targetRect!!.top + pointerSize <= rootView.height / scaleFactor
                yPointer = if (isTop) targetRect!!.bottom else targetRect!!.top
            }
            setMessageLocation(resolveMessageViewLocation())
            backgroundRect[paddingLeft, paddingTop, width - paddingRight] = height - paddingBottom
        }
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    private val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    private val isLandscape: Boolean
        get() {
            val displayMode = resources.configuration.orientation
            return displayMode != Configuration.ORIENTATION_PORTRAIT
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(backgroundRect, selfPaint)
        if (targetView != null) {
            val x = targetRect!!.left / 2 + targetRect!!.right / 2
            arrowPath.moveTo(x, yPointer)
            if (isTop) {
                arrowPath.lineTo(x + pointerSize, yPointer + pointerSize * 2)
                arrowPath.lineTo(x - pointerSize, yPointer + pointerSize * 2)
            } else {
                arrowPath.lineTo(x + pointerSize, yPointer - pointerSize * 2)
                arrowPath.lineTo(x - pointerSize, yPointer - pointerSize * 2)
            }
            arrowPath.close()
            canvas.drawPath(arrowPath, paintArrow)
            if (targetView is Targetable) {
                (targetView as Targetable).guidePath()?.let { canvas.drawPath(it, targetPaint) }
            } else {
                canvas.drawRoundRect(
                    targetRect!!,
                    RADIUS_SIZE_TARGET_RECT.toFloat(),
                    RADIUS_SIZE_TARGET_RECT.toFloat(),
                    targetPaint
                )
                canvas.drawRect(targetRect!!, targetBorderPaint)
            }
        }
    }

    /**
     * Override the onClick event when the user clicks outside of the message view to prevent
     * misbehaving of the app, or unintentionally opening something else.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    /**
     * Removes the view from the window.
     */
    private fun dismiss() {
        ((context as Activity).window.decorView as ViewGroup).removeView(this)
    }

    /**
     * Checks if the given @param[rx] and @param[ry] are within the area of @param[view]
     * @param view the target view
     * @param rx point in the X axis
     * @param ry point in the Y axis
     * @return true if the point (rx, ry) is within the bounds of @param[view], false otherwise
     */
    private fun isViewContains(view: View, rx: Float, ry: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        val w = view.width
        val h = view.height
        return !(rx < x || rx > x + w || ry < y || ry > y + h)
    }

    /**
     * Set the location of the message view in the screen
     * @param p the @see[Point] where the top-left corner of the message view should be.
     */
    private fun setMessageLocation(p: Point) {
        binding.root.x = p.x.toFloat()
        binding.root.y = p.y.toFloat()
        postInvalidate()
    }

    /**
     * Checks for the message view location in the screen, used to determine if the message view
     * should be on top or below the targetted view.
     * @return the point where the message view should be.
     */
    private fun resolveMessageViewLocation(): Point {
        var xMessageView: Int
        var yMessageView: Int
        if (targetView == null) {
            xMessageView = (resources.displayMetrics.widthPixels - binding.root.width) / 2
            yMessageView = APPEARING_ANIMATION_DURATION
            return Point(xMessageView, yMessageView)
        }
        xMessageView = when (model.gravity){
             Gravity.START -> {
                 EDGE_PADDING
             }
            Gravity.END -> {
               resources.displayMetrics.widthPixels - EDGE_PADDING - binding.root.width
            }
            else -> {
                (targetRect!!.left - binding.root.width / 2 + targetView!!.width / 2).toInt()
            }
        }
        if (xMessageView + binding.root.width > width) {
            xMessageView = width - binding.root.width
        }
        if (xMessageView < 0) {
            xMessageView = 0
        }

        //set message view bottom
        if (targetRect!!.top + pointerSize > rootView.height / scaleFactor) {
            isTop = false
            yMessageView = (targetRect!!.top - binding.root.height - pointerSize * 2).toInt()
        } else {
            isTop = true
            yMessageView = (targetRect!!.top + targetView!!.height + pointerSize * 2).toInt()
        }
        if (yMessageView < 0) {
            yMessageView = 0
        }
        return Point(xMessageView, yMessageView)
    }

    /**
     * Displays the message view.
     */
    fun show() {
        this.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.isClickable = false
        ((context as Activity).window.decorView as ViewGroup).addView(this)
        val startAnimation = AlphaAnimation(0.0f, 1.0f)
        startAnimation.duration = APPEARING_ANIMATION_DURATION.toLong()
        startAnimation.fillAfter = true
        startAnimation(startAnimation)

        binding.model = model
    }

    companion object {
        private const val APPEARING_ANIMATION_DURATION = 400
        private const val TARGET_PADDING = 4
        private const val CIRCLE_INDICATOR_SIZE = 12
        private const val STROKE_CIRCLE_INDICATOR_SIZE = 1
        private const val RADIUS_SIZE_TARGET_RECT = 20
        private const val BACKGROUND_COLOR = -0x67000000
        private const val CIRCLE_INNER_INDICATOR_COLOR = -0x333334
        private const val CIRCLE_INDICATOR_COLOR = Color.WHITE
        private const val EDGE_PADDING = 30
    }
}
