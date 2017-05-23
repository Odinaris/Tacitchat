package cn.odinaris.tacitchat.util

import android.text.TextUtils

import com.avos.avoscloud.AVCallback
import com.avos.avoscloud.AVException
import com.avos.avoscloud.im.v2.AVIMConversation
import com.avos.avoscloud.im.v2.AVIMConversationQuery
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

import cn.leancloud.chatkit.LCChatKit
import cn.leancloud.chatkit.LCChatKitUser
import cn.leancloud.chatkit.cache.LCIMProfileCache
import cn.leancloud.chatkit.utils.LCIMConversationUtils
import cn.odinaris.tacitchat.model.ConversationType


class ConversationUtils : LCIMConversationUtils() {
    companion object {
        fun typeOfConversation(conversation: AVIMConversation): ConversationType {
            if (isValidConversation(conversation)) {
                val typeObject = conversation.getAttribute(ConversationType.TYPE_KEY)
                val typeInt = typeObject as Int
                return ConversationType.fromInt(typeInt)
            } else {
                //LogUtils.e("invalid conversation ");
                // 因为 Group 不需要取 otherId，检查没那么严格，避免导致崩溃
                return ConversationType.Group
            }
        }

        fun getConversationPeerId(conversation: AVIMConversation?): String {
            if (null != conversation && 2 == conversation.members.size) {
                val currentUserId = LCChatKit.getInstance().currentUserId
                val firstMemeberId = conversation.members[0]
                return conversation.members[if (firstMemeberId == currentUserId) 1 else 0]
            }
            return ""
        }

        fun createGroupConversation(memberIds: List<String>, callback: AVIMConversationCreatedCallback) {
            LCIMProfileCache.getInstance().getCachedUsers(memberIds, object : AVCallback<List<LCChatKitUser>>() {
                override fun internalDone0(lcimUserProfiles: List<LCChatKitUser>, e: AVException) {
                    val nameList = ArrayList<String>()
                    for (userProfile in lcimUserProfiles) {
                        nameList.add(userProfile.userName)
                    }

                    val attrs = HashMap<String, Any>()
                    attrs.put(ConversationType.TYPE_KEY, ConversationType.Group.value)
                    attrs.put("name", TextUtils.join(",", nameList))
                    LCChatKit.getInstance().client.createConversation(memberIds, "", attrs, false, true, callback)
                }
            })
        }

        fun createSingleConversation(memberId: String, callback: AVIMConversationCreatedCallback) {
            val attrs = HashMap<String, Any>()
            attrs.put(ConversationType.TYPE_KEY, ConversationType.Single.value)
            LCChatKit.getInstance().client.createConversation(Arrays.asList(memberId), "", attrs, false, true, callback)
        }

        fun findGroupConversationsIncludeMe(callback: AVIMConversationQueryCallback?) {
            val conversationQuery = LCChatKit.getInstance().client.query
            if (null != conversationQuery) {
                conversationQuery.containsMembers(Arrays.asList(LCChatKit.getInstance().currentUserId))
                conversationQuery.whereEqualTo(ConversationType.ATTR_TYPE_KEY, ConversationType.Group.value)
                conversationQuery.orderByDescending(Constants.UPDATED_AT)
                conversationQuery.limit(1000)
                conversationQuery.findInBackground(callback)
            } else callback?.done(ArrayList<AVIMConversation>(), null)
        }

        fun isValidConversation(conversation: AVIMConversation?): Boolean {
            if (conversation == null) {
                //      LogUtils.d("invalid reason : conversation is null");
                return false
            }
            if (conversation.members == null || conversation.members.size == 0) {
                //      LogUtils.d("invalid reason : conversation members null or empty");
                return false
            }
            val type = conversation.getAttribute(ConversationType.TYPE_KEY) ?: //      LogUtils.d("invalid reason : type is null");
                    return false

            val typeInt = type as Int
            if (typeInt == ConversationType.Single.value) {
                if (conversation.members.size != 2 || !conversation.members.contains(LCChatKit.getInstance().currentUserId)) {
                    //        LogUtils.d("invalid reason : oneToOne conversation not correct");
                    return false
                }
            } else if (typeInt == ConversationType.Group.value) {

            } else {
                //      LogUtils.d("invalid reason : typeInt wrong");
                return false
            }
            return true
        }
    }
}
