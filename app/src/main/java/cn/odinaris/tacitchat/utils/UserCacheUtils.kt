package cn.odinaris.tacitchat.utils

import android.text.TextUtils

import com.avos.avoscloud.AVException
import com.avos.avoscloud.AVQuery
import com.avos.avoscloud.FindCallback
import java.util.HashMap

import cn.odinaris.tacitchat.user.TacitUser

/**
 * TODO 1、本地存储 2、避免内存、外存占用过多
 */
object UserCacheUtils {

    private var userMap: MutableMap<String, TacitUser>? = null

    init { userMap = HashMap<String, TacitUser>() }

    fun getCachedUser(objectId: String): TacitUser? { return userMap!![objectId] }

    fun hasCachedUser(objectId: String): Boolean { return userMap!!.containsKey(objectId) }

    fun cacheUser(user: TacitUser?) {
        if (null != user && !TextUtils.isEmpty(user.objectId)) { userMap!!.put(user.objectId, user) }
    }

    fun cacheUsers(users: List<TacitUser>?) { if (null != users) { for (user in users) { cacheUser(user) } } }


    fun fetchUsers(ids: List<String>, cacheUserCallback: CacheUserCallback? = null) {
        val unCachedIds = ids.filterNot { userMap!!.containsKey(it) }.toSet()
        if (unCachedIds.isEmpty()) {
            if (null != cacheUserCallback) {
                cacheUserCallback.done(getUsersFromCache(ids), null)
                return
            }
        }
        val q = TacitUser.getQuery(TacitUser::class.java)
        q.whereContainedIn(Constants.OBJECT_ID, unCachedIds)
        q.limit = 1000
        q.cachePolicy = AVQuery.CachePolicy.NETWORK_ELSE_CACHE
        q.findInBackground(object : FindCallback<TacitUser>() {
            override fun done(list: List<TacitUser>, e: AVException?) {
                if (null == e) {
                    for (user in list) {
                        userMap!!.put(user.objectId, user)
                    }
                }
                cacheUserCallback?.done(getUsersFromCache(ids), e)
            }
        })
    }

    fun getUsersFromCache(ids: List<String>): List<TacitUser?> {
        val userList = ids.filter { userMap!!.containsKey(it) }.map { userMap!![it] }
        return userList
    }

    abstract class CacheUserCallback {
        abstract fun done(userList: List<TacitUser?>, e: Exception?)
    }
}
