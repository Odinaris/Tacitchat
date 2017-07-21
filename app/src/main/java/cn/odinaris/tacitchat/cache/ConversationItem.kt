package cn.odinaris.tacitchat.cache

import cn.odinaris.tacitchat.utils.LogUtils
import com.alibaba.fastjson.JSONObject

class ConversationItem() : Comparable<Any> {
    var conversationId = ""
    var unreadCount = 0
    var updateTime = 0L

    constructor(conversationId: String) : this() { this.conversationId = conversationId }

    fun toJsonString(): String {
        val jsonObject = JSONObject()
        jsonObject.put("conversation_id", this.conversationId)
        jsonObject.put("unreadcount", Integer.valueOf(this.unreadCount))
        jsonObject.put("update_time", java.lang.Long.valueOf(this.updateTime))
        return jsonObject.toJSONString()
    }

    override fun compareTo(other: Any): Int { return ((other as ConversationItem).updateTime - this.updateTime).toInt() }

    companion object {
        private val ITEM_KEY_CONVCERSATION_ID = "conversation_id"
        private val ITEM_KEY_UNREADCOUNT = "unreadcount"
        private val ITEM_KEY_UNDATE_TIME = "upadte_time"

        fun fromJsonString(json: String): ConversationItem {
            val item = ConversationItem()
            var jsonObject: JSONObject? = null

            try {
                jsonObject = JSONObject.parseObject(json)
                item.conversationId = jsonObject!!.getString("conversation_id")
                item.unreadCount = jsonObject.getInteger("unreadcount")!!.toInt()
                item.updateTime = jsonObject.getLong("update_time")!!.toLong()
            } catch (var4: Exception) {
                LogUtils.logException(var4)
            }
            return item
        }
    }
}
