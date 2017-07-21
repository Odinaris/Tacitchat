package cn.odinaris.tacitchat.event

import com.avos.avoscloud.im.v2.AVIMConversation

/**
 * 对话长按点击事件
 */
class ConversationItemLongClickEvent(Conversation: AVIMConversation) {
    var conversation = Conversation
}