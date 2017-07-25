package cn.odinaris.tacitchat.contacts

import cn.odinaris.tacitchat.user.TacitUser
import com.avos.avoscloud.AVClassName
import com.avos.avoscloud.AVObject


@AVClassName("AddRequest")
class AddRequest : AVObject() {

    var fromUser: TacitUser
        get() = getAVUser(FROM_USER, TacitUser::class.java)
        set(fromUser) = put(FROM_USER, fromUser)

    var toUser: TacitUser
        get() = getAVUser(TO_USER, TacitUser::class.java)
        set(toUser) = put(TO_USER, toUser)

    var status: Int
        get() = getInt(STATUS)
        set(status) = put(STATUS, status)

    var isRead: Boolean
        get() = getBoolean(IS_READ)
        set(isRead) = put(IS_READ, isRead)

    companion object {
        val STATUS_WAIT = 0
        val STATUS_DONE = 1

        val FROM_USER = "fromUser"
        val TO_USER = "toUser"
        val STATUS = "status"

        /**
         * 标记接收方是否已读该消息
         */
        val IS_READ = "isRead"
    }
}
