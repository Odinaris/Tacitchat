package cn.odinaris.tacitchat.event

import com.avos.avoscloud.im.v2.AVIMConversation

/**
 * 离线消息计数更新
 */
class OfflineMessageCountChangeEvent() {
    var conversation: AVIMConversation? = null
}