package cn.odinaris.tacitchat.message


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.Window
import android.widget.Toast

import com.avos.avoscloud.AVCallback
import com.avos.avoscloud.AVException
import com.avos.avoscloud.im.v2.AVIMConversation
import com.avos.avoscloud.im.v2.AVIMException
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback

import cn.leancloud.chatkit.LCChatKit
import cn.leancloud.chatkit.cache.LCIMConversationItemCache
import cn.leancloud.chatkit.utils.LCIMConstants
import cn.leancloud.chatkit.utils.LCIMConversationUtils
import cn.leancloud.chatkit.utils.LCIMLogUtils
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.utils.ConversationUtils
import kotlinx.android.synthetic.main.act_conversation.*

class ChatActivity : AppCompatActivity() {
    val QUIT_GROUP_REQUEST = 200
    var chatFragment: ChatFragment = ChatFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(R.layout.act_conversation)
        this.chatFragment = this.supportFragmentManager.findFragmentById(R.id.frag_conversation) as ChatFragment
        this.initByIntent(this.intent)
    }

    override fun onResume() { super.onResume() }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.initByIntent(intent)
    }

    private fun initByIntent(intent: Intent) {
        if (null == LCChatKit.getInstance().client) {
            this.showToast("please login first!")
            this.finish()
        } else {
            val extras = intent.extras
            if (null != extras) {
                if (extras.containsKey(LCIMConstants.PEER_ID)) {
                    // Todo 优化对话界面Title设计
                    tb_conversation_username.title = extras.getString("username")
                    this.getConversation(extras.getString(LCIMConstants.PEER_ID))
                } else if (extras.containsKey(LCIMConstants.CONVERSATION_ID)) {
                    val conversationId = extras.getString(LCIMConstants.CONVERSATION_ID)
                    this.updateConversation(LCChatKit.getInstance().client.getConversation(conversationId))
                } else {
                    this.showToast("memberId or conversationId is needed")
                    this.finish()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (16908332 == item.itemId) {
            this.onBackPressed()
            return true
        }
        else { return super.onOptionsItemSelected(item) }
    }

    fun updateConversation(conversation: AVIMConversation?) {
        if (null != conversation) {
            this.chatFragment.setConversation(conversation)
            LCIMConversationItemCache.getInstance().clearUnread(conversation.conversationId)
            LCIMConversationUtils.getConversationName(conversation, object : AVCallback<String>() {
                override fun internalDone0(s: String, e: AVException?) {
                    if (null != e) { LCIMLogUtils.logException(e) }
                    else { tb_conversation_username.title = s }
                }
            })
        }
    }

    fun getConversation(memberId: String) {
//        LCChatKit.getInstance().client.createConversation(Arrays.asList(*arrayOf(memberId)), "", null as Map<String, Any>?, false, true, object : AVIMConversationCreatedCallback() {
//            override fun done(avimConversation: AVIMConversation, e: AVIMException?) {
//                if (null != e) {
//                    this@ChatActivity.showToast(e.message!!)
//                }
//                else {
//                    this@ChatActivity.updateConversation(avimConversation)
//                }
//
//            }
//        })
        ConversationUtils.createSingleConversation(memberId, object : AVIMConversationCreatedCallback() {
            override fun done(avimConversation: AVIMConversation?, e: AVIMException?) {
                updateConversation(avimConversation)
            }

        })
    }

    private fun showToast(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if(intent!=null){
            if (resultCode == Activity.RESULT_OK) { when (requestCode) { QUIT_GROUP_REQUEST -> finish() } }
        }
    }
}

