package cn.odinaris.tacitchat.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue

import cn.odinaris.tacitchat.R

/**
 * Created by lgp on 2015/3/25.
 */
class BubbleImageView : android.support.v7.widget.AppCompatImageView {
    private var bubbleDrawable: BubbleDrawable? = null
    private var sourceDrawable: Drawable? = null
    private var mArrowWidth: Float = 0.toFloat()
    private var mAngle: Float = 0.toFloat()
    private var mArrowHeight: Float = 0.toFloat()
    private var mArrowPosition: Float = 0.toFloat()
    private var mBitmap: Bitmap? = null
    private var mArrowLocation: BubbleDrawable.ArrowLocation? = null
    private var mArrowCenter: Boolean = false

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.BubbleView)
            mArrowWidth = array.getDimension(R.styleable.BubbleView_arrowWidth,
                    BubbleDrawable.Builder.DEFAULT_ARROW_WITH)
            mArrowHeight = array.getDimension(R.styleable.BubbleView_arrowHeight,
                    BubbleDrawable.Builder.DEFAULT_ARROW_HEIGHT)
            mAngle = array.getDimension(R.styleable.BubbleView_angle,
                    BubbleDrawable.Builder.DEFAULT_ANGLE)
            mArrowPosition = array.getDimension(R.styleable.BubbleView_arrowPosition,
                    BubbleDrawable.Builder.DEFAULT_ARROW_POSITION)
            val location = array.getInt(R.styleable.BubbleView_arrowLocation, 0)
            mArrowLocation = BubbleDrawable.ArrowLocation.mapIntToValue(location)
            mArrowCenter = array.getBoolean(R.styleable.BubbleView_arrowCenter, false)
            array.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = measuredHeight
        if (width <= 0 && height > 0) {
            setMeasuredDimension(height, height)
        }
        if (height <= 0 && width > 0) {
            setMeasuredDimension(width, width)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            setUp(w, h)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setUp()
    }

    override fun onDraw(canvas: Canvas) {
        val saveCount = canvas.saveCount
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        if (bubbleDrawable != null)
            bubbleDrawable!!.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun setUp(left: Int, right: Int, top: Int, bottom: Int) {
        if (right <= left || bottom <= top)
            return

        val rectF = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        if (sourceDrawable != null)
            mBitmap = getBitmapFromDrawable(sourceDrawable!!)
        bubbleDrawable = BubbleDrawable.Builder()
                .rect(rectF)
                .arrowLocation(mArrowLocation)
                .angle(mAngle)
                .arrowHeight(mArrowHeight)
                .arrowWidth(mArrowWidth)
                .bubbleType(BubbleDrawable.BubbleType.BITMAP)
                .arrowPosition(mArrowPosition)
                .bubbleBitmap(mBitmap)
                .arrowCenter(mArrowCenter)
                .build()
    }

    private fun setUp(width: Int, height: Int) {
        setUp(paddingLeft, width - paddingRight,
                paddingTop, height - paddingBottom)
    }

    private fun setUp() {
        var width = width
        var height = height
        var scale: Int

        if (width > 0 && height <= 0 && sourceDrawable != null) {
            if (sourceDrawable!!.intrinsicWidth >= 0) {
                scale = width / sourceDrawable!!.intrinsicWidth
                height = scale * sourceDrawable!!.intrinsicHeight
            }
        }

        if (height > 0 && width <= 0 && sourceDrawable != null) {
            if (sourceDrawable!!.intrinsicHeight >= 0) {
                scale = height / sourceDrawable!!.intrinsicHeight
                width = scale * sourceDrawable!!.intrinsicWidth
            }
        }
        setUp(width, height)
    }

    override fun setImageBitmap(mBitmap: Bitmap?) {
        if (mBitmap == null)
            return
        this.mBitmap = mBitmap
        sourceDrawable = BitmapDrawable(resources, mBitmap)
        setUp()
        super.setImageDrawable(bubbleDrawable)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (drawable == null)
            return
        sourceDrawable = drawable
        setUp()
        super.setImageDrawable(bubbleDrawable)
    }

    override fun setImageResource(res: Int) {
        setImageDrawable(getDrawable(res))
    }

    private fun getDrawable(res: Int): Drawable {
//        if (res == 0) {
//            throw IllegalArgumentException("getDrawable res can not be zero")
//        }
        return context.resources.getDrawable(res)
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        return getBitmapFromDrawable(context, drawable, width, width, 25)!!
    }

    companion object {

        fun getBitmapFromDrawable(mContext: Context, drawable: Drawable?, width: Int, height: Int, defaultSize: Int): Bitmap? {
            if (drawable == null) {
                return null
            }
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            try {
                val bitmap: Bitmap
                if (width > 0 && height > 0) {
                    bitmap = Bitmap.createBitmap(width,
                            height, Bitmap.Config.ARGB_8888)
                } else {
                    bitmap = Bitmap.createBitmap(dp2px(mContext, defaultSize),
                            dp2px(mContext, defaultSize), Bitmap.Config.ARGB_8888)
                }
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                return bitmap
            } catch (e: OutOfMemoryError) {
                return null
            }

        }

        fun dp2px(context: Context, dp: Int): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                    context.resources.displayMetrics).toInt()
        }
    }
}
