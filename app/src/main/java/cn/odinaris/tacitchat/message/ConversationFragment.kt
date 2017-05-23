package cn.odinaris.tacitchat.message

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leancloud.chatkit.LCChatKit
import cn.odinaris.tacitchat.R
import cn.leancloud.chatkit.cache.LCIMConversationItemCache
import cn.leancloud.chatkit.event.LCIMConversationItemLongClickEvent
import cn.leancloud.chatkit.event.LCIMIMTypeMessageEvent
import cn.leancloud.chatkit.event.LCIMOfflineMessageCountChangeEvent
import cn.leancloud.chatkit.view.LCIMDividerItemDecoration
import cn.odinaris.tacitchat.message.viewholder.LCIMConversationItemHolder
import com.avos.avoscloud.im.v2.AVIMConversation
import de.greenrobot.event.EventBus
import java.util.ArrayList
import kotlinx.android.synthetic.main.frg_conversation.*

class ConversationFragment : Fragment() {

    private var itemAdapter : LCIMCommonListAdapter<AVIMConversation> = LCIMCommonListAdapter(LCIMConversationItemHolder::class.java)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.frg_conversation, container, false)
    }

    override fun onViewCreated(view: View,savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        srl_conversation.isEnabled = true
        rv_conversation_list.layoutManager = LinearLayoutManager(context)
        rv_conversation_list.addItemDecoration(LCIMDividerItemDecoration(context))
        rv_conversation_list.adapter = itemAdapter
        EventBus.getDefault().register(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.updateConversationList()
    }

    override fun onResume() {
        super.onResume()
        this.updateConversationList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    fun onEvent(event: LCIMIMTypeMessageEvent) { this.updateConversationList() }

    fun onEvent(event: LCIMConversationItemLongClickEvent) {
        if (null != event.conversation) {
            val conversationId = event.conversation.conversationId
            LCIMConversationItemCache.getInstance().deleteConversation(conversationId)
            this.updateConversationList()
        }

    }

    private fun updateConversationList() {
        val convIdList = LCIMConversationItemCache.getInstance().sortedConversationList
        val conversationList = ArrayList<AVIMConversation>()
        val var3 = convIdList.iterator()
        while (var3.hasNext()) { conversationList.add(LCChatKit.getInstance().client.getConversation(var3.next())) }
        this.itemAdapter.setDataList(conversationList)
        this.itemAdapter.notifyDataSetChanged()
    }

    fun onEvent(updateEvent: LCIMOfflineMessageCountChangeEvent) { this.updateConversationList() }
}