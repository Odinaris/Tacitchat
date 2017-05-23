package cn.odinaris.tacitchat.message.viewholder

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.avos.avoscloud.im.v2.AVIMMessage
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage
import com.squareup.picasso.Picasso

import java.io.File

import cn.leancloud.chatkit.activity.LCIMImageActivity
import cn.leancloud.chatkit.R
import cn.leancloud.chatkit.utils.LCIMConstants

/**
 * 聊天页面中的图片 item 对应的 holder
 */
class ChatItemImageHolder(context: Context, root: ViewGroup, isLeft: Boolean) : ChatItemHolder(context, root, isLeft) {

    var contentView: ImageView? = null

    override fun initView() {
        super.initView()
        conventLayout?.addView(View.inflate(context, R.layout.lcim_chat_item_image_layout, null))
        contentView = itemView.findViewById(R.id.chat_item_image_view) as ImageView
        if (isLeft) {
            contentView?.setBackgroundResource(R.drawable.lcim_chat_item_left_bg)
        } else {
            contentView?.setBackgroundResource(R.drawable.lcim_chat_item_right_bg)
        }

        contentView?.setOnClickListener {
            try {
                val intent = Intent(context, LCIMImageActivity::class.java)
                intent.`package` = context.packageName
                intent.putExtra(LCIMConstants.IMAGE_LOCAL_PATH, (message as AVIMImageMessage).localFilePath)
                intent.putExtra(LCIMConstants.IMAGE_URL, (message as AVIMImageMessage).fileUrl)
                context.startActivity(intent)
            } catch (exception: ActivityNotFoundException) {
                Log.i(LCIMConstants.LCIM_LOG_TAG, exception.toString())
            }
        }
    }

    override fun bindData(t: Any?) {
        super.bindData(t)
        contentView?.setImageResource(0)
        val message = t as AVIMMessage
        if (message is AVIMImageMessage) {
            val imageMsg = message
            val localFilePath = imageMsg.localFilePath

            // 图片的真实高度与宽度
            val actualHight = imageMsg.height.toDouble()
            val actualWidth = imageMsg.width.toDouble()

            var viewHeight = MAX_DEFAULT_HEIGHT.toDouble()
            var viewWidth = MAX_DEFAULT_WIDTH.toDouble()

            if (0.0 != actualHight && 0.0 != actualWidth) {
                // 要保证图片的长宽比不变
                val ratio = actualHight / actualWidth
                if (ratio > viewHeight / viewWidth) {
                    viewHeight = if (actualHight > viewHeight) viewHeight else actualHight
                    viewWidth = viewHeight / ratio
                } else {
                    viewWidth = if (actualWidth > viewWidth) viewWidth else actualWidth
                    viewHeight = viewWidth * ratio
                }
            }

            contentView?.layoutParams?.height = viewHeight.toInt()
            contentView?.layoutParams?.width = viewWidth.toInt()

            if (!TextUtils.isEmpty(localFilePath)) {
                Picasso.with(context.applicationContext).load(File(localFilePath)).resize(viewWidth.toInt(), viewHeight.toInt()).centerCrop().into(contentView)
            } else if (!TextUtils.isEmpty(imageMsg.fileUrl)) {
                Picasso.with(context.applicationContext).load(imageMsg.fileUrl).resize(viewWidth.toInt(), viewHeight.toInt()).centerCrop().into(contentView)
            } else {
                contentView?.setImageResource(0)
            }
        }
    }

    companion object {
        private val MAX_DEFAULT_HEIGHT = 400
        private val MAX_DEFAULT_WIDTH = 300
    }
}