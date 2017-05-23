package cn.odinaris.tacitchat.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView

import java.util.ArrayList

import cn.odinaris.tacitchat.event.MemberLetterEvent
import de.greenrobot.event.EventBus


/**
 * 联系人列表，快速滑动字母导航 View
 * 此处仅在滑动或点击时发送 MemberLetterEvent，接收放自己处理相关逻辑
 * 注意：因为长按事件等触发，有可能重复发送
 */
class LetterView : LinearLayout {

    constructor(context: Context) : super(context) {
        orientation = LinearLayout.VERTICAL
        updateLetters()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        orientation = LinearLayout.VERTICAL
        updateLetters()
    }

    private fun updateLetters() {
        setLetters(sortLetters)
    }

    /**
     * 设置快速滑动的字母集合
     */
    fun setLetters(letters: List<Char>) {
        removeAllViews()
        for (content in letters) {
            val view = TextView(context)
            view.text = content.toString()
            addView(view)
        }

        setOnTouchListener { v, event ->
            val x = Math.round(event.x)
            val y = Math.round(event.y)
            for (i in 0..childCount - 1) {
                val child = getChildAt(i) as TextView
                if (y > child.top && y < child.bottom) {
                    val letterEvent = MemberLetterEvent()
                    letterEvent.letter = child.text.toString()[0]
                    EventBus.getDefault().post(letterEvent)
                }
            }
            true
        }
    }

    /**
     * 默认的只包含 A-Z 的字母
     */
    private val sortLetters: List<Char>
        get() {
            val letterList = ArrayList<Char>()
            var c = 'A'
            while (c <= 'Z') {
                letterList.add(c)
                c++
            }
            return letterList
        }
}
