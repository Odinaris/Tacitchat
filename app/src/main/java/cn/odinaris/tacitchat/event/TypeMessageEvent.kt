package cn.odinaris.tacitchat.event

import com.avos.avoscloud.im.v2.AVIMConversation
import com.avos.avoscloud.im.v2.AVIMTypedMessage

/**
 *  消息类型Event
 */
class TypeMessageEvent() {
    var message: AVIMTypedMessage? = null
    var conversation: AVIMConversation? = null
}