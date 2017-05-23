package cn.odinaris.tacitchat.contacts

import android.content.Context
import android.widget.Toast

import com.avos.avoscloud.AVException
import com.avos.avoscloud.AVObject
import com.avos.avoscloud.AVQuery
import com.avos.avoscloud.CountCallback
import com.avos.avoscloud.FindCallback
import com.avos.avoscloud.FollowCallback
import com.avos.avoscloud.SaveCallback

import cn.odinaris.tacitchat.App
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.service.PushManager
import cn.odinaris.tacitchat.user.TacitUser
import cn.odinaris.tacitchat.util.Constants
import cn.odinaris.tacitchat.util.SimpleNetTask
import cn.odinaris.tacitchat.util.Utils

internal class AddRequestManager {

    /**
     * 用户端未读的邀请消息的数量
     */
    private var unreadAddRequestsCount = 0

    /**
     * 是否有未读的消息
     */
    fun hasUnreadRequests(): Boolean {
        return unreadAddRequestsCount > 0
    }

    /**
     * 推送过来时自增
     */
    fun unreadRequestsIncrement() {
        ++unreadAddRequestsCount
    }

    /**
     * 从 server 获取未读消息的数量
     */
    fun countUnreadRequests(countCallback: CountCallback?) {
        val addRequestAVQuery = AVObject.getQuery(AddRequest::class.java)
        addRequestAVQuery.cachePolicy = AVQuery.CachePolicy.NETWORK_ONLY
        addRequestAVQuery.whereEqualTo(AddRequest.TO_USER, TacitUser.getCurrentUser())
        addRequestAVQuery.whereEqualTo(AddRequest.IS_READ, false)
        addRequestAVQuery.countInBackground(object : CountCallback() {
            override fun done(i: Int, e: AVException) {
                if (null != countCallback) {
                    unreadAddRequestsCount = i
                    countCallback.done(i, e)
                }
            }
        })
    }

    /**
     * 标记消息为已读，标记完后会刷新未读消息数量
     */
    fun markAddRequestsRead(addRequestList: List<AddRequest>?) {
        if (addRequestList != null) {
            for (request in addRequestList) {
                request.put(AddRequest.IS_READ, true)
            }
            AVObject.saveAllInBackground(addRequestList, object : SaveCallback() {
                override fun done(e: AVException?) {
                    if (e == null) {
                        countUnreadRequests(null)
                    }
                }
            })
        }
    }

    fun findAddRequests(skip: Int, limit: Int, findCallback: FindCallback<AddRequest>) {
        val user = TacitUser.getCurrentUser()
        val q = AVObject.getQuery(AddRequest::class.java)
        q.include(AddRequest.FROM_USER)
        q.skip(skip)
        q.limit(limit)
        q.whereEqualTo(AddRequest.TO_USER, user)
        q.orderByDescending(AVObject.CREATED_AT)
        q.cachePolicy = AVQuery.CachePolicy.NETWORK_ELSE_CACHE
        q.findInBackground(findCallback)
    }

    fun agreeAddRequest(addRequest: AddRequest, saveCallback: SaveCallback) {
        addFriend(addRequest.fromUser.objectId, object : SaveCallback() {
            override fun done(e: AVException?) {
                if (e != null) {
                    if (e.code == AVException.DUPLICATE_VALUE) {
                        addRequest.status = AddRequest.STATUS_DONE
                        addRequest.saveInBackground(saveCallback)
                    } else {
                        saveCallback.done(e)
                    }
                } else {
                    addRequest.status = AddRequest.STATUS_DONE
                    addRequest.saveInBackground(saveCallback)
                }
            }
        })
    }

    @Throws(Exception::class)
    private fun createAddRequest(toUser: TacitUser) {
        val curUser = TacitUser.getCurrentUser()
        val q = AVObject.getQuery(AddRequest::class.java)
        q.whereEqualTo(AddRequest.FROM_USER, curUser)
        q.whereEqualTo(AddRequest.TO_USER, toUser)
        q.whereEqualTo(AddRequest.STATUS, AddRequest.STATUS_WAIT)
        var count = 0
        try {
            count = q.count()
        } catch (e: AVException) {
            e.printStackTrace()
            if (e.code == AVException.OBJECT_NOT_FOUND) {
                count = 0
            } else {
                throw e
            }
        }

        if (count > 0) {
            // 抛出异常，然后提示用户
            throw IllegalStateException("请求已发送")
        } else {
            val add = AddRequest()
            add.fromUser = curUser
            add.toUser = toUser
            add.status = AddRequest.STATUS_WAIT
            add.setIsRead(false)
            add.save()
        }
    }

    fun createAddRequestInBackground(ctx: Context, user: TacitUser) {
        object : SimpleNetTask(ctx) {
            @Throws(Exception::class)
            override fun doInBack() {
                createAddRequest(user)
            }

            override fun onSucceed() {
                PushManager.instance.pushMessage(user.objectId, "您有新的好友申请",
                        Constants.INVITATION_ACTION)
                Toast.makeText(ctx, "送请求成功，等待对方验证", Toast.LENGTH_SHORT).show()
            }
        }.execute()
    }

    companion object {
        private var addRequestManager: AddRequestManager? = null

        val instance: AddRequestManager
            @Synchronized get() {
                if (addRequestManager == null) {
                    addRequestManager = AddRequestManager()
                }
                return addRequestManager!!
            }

        fun addFriend(friendId: String, saveCallback: SaveCallback?) {
            val user = TacitUser.getCurrentUser()
            user.followInBackground(friendId, object : FollowCallback<AVObject>() {
                override fun done(`object`: AVObject, e: AVException) {
                    saveCallback?.done(e)
                }
            })
        }
    }
}
