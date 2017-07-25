package cn.odinaris.tacitchat.message

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leancloud.chatkit.LCChatKit
import cn.leancloud.chatkit.cache.LCIMConversationItemCache
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
        srl_conversation.setOnRefreshListener{ updateConversationList() }
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

    // 刷新对话列表
    private fun updateConversationList() {
        // Todo 改为ConversationItemCache 时获取不到对话列表
        // Todo 可能是因为缓存的原因，正式测试之前可以清除缓存重新测试
        val convIdList = LCIMConversationItemCache.getInstance().sortedConversationList
        val conversationList = ArrayList<AVIMConversation>()
        val var3 = convIdList.iterator()
        while (var3.hasNext()) { conversationList.add(LCChatKit.getInstance().client.getConversation(var3.next())) }
        this.itemAdapter.dataList = conversationList
        this.itemAdapter.notifyDataSetChanged()
        srl_conversation.isRefreshing = false
    }

    fun onEvent(updateEvent: OfflineMessageCountChangeEvent) { this.updateConversationList() }
}