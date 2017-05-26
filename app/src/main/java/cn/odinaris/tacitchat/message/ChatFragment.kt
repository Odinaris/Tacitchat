package cn.odinaris.tacitchat.message

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.avos.avoscloud.im.v2.AVIMConversation
import com.avos.avoscloud.im.v2.AVIMException
import com.avos.avoscloud.im.v2.AVIMMessage
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage

import java.io.File
import java.io.IOException

import cn.leancloud.chatkit.event.LCIMIMTypeMessageEvent
import cn.leancloud.chatkit.event.LCIMMessageResendEvent
import cn.leancloud.chatkit.utils.LCIMAudioHelper
import cn.leancloud.chatkit.utils.LCIMLogUtils
import cn.leancloud.chatkit.utils.LCIMNotificationUtils
import cn.leancloud.chatkit.utils.LCIMPathUtils
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.event.TacitInputBarEvent
import cn.odinaris.tacitchat.event.TacitInputBarRecordEvent
import cn.odinaris.tacitchat.event.TacitInputBarTextEvent
import cn.odinaris.tacitchat.utils.PathUtils
import cn.odinaris.tacitchat.view.TacitInputBar
import de.greenrobot.event.EventBus

class ChatFragment : Fragment() {
    var imConversation: AVIMConversation? = null
    var itemAdapter: ChatListAdapter? = null
    var recyclerView: RecyclerView? = null
    var layoutManager: LinearLayoutManager? = null
    var inputBar: TacitInputBar? = null
    var refreshLayout: SwipeRefreshLayout? = null
    var localCameraPath: String? = null
    val RESULT_PICK_GALLERY = 1     //选择相册图片
    val RESULT_PICK_CAMERA = 2      //打开相机进行拍照
    val RESULT_PICK_IMAGE = 3       //选择本地图片(相册/拍照)
    val RESULT_EMBED_IMAGE = 4      //嵌入信息

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.frg_chat, container, false)
        this.recyclerView = view.findViewById(R.id.rv_chat) as RecyclerView
        this.refreshLayout = view.findViewById(R.id.srl_chat) as SwipeRefreshLayout
        this.refreshLayout?.isEnabled = false
        this.inputBar = view.findViewById(R.id.tib_input_bar) as TacitInputBar
        this.layoutManager = LinearLayoutManager(this.activity)
        this.recyclerView?.layoutManager = this.layoutManager
        this.itemAdapter = this.adapter
        this.itemAdapter?.resetRecycledViewPoolSize(this.recyclerView)
        this.recyclerView?.adapter = this.itemAdapter
        EventBus.getDefault().register(this)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        this.refreshLayout?.setOnRefreshListener {
            val message = this@ChatFragment.itemAdapter?.firstMessage
            if (null == message) {
                this@ChatFragment.refreshLayout?.isRefreshing = false
            } else {
                this@ChatFragment.imConversation!!.queryMessages(message.messageId, message.timestamp, 20, object : AVIMMessagesQueryCallback() {
                    override fun done(list: List<AVIMMessage>?, e: AVIMException?) {
                        this@ChatFragment.refreshLayout?.isRefreshing = false
                        if (this@ChatFragment.filterException(e) && null != list && list.isNotEmpty()) {
                            this@ChatFragment.itemAdapter?.addMessageList(list)
                            this@ChatFragment.itemAdapter?.notifyDataSetChanged()
                            this@ChatFragment.layoutManager?.scrollToPositionWithOffset(list.size - 1, 0)
                        }
                    }
                })
            }
        }
    }
    private val adapter: ChatListAdapter
        get() = ChatListAdapter()

    override fun onResume() {
        super.onResume()
        if (null != this.imConversation) { LCIMNotificationUtils.addTag(this.imConversation!!.conversationId) }
    }

    override fun onPause() {
        super.onPause()
        LCIMAudioHelper.getInstance().stopPlayer()
        if (null != this.imConversation) { LCIMNotificationUtils.removeTag(this.imConversation!!.conversationId) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    fun setConversation(conversation: AVIMConversation) {
        this.imConversation = conversation
        this.refreshLayout?.isEnabled = true
        this.inputBar?.tag = this.imConversation!!.conversationId
        //this.inputBottomBar?.tag = this.imConversation!!.conversationId
        this.fetchMessages()
        LCIMNotificationUtils.addTag(conversation.conversationId)
        if (!conversation.isTransient) {
            if (conversation.members.size == 0) {
                conversation.fetchInfoInBackground(object : AVIMConversationCallback() {
                    override fun done(e: AVIMException?) {
                        if (null != e) { LCIMLogUtils.logException(e) }
                        this@ChatFragment.itemAdapter?.showUserName(conversation.members.size > 2)
                    }
                })
            }
            else { this.itemAdapter?.showUserName(conversation.members.size > 2) }
        }
        else { this.itemAdapter?.showUserName(true) }
    }

    private fun fetchMessages() {
        this.imConversation!!.queryMessages(object : AVIMMessagesQueryCallback() {
            override fun done(messageList: List<AVIMMessage>, e: AVIMException?) {
                if (this@ChatFragment.filterException(e)) {
                    this@ChatFragment.itemAdapter?.setMessageList(messageList)
                    this@ChatFragment.recyclerView?.adapter = this@ChatFragment.itemAdapter
                    this@ChatFragment.itemAdapter?.notifyDataSetChanged()
                    this@ChatFragment.scrollToBottom()
                }
            }
        })
    }

    //发送文字事件
    fun onEvent(textEvent: TacitInputBarTextEvent?) {
        if (null != this.imConversation && null != textEvent && !TextUtils.isEmpty(textEvent.sendContent) && this.imConversation!!.conversationId == textEvent.tag) {
            //Todo 加密信息
            this.sendText(textEvent.sendContent)
        }
    }

    //聊天内容列表更新
    fun onEvent(messageEvent: LCIMIMTypeMessageEvent?) {
        if (null != this.imConversation && null != messageEvent && this.imConversation!!.conversationId == messageEvent.conversation.conversationId) {
            this.itemAdapter?.addMessage(messageEvent.message)
            this.itemAdapter?.notifyDataSetChanged()
            this.scrollToBottom()
        }
    }

    //重新发送聊天信息
    fun onEvent(resendEvent: LCIMMessageResendEvent?) {
        if (null != this.imConversation && null != resendEvent && null != resendEvent.message && this.imConversation!!.conversationId == resendEvent.message.conversationId && AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed == resendEvent.message.messageStatus && this.imConversation!!.conversationId == resendEvent.message.conversationId) {
            this.sendMessage(resendEvent.message, false)
        }
    }

    //处理非文字、语音之外的对象
    fun onEvent(event: TacitInputBarEvent?) {
        if (null != this.imConversation && null != event && this.imConversation!!.conversationId == event.tag) {
            when (event.eventAction) {
                0 -> this.dispatchPickPictureIntent()
                1 -> this.dispatchTakePictureIntent()
                4 -> this.sendFile()                    //发送文件
                5 -> this.fireMessage()                 //阅后即焚
                6 -> this.dispatchGetPictureIntent()    //选择图片进行嵌入
            }
        }
    }

    //发送语音
    fun onEvent(recordEvent: TacitInputBarRecordEvent?) {
        if (null != this.imConversation && null != recordEvent && !TextUtils.isEmpty(recordEvent.audioPath) && this.imConversation!!.conversationId == recordEvent.tag) {
            //Todo 加密语音信息
            toast("发送语音!")
            //this.sendAudio(recordEvent.audioPath)
        }
    }

    //选择本地图片
    private fun dispatchGetPictureIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RESULT_PICK_IMAGE)
    }

    private fun sendFile() { toast("发送文件!") }

    private fun fireMessage() { toast("阅后即焚!") }

    //跳转拍照
    private fun dispatchTakePictureIntent() {
        this.localCameraPath = LCIMPathUtils.getPicturePathByCurrentTime(this.context)
        val takePictureIntent = Intent("android.media.action.IMAGE_CAPTURE")
        val imageUri = Uri.fromFile(File(this.localCameraPath))
        takePictureIntent.putExtra("return-data", false)
        takePictureIntent.putExtra("output", imageUri)
        if (takePictureIntent.resolveActivity(this.activity.packageManager) != null) {
            this.startActivityForResult(takePictureIntent, RESULT_PICK_CAMERA)
        }
    }

    //跳转相册
    private fun dispatchPickPictureIntent() {
        val photoPickerIntent = Intent("android.intent.action.PICK", null as Uri?)
        photoPickerIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        this.startActivityForResult(photoPickerIntent, RESULT_PICK_GALLERY)
    }

    private fun scrollToBottom() {
        this.layoutManager?.scrollToPositionWithOffset(this.itemAdapter?.itemCount!! - 1, 0)
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        if (contentUri.scheme == "file") { return contentUri.encodedPath }
        else {
            var cursor: Cursor? = null
            val var5: String
            try {
                val proj = arrayOf("_data")
                cursor = context.contentResolver.query(contentUri, proj, null as String?, null as Array<String>?, null as String?)
                if (null != cursor) {
                    val column_index = cursor.getColumnIndexOrThrow("_data")
                    cursor.moveToFirst()
                    val var6 = cursor.getString(column_index)
                    return var6
                }
                var5 = ""
            } finally {
                if (cursor != null) { cursor.close() }
            }
            return var5
        }
    }

    private fun sendText(content: String) {
        val message = AVIMTextMessage()
        message.text = content
        this.sendMessage(message)
    }

    private fun sendImage(imagePath: String?) {
        try {
            //Todo 输入选择的图片Url，生成加密后的图片Url并发送
            this.sendMessage(AVIMImageMessage(imagePath!!))
        } catch (var3: IOException) {
            LCIMLogUtils.logException(var3)
        }

    }

    private fun sendAudio(audioPath: String) {
        try {
            val audioMessage = AVIMAudioMessage(audioPath)
            this.sendMessage(audioMessage)
        } catch (var3: IOException) {
            LCIMLogUtils.logException(var3)
        }
    }

    fun sendMessage(message: AVIMMessage) { this.sendMessage(message, true) }

    fun sendMessage(message: AVIMMessage, addToList: Boolean) {
        if (addToList) { this.itemAdapter?.addMessage(message) }
        this.itemAdapter?.notifyDataSetChanged()
        this.scrollToBottom()
        this.imConversation!!.sendMessage(message, object : AVIMConversationCallback() {
            override fun done(e: AVIMException?) {
                this@ChatFragment.itemAdapter?.notifyDataSetChanged()
                if (null != e) { LCIMLogUtils.logException(e) }
            }
        })
    }

    private fun filterException(e: Exception?): Boolean {
        if (null != e) {
            LCIMLogUtils.logException(e)
            Toast.makeText(this.context, e.message, Toast.LENGTH_SHORT).show()
        }
        return null == e
    }

    private fun toast(content : String){ Toast.makeText(context,content,Toast.LENGTH_SHORT).show() }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (-1 == resultCode) {
            if(data != null){
                when (requestCode) {
                    RESULT_PICK_GALLERY -> this.sendImage(this.getRealPathFromURI(this.activity, data.data))
                    RESULT_PICK_CAMERA -> this.sendImage(this.localCameraPath)
                    RESULT_PICK_IMAGE ->{
                        //嵌入文本信息时
                        //
                        //startImageCrop(uri, 200, 200, CROP_REQUEST)
                    }

                }
            }
        }
    }

    fun startImageCrop(uri: Uri, outputX: Int, outputY: Int, requestCode: Int): Uri {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", outputX)
        intent.putExtra("outputY", outputY)
        intent.putExtra("scale", true)
        val outputPath = PathUtils.avatarTmpPath()
        val outputUri = Uri.fromFile(File(outputPath))
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        intent.putExtra("return-data", true)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", false) // face detection
        startActivityForResult(intent, requestCode)
        return outputUri
    }
}
