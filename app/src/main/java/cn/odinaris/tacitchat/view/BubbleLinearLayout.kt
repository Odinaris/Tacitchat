package cn.odinaris.tacitchat.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.LinearLayout

import cn.odinaris.tacitchat.R

/**
 * Created by lgp on 2015/3/25.
 */
class BubbleLinearLayout : LinearLayout {
    private var bubbleDrawable: BubbleDrawable? = null
    private var mArrowWidth: Float = 0.toFloat()
    private var mAngle: Float = 0.toFloat()
    private var mArrowHeight: Float = 0.toFloat()
    private var mArrowPosition: Float = 0.toFloat()
    private var mArrowLocation: BubbleDrawable.ArrowLocation? = null
    private var bubbleColor: Int = 0
    private var mArrowCenter: Boolean = false

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
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
            bubbleColor = array.getColor(R.styleable.BubbleView_bubbleColor,
                    BubbleDrawable.Builder.DEFAULT_BUBBLE_COLOR)
            val location = array.getInt(R.styleable.BubbleView_arrowLocation, 0)
            mArrowLocation = BubbleDrawable.ArrowLocation.mapIntToValue(location)
            mArrowCenter = array.getBoolean(R.styleable.BubbleView_arrowCenter, false)
            array.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            setUp(w, h)
        }
    }

    private fun setUp(left: Int, right: Int, top: Int, bottom: Int) {
        if (right < left || bottom < top)
            return
        val rectF = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        bubbleDrawable = BubbleDrawable.Builder()
                .rect(rectF)
                .arrowLocation(mArrowLocation)
                .bubbleType(BubbleDrawable.BubbleType.COLOR)
                .angle(mAngle)
                .arrowHeight(mArrowHeight)
                .arrowWidth(mArrowWidth)
                .arrowPosition(mArrowPosition)
                .bubbleColor(bubbleColor)
                .arrowCenter(mArrowCenter)
                .build()
    }

    private fun setUp(width: Int, height: Int) {
        setUp(paddingLeft, width - paddingRight,
                paddingTop, height - paddingBottom)
        setBackgroundDrawable(bubbleDrawable)
    }

    fun setUpBubbleDrawable() {
        setBackgroundDrawable(null)
        post { setUp(width, height) }
    }

}
