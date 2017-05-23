package cn.odinaris.tacitchat.message.viewholder

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

import com.avos.avoscloud.AVCallback
import com.avos.avoscloud.AVException
import com.avos.avoscloud.im.v2.AVIMMessage
import com.squareup.picasso.Picasso

import java.text.SimpleDateFormat
import java.util.Date

import cn.leancloud.chatkit.LCChatKitUser
import cn.leancloud.chatkit.cache.LCIMProfileCache
import cn.leancloud.chatkit.event.LCIMMessageResendEvent
import cn.leancloud.chatkit.utils.LCIMConstants
import cn.leancloud.chatkit.utils.LCIMLogUtils
import de.greenrobot.event.EventBus

/**
 * 聊天 item 的 holder
 */
open class ChatItemHolder(context: Context, root: ViewGroup, protected var isLeft: Boolean) : BaseViewHolder<Any?>(context, root, if (isLeft) cn.leancloud.chatkit.R.layout.lcim_chat_item_left_layout else cn.leancloud.chatkit.R.layout.lcim_chat_item_right_layout) {
    protected var message: AVIMMessage? = null
    protected var avatarView: ImageView? = null
    protected var timeView: TextView? = null
    protected var nameView: TextView? = null
    protected var conventLayout: LinearLayout? = null
    protected var statusLayout: FrameLayout? = null
    protected var progressBar: ProgressBar? = null
    protected var statusView: TextView? = null
    protected var errorView: ImageView? = null

    init { this.initView() }

    open fun initView() {
        if (this.isLeft) {
            this.avatarView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_iv_avatar) as ImageView
            this.timeView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_tv_time) as TextView
            this.nameView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_tv_name) as TextView
            this.conventLayout = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_layout_content) as LinearLayout
            this.statusLayout = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_layout_status) as FrameLayout
            this.statusView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_tv_status) as TextView
            this.progressBar = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_progressbar) as ProgressBar
            this.errorView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_tv_error) as ImageView
        } else {
            this.avatarView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_iv_avatar) as ImageView
            this.timeView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_tv_time) as TextView
            this.nameView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_tv_name) as TextView
            this.conventLayout = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_layout_content) as LinearLayout
            this.statusLayout = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_layout_status) as FrameLayout
            this.progressBar = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_progressbar) as ProgressBar
            this.errorView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_tv_error) as ImageView
            this.statusView = this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_tv_status) as TextView
        }

        this.setAvatarClickEvent()
        this.setResendClickEvent()
    }

    override fun bindData(t: Any?) {
        this.message = t as AVIMMessage
        this.timeView?.text = millisecsToDateString(this.message!!.timestamp)
        this.nameView?.text = ""
        this.avatarView?.setImageResource(cn.leancloud.chatkit.R.drawable.lcim_default_avatar_icon)
        LCIMProfileCache.getInstance().getCachedUser(this.message!!.from, object : AVCallback<LCChatKitUser>() {
            override fun internalDone0(userProfile: LCChatKitUser?, e: AVException?) {
                if (null != e) {
                    LCIMLogUtils.logException(e)
                } else if (null != userProfile) {
                    this@ChatItemHolder.nameView?.text = userProfile.userName
                    val avatarUrl = userProfile.avatarUrl
                    if (!TextUtils.isEmpty(avatarUrl)) {
                        Picasso.with(this@ChatItemHolder.context).load(avatarUrl).placeholder(cn.leancloud.chatkit.R.drawable.lcim_default_avatar_icon).into(this@ChatItemHolder.avatarView)
                    }
                }

            }
        })
        when (this.message!!.messageStatus.ordinal) {
            1 -> {
                this.statusLayout?.visibility = View.VISIBLE
                this.progressBar?.visibility = View.GONE
                this.statusView?.visibility = View.GONE
                this.errorView?.visibility = View.VISIBLE
            }
            2 -> {
                this.statusLayout?.visibility = View.VISIBLE
                this.progressBar?.visibility = View.GONE
                this.statusView?.visibility = View.VISIBLE
                this.statusView?.visibility = View.GONE
                this.errorView?.visibility = View.GONE
            }
            3 -> {
                this.statusLayout?.visibility = View.VISIBLE
                this.progressBar?.visibility = View.VISIBLE
                this.statusView?.visibility = View.GONE
                this.errorView?.visibility = View.GONE
            }
            4, 5 -> this.statusLayout?.visibility = View.GONE
        }

    }

    fun showTimeView(isShow: Boolean) {
        this.timeView?.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    fun showUserName(isShow: Boolean) {
        this.nameView?.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    private fun setAvatarClickEvent() {
        this.avatarView?.setOnClickListener {
            try {
                val intent = Intent()
                intent.`package` = this@ChatItemHolder.context.packageName
                intent.action = LCIMConstants.AVATAR_CLICK_ACTION
                intent.addCategory("android.intent.category.DEFAULT")
                this@ChatItemHolder.context.startActivity(intent)
            } catch (var3: ActivityNotFoundException) {
                Log.i(LCIMConstants.LCIM_LOG_TAG, var3.toString())
            }
        }
    }

    private fun setResendClickEvent() {
        this.errorView?.setOnClickListener {
            val event = LCIMMessageResendEvent()
            event.message = this@ChatItemHolder.message
            EventBus.getDefault().post(event)
        }
    }

    private fun millisecsToDateString(timestamp: Long): String {
        val format = SimpleDateFormat("MM-dd HH:mm")
        return format.format(Date(timestamp))
    }
}
