package cn.odinaris.tacitchat.message

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leancloud.chatkit.LCChatKit
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.cache.ConversationItemCache
import cn.odinaris.tacitchat.event.ConversationItemLongClickEvent
import cn.odinaris.tacitchat.event.OfflineMessageCountChangeEvent
import cn.odinaris.tacitchat.event.TypeMessageEvent
import cn.odinaris.tacitchat.message.viewholder.ConversationItemHolder
import com.avos.avoscloud.im.v2.AVIMConversation
import de.greenrobot.event.EventBus
import java.util.ArrayList
import kotlinx.android.synthetic.main.frg_conversation.*

class ConversationFragment : Fragment() {

    private var itemAdapter : ConversationListAdapter<AVIMConversation> = ConversationListAdapter(ConversationItemHolder::class.java)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.frg_conversation, container, false)
    }

    override fun onViewCreated(view: View,savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        srl_conversation.isEnabled = true
        rv_conversation_list.layoutManager = LinearLayoutManager(activity)
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

    fun onEvent(event: TypeMessageEvent) { this.updateConversationList() }

    fun onEvent(event: ConversationItemLongClickEvent) {
        val conversationId = event.conversation.conversationId
        ConversationItemCache.instance.deleteConversation(conversationId)
        this.updateConversationList()
        //if (null != event.conversation) { }
    }

    private fun updateConversationList() {
        val convIdList = ConversationItemCache.instance.sortedConversationList
        val conversationList = ArrayList<AVIMConversation>()
        val var3 = convIdList.iterator()
        while (var3.hasNext()) { conversationList.add(LCChatKit.getInstance().client.getConversation(var3.next())) }
        this.itemAdapter.dataList = conversationList
        this.itemAdapter.notifyDataSetChanged()
    }

    fun onEvent(updateEvent: OfflineMessageCountChangeEvent) { this.updateConversationList() }
}