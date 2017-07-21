package cn.odinaris.tacitchat.cache

import android.content.Context
import android.text.TextUtils
import com.avos.avoscloud.AVCallback
import com.avos.avoscloud.AVException
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.TreeSet

class ConversationItemCache {
    private val CONVERSATION_ITEM_TABLE_NAME = "ConversationItem"
    private val conversationItemMap = HashMap<String, ConversationItem>()
    private var conversationItemDBHelper: LocalStorage? = null

    @Synchronized fun initDB(context: Context, clientId: String, callback: AVCallback<*>) {
        this.conversationItemDBHelper = LocalStorage(context, clientId, "ConversationItem")
        this.conversationItemMap.clear()
        this.syncData(callback)
    }

    fun increaseUnreadCount(convid: String) {
        if (!TextUtils.isEmpty(convid)) {
            this.increaseUnreadCount(convid, 1)
        }

    }

    @Synchronized fun increaseUnreadCount(convId: String, increment: Int) {
        if (!TextUtils.isEmpty(convId) && increment > 0) {
            val conversationItem = this.getConversationItemFromMap(convId)
            conversationItem.unreadCount = conversationItem.unreadCount.plus(increment)
            conversationItem.updateTime = System.currentTimeMillis()
            this.syncToCache(conversationItem)
        }
    }

    @Synchronized fun clearUnread(convid: String) {
        if (!TextUtils.isEmpty(convid)) {
            val unreadCountItem = this.getConversationItemFromMap(convid)
            unreadCountItem.unreadCount = 0
            unreadCountItem.updateTime = System.currentTimeMillis()
            this.syncToCache(unreadCountItem)
        }

    }

    @Synchronized fun deleteConversation(convid: String) {
        if (!TextUtils.isEmpty(convid)) {
            this.conversationItemMap.remove(convid)
            this.conversationItemDBHelper!!.deleteData(Arrays.asList(*arrayOf(convid)))
        }
    }

    @Synchronized fun insertConversation(convId: String) {
        if (!TextUtils.isEmpty(convId)) {
            val item = this.getConversationItemFromMap(convId)
            item.updateTime = System.currentTimeMillis()
            this.syncToCache(item)
        }
    }

    @Synchronized fun insertConversation(convId: String, milliSeconds: Long) {
        if (!TextUtils.isEmpty(convId) && milliSeconds >= 0L) {
            val item = this.getConversationItemFromMap(convId)
            item.updateTime = milliSeconds
            this.syncToCache(item)
        }
    }

    @Synchronized
    fun getUnreadCount(convId: String): Int { return this.getConversationItemFromMap(convId).unreadCount }

    val sortedConversationList: List<String>
        @Synchronized
        get() {
            val idList = ArrayList<String>()
            val sortedSet = TreeSet<ConversationItem>()
            sortedSet.addAll(this.conversationItemMap.values)
            val var3 = sortedSet.iterator()

            while (var3.hasNext()) {
                val item = var3.next()
                idList.add(item.conversationId)
            }

            return idList
        }

    private fun syncData(callback: AVCallback<*>) {
        this.conversationItemDBHelper!!.getIds(object : AVCallback<List<String>>() {
            override fun internalDone0(idList: List<String>, e: AVException) {
                this@ConversationItemCache.conversationItemDBHelper!!.getData(idList, object : AVCallback<List<String>>() {
                    override fun internalDone0(dataList: List<String>?, e: AVException) {
                        if (null != dataList) {
                            for (i in dataList.indices) {
                                val conversationItem = ConversationItem.fromJsonString(dataList[i])
                                this@ConversationItemCache.conversationItemMap.put(conversationItem.conversationId, conversationItem)
                            }
                        }
                        callback.internalDone(e)
                    }
                })
            }
        })
    }

    private fun getConversationItemFromMap(convId: String): ConversationItem {
        return if (this.conversationItemMap.containsKey(convId)) this.conversationItemMap[convId]!! else ConversationItem(convId)
    }

    private fun syncToCache(item: ConversationItem?) {
        if (null != item) {
            this.conversationItemMap.put(item.conversationId, item)
            this.conversationItemDBHelper!!.insertData(item.conversationId, item.toJsonString())
        }

    }

    companion object {
        private var conversationItemCache: ConversationItemCache? = null

        val instance: ConversationItemCache
            @Synchronized get() {
                if (null == conversationItemCache) { conversationItemCache = ConversationItemCache() }
                return conversationItemCache as ConversationItemCache
            }
    }
}
