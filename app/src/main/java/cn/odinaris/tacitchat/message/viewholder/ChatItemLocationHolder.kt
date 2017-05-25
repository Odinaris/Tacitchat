package cn.odinaris.tacitchat.message.viewholder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.avos.avoscloud.im.v2.AVIMMessage
import com.avos.avoscloud.im.v2.messages.AVIMLocationMessage

import cn.leancloud.chatkit.R
import cn.leancloud.chatkit.event.LCIMLocationItemClickEvent
import de.greenrobot.event.EventBus

/**
 * 聊天页面中的地理位置 item 对应的 holder
 */
class ChatItemLocationHolder(context: Context, root: ViewGroup, isLeft: Boolean) : ChatItemHolder(context, root, isLeft) {

    var contentView: TextView? = null

    override fun initView() {
        super.initView()
        conventLayout?.addView(View.inflate(context, R.layout.lcim_chat_item_location, null))
        contentView = itemView.findViewById(R.id.locationView) as TextView
        conventLayout?.setBackgroundResource(if (isLeft) R.drawable.lcim_chat_item_left_bg else R.drawable.lcim_chat_item_right_bg)
        contentView!!.setOnClickListener {
            val event = LCIMLocationItemClickEvent()
            event.message = message
            EventBus.getDefault().post(event)
        }
    }

    override fun bindData(t: Any?) {
        super.bindData(t)
        val message = t as AVIMMessage
        if (message is AVIMLocationMessage) {
            contentView?.text = message.text
        }
    }
}
