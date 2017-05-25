package cn.odinaris.tacitchat.message.viewholder

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage

import cn.leancloud.chatkit.R
import cn.leancloud.chatkit.cache.LCIMLocalCacheUtils
import cn.leancloud.chatkit.utils.LCIMPathUtils
import cn.leancloud.chatkit.view.LCIMPlayButton

/**
 * Created by wli on 15/9/17.
 * 聊天页面中的语音 item 对应的 holder
 */
class ChatItemAudioHolder(context: Context, root: ViewGroup, isLeft: Boolean) : ChatItemHolder(context, root, isLeft) {

    var playButton: LCIMPlayButton? = null
    var durationView: TextView? = null

    override fun initView() {
        super.initView()
        if (isLeft) {
            conventLayout?.addView(View.inflate(context, R.layout.lcim_chat_item_left_audio_layout, null))
        } else {
            conventLayout?.addView(View.inflate(context, R.layout.lcim_chat_item_right_audio_layout, null))
        }
        playButton = itemView.findViewById(R.id.chat_item_audio_play_btn) as LCIMPlayButton
        durationView = itemView.findViewById(R.id.chat_item_audio_duration_view) as TextView

        if (itemMaxWidth <= 0) {
            itemMaxWidth = itemView.resources.displayMetrics.widthPixels * 0.6
        }
    }

    override fun bindData(t: Any?) {
        super.bindData(t)
        if (t is AVIMAudioMessage) {
            val audioMessage = t
            durationView?.text = String.format("%.0f\"", audioMessage.duration)
            val duration = audioMessage.duration
            val width = getWidthInPixels(duration)
            if (width > 0) {
                playButton?.width = width
            }

            val localFilePath = audioMessage.localFilePath
            if (!TextUtils.isEmpty(localFilePath)) {
                playButton?.setPath(localFilePath)
            } else {
                val path = LCIMPathUtils.getAudioCachePath(context, audioMessage.messageId)
                playButton?.setPath(path)
                LCIMLocalCacheUtils.downloadFileAsync(audioMessage.fileUrl, path)
            }
        }
    }

    private fun getWidthInPixels(duration: Double): Int {
        if (itemMaxWidth <= 0) {
            return 0
        }
        val unitWidth = itemMaxWidth / 100
        if (duration < 2) {
            return itemMinWidth
        } else if (duration < 10) {
            return itemMinWidth + (unitWidth * 5.0 * duration).toInt()
        } else {
            return itemMinWidth + (unitWidth * 50).toInt() + (unitWidth * (duration - 10)).toInt()
        }
    }

    companion object {

        private var itemMaxWidth = 0.0
        private val itemMinWidth = 200
    }
}