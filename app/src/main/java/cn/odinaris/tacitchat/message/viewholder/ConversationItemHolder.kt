package cn.odinaris.tacitchat.message.viewholder

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import cn.leancloud.chatkit.LCChatMessageInterface
import cn.leancloud.chatkit.cache.LCIMConversationItemCache
import cn.leancloud.chatkit.event.LCIMConversationItemLongClickEvent
import cn.leancloud.chatkit.utils.LCIMConstants
import cn.leancloud.chatkit.utils.LCIMConversationUtils
import cn.leancloud.chatkit.utils.LCIMLogUtils
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.message.ConversationActivity
import cn.odinaris.tacitchat.view.RoundImageView
import com.avos.avoscloud.AVCallback
import com.avos.avoscloud.AVException
import com.avos.avoscloud.im.v2.*
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage
import com.squareup.picasso.Picasso
import de.greenrobot.event.EventBus
import java.text.SimpleDateFormat
import java.util.*

class ConversationItemHolder(root: ViewGroup) : BaseViewHolder<AVIMConversation>(root.context, root, R.layout.item_conversation) {
    var avatarView: RoundImageView? = null
    var unreadView: TextView? = null
    var messageView: TextView? = null
    var timeView: TextView? = null
    var nameView: TextView? = null
    var avatarLayout: RelativeLayout? = null
    var contentLayout: LinearLayout? = null

    init { this.initView() }

    fun initView() {
        avatarView = this.itemView.findViewById(R.id.riv_avatar) as RoundImageView
        nameView = this.itemView.findViewById(R.id.tv_name) as TextView
        timeView = this.itemView.findViewById(R.id.tv_time) as TextView
        unreadView = this.itemView.findViewById(R.id.tv_unread) as TextView
        messageView = this.itemView.findViewById(R.id.tv_message) as TextView
        avatarLayout = this.itemView.findViewById(R.id.rl_avatar) as RelativeLayout
        contentLayout = this.itemView.findViewById(R.id.ll_content) as LinearLayout
    }

    override fun bindData(t: AVIMConversation) {
        this.reset()
        val conversation = t
        if (true) {
            if (null == conversation.createdAt) {
                conversation.fetchInfoInBackground(object : AVIMConversationCallback() {
                    override fun done(e: AVIMException?) {
                        if (e != null) {
                            LCIMLogUtils.logException(e)
                        } else {
                            this@ConversationItemHolder.updateName(conversation)
                            this@ConversationItemHolder.updateIcon(conversation)
                        }
                    }
                })
            }
            else {
                this.updateName(conversation)
                this.updateIcon(conversation)
            }

            this.updateUnreadCount(conversation)
            this.updateLastMessage(conversation.lastMessage)
            this.itemView.setOnClickListener { this@ConversationItemHolder.onConversationItemClick(conversation) }
            this.itemView.setOnLongClickListener {
                val builder = AlertDialog.Builder(this@ConversationItemHolder.context)
                builder.setItems(arrayOf("删除该聊天")) { _, _ ->
                    EventBus.getDefault().post(LCIMConversationItemLongClickEvent(conversation)) }
                val dialog = builder.create()
                dialog.show()
                false
            }
        }
    }

    private fun reset() {
        avatarView?.setImageResource(0)
        nameView?.text = ""
        timeView?.text = ""
        messageView?.text = ""
        unreadView?.visibility = View.GONE
    }

    private fun updateName(conversation: AVIMConversation) {
        LCIMConversationUtils.getConversationName(conversation, object : AVCallback<String>() {
            override fun internalDone0(s: String, e: AVException?) {
                if (null != e) {
                    LCIMLogUtils.logException(e)
                } else {
                    this@ConversationItemHolder.nameView?.text = s
                }

            }
        })
    }

    private fun updateIcon(conversation: AVIMConversation?) {
        if (null != conversation) {
            if (!conversation.isTransient && conversation.members.size <= 2) {
                LCIMConversationUtils.getConversationPeerIcon(conversation, object : AVCallback<String>() {
                    override fun internalDone0(s: String, e: AVException?) {
                        if (null != e) { LCIMLogUtils.logException(e) }
                        if (!TextUtils.isEmpty(s)) {
                            Picasso.with(this@ConversationItemHolder.context).load(s).placeholder(cn.leancloud.chatkit.R.drawable.lcim_default_avatar_icon).into(this@ConversationItemHolder.avatarView)
                        } else {
                            this@ConversationItemHolder.avatarView?.setImageResource(cn.leancloud.chatkit.R.drawable.lcim_default_avatar_icon)
                        }
                    }
                })
            } else { this.avatarView?.setImageResource(cn.leancloud.chatkit.R.drawable.lcim_group_icon) }
        }
    }

    private fun updateUnreadCount(conversation: AVIMConversation) {
        val num = LCIMConversationItemCache.getInstance().getUnreadCount(conversation.conversationId)
        this.unreadView?.text = num.toString()
        this.unreadView?.visibility = if (num > 0) View.VISIBLE else View.GONE
    }

    private fun updateLastMessage(message: AVIMMessage?) {
        if (null != message) {
            val date = Date(message.timestamp)
            val format = SimpleDateFormat("MM-dd HH:mm")
            this.timeView?.text = format.format(date)
            this.messageView?.text = getMessageShorthand(this.context, message)
        }

    }

    private fun onConversationItemClick(conversation: AVIMConversation) {
        try {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(LCIMConstants.CONVERSATION_ID, conversation.conversationId)
            this.context.startActivity(intent)
        } catch (var3: ActivityNotFoundException) {
            Log.i(LCIMConstants.LCIM_LOG_TAG, var3.toString())
        }
    }

    companion object {
        private fun getMessageShorthand(context: Context, message: AVIMMessage): CharSequence {
            if (message is AVIMTypedMessage) {
                val type = AVIMReservedMessageType.getAVIMReservedMessageType(message.messageType)
                when (type.ordinal) {
                    1 -> return (message as AVIMTextMessage).text
                    2 -> return context.getString(cn.leancloud.chatkit.R.string.lcim_message_shorthand_image)
                    3 -> return context.getString(cn.leancloud.chatkit.R.string.lcim_message_shorthand_location)
                    4 -> return context.getString(cn.leancloud.chatkit.R.string.lcim_message_shorthand_audio)
                    else -> {
                        var shortHand: CharSequence = ""
                        if (message is LCChatMessageInterface) { shortHand = message.shorthand }
                        if (TextUtils.isEmpty(shortHand)) {
                            shortHand = context.getString(cn.leancloud.chatkit.R.string.lcim_message_shorthand_unknown)
                        }
                        return shortHand
                    }
                }
            }
            else { return message.content }
        }
    }
}