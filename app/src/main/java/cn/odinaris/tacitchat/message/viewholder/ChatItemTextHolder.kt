package cn.odinaris.tacitchat.message.viewholder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.view.BubbleTextView

import com.avos.avoscloud.im.v2.AVIMMessage
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage

/**
 * 聊天页面中的文本 item 对应的 holder
 */

class ChatItemTextHolder(context: Context, root: ViewGroup, isLeft: Boolean) : ChatItemHolder(context, root, isLeft) {

    private var contentView: TextView? = null

    override fun initView() {
        super.initView()
        if (isLeft) {
            conventLayout?.addView(View.inflate(context, R .layout.item_chat_left_text, null))
            contentView = itemView.findViewById(R.id.tv_chat_left_text) as BubbleTextView
        } else {
            conventLayout?.addView(View.inflate(context, R.layout.item_chat_right_text, null))
            contentView = itemView.findViewById(R.id.tv_chat_right_text) as BubbleTextView
        }
    }

    override fun bindData(t: Any?) {
        super.bindData(t)
        val message = t as AVIMMessage
        if (message is AVIMTextMessage) { contentView!!.text = message.text }
    }
}
