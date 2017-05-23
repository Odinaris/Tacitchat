package cn.odinaris.tacitchat.contacts

import com.avos.avoscloud.AVException
import com.avos.avoscloud.AVQuery
import com.avos.avoscloud.FindCallback

import java.util.ArrayList

import cn.odinaris.tacitchat.user.TacitUser
import cn.odinaris.tacitchat.util.UserCacheUtils


object ContactsManager {

    private val contactIds = ArrayList<String>()


    fun getContactIds(): List<String> {
        return contactIds
    }

    fun setContactIds(friendList: List<String>?) {
        contactIds.clear()
        if (friendList != null) {
            contactIds.addAll(friendList)
        }
    }

    fun fetchContacts(isForce: Boolean, findCallback: FindCallback<TacitUser>) {
        val policy = if (isForce) AVQuery.CachePolicy.NETWORK_ELSE_CACHE else AVQuery.CachePolicy.CACHE_ELSE_NETWORK
        TacitUser.getCurrentUser().findFriendsWithCachePolicy(policy, object : FindCallback<TacitUser>() {
            override fun done(list: List<TacitUser>, e: AVException?) {
                if (null != e) {
                    findCallback.done(null, e)
                } else {
                    val userIds = list.map { it.objectId }
                    UserCacheUtils.fetchUsers(userIds, object : UserCacheUtils.CacheUserCallback() {
                        override fun done(userList: List<TacitUser?>, e: Exception?) {
                            if(e==null){
                                setContactIds(userIds)
                                findCallback.done(userList, null)
                            }else{
                                e.printStackTrace()
                            }
                        }
                    })
                }
            }
        })
    }
}
