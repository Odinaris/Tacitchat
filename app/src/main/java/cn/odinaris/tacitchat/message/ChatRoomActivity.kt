package cn.odinaris.tacitchat.message

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuItem

import com.avos.avoscloud.im.v2.AVIMConversation
import com.avos.avoscloud.im.v2.AVIMException
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback
import cn.odinaris.tacitchat.util.ConversationUtils

class ChatRoomActivity : ConversationActivity() {

    private var conversation: AVIMConversation? = null

    override fun onResume() { super.onResume() }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.chat_ativity_menu, menu);
        if (null != menu && menu.size() > 0) {
            val item = menu.getItem(0)
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS or MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun updateConversation(conversation: AVIMConversation?) {
        super.updateConversation(conversation)
        this.conversation = conversation
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                QUIT_GROUP_REQUEST -> finish()
            }
        }
    }

    override fun getConversation(memberId: String) {
        super.getConversation(memberId)
        ConversationUtils.createSingleConversation(memberId, object : AVIMConversationCreatedCallback() {
            override fun done(avimConversation: AVIMConversation?, e: AVIMException?) {
                updateConversation(avimConversation)
            }
        })
    }

    companion object { val QUIT_GROUP_REQUEST = 200 }
}