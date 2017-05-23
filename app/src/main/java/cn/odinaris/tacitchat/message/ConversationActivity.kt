package cn.odinaris.tacitchat.message


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

import java.util.Arrays

import cn.leancloud.chatkit.LCChatKit
import cn.leancloud.chatkit.cache.LCIMConversationItemCache
import cn.leancloud.chatkit.utils.LCIMConstants
import cn.leancloud.chatkit.utils.LCIMConversationUtils
import cn.leancloud.chatkit.utils.LCIMLogUtils
import cn.odinaris.tacitchat.R
import kotlinx.android.synthetic.main.act_conversation.*

open class ConversationActivity : AppCompatActivity() {
    protected var conversationFragment: LCIMConversationFragment = LCIMConversationFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(R.layout.act_conversation)
        this.conversationFragment = this.supportFragmentManager.findFragmentById(R.id.frag_conversation) as LCIMConversationFragment
        this.initByIntent(this.intent)
    }

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

    protected fun initActionBar(title: String?) {
        val actionBar = this.supportActionBar
        if (null != actionBar) {
            if (null != title) {
                actionBar.title = title
            }

            actionBar.setDisplayUseLogoEnabled(false)
            actionBar.setDisplayHomeAsUpEnabled(true)
            this.finishActivity(-1)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (16908332 == item.itemId) {
            this.onBackPressed()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    protected open fun updateConversation(conversation: AVIMConversation?) {
        if (null != conversation) {
            this.conversationFragment.setConversation(conversation)
            LCIMConversationItemCache.getInstance().clearUnread(conversation.conversationId)
            LCIMConversationUtils.getConversationName(conversation, object : AVCallback<String>() {
                override fun internalDone0(s: String, e: AVException?) {
                    if (null != e) {
                        LCIMLogUtils.logException(e)
                    } else {
                        this@ConversationActivity.initActionBar(s)
                    }

                }
            })
        }

    }

    protected open fun getConversation(memberId: String) {
        LCChatKit.getInstance().client.createConversation(Arrays.asList(*arrayOf(memberId)), "", null as Map<String, Any>?, false, true, object : AVIMConversationCreatedCallback() {
            override fun done(avimConversation: AVIMConversation, e: AVIMException?) {
                if (null != e) {
                    this@ConversationActivity.showToast(e.message!!)
                } else {
                    this@ConversationActivity.updateConversation(avimConversation)
                }

            }
        })
    }

    private fun showToast(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }
}

