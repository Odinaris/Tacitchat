package cn.odinaris.tacitchat.user

import com.avos.avoscloud.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class TacitChatUser : AVUser() {

    val avatarUrl = if(getAVFile<AVFile>(AVATAR)!=null) getAVFile<AVFile>(AVATAR).url else null

    fun saveAvatar(path: String, saveCallback: SaveCallback?) {
        val file: AVFile
        try {
            file = AVFile.withAbsoluteLocalPath(username, path)
            put(AVATAR, file)
            file.saveInBackground(object : SaveCallback() {
                override fun done(e: AVException?) {
                    if (null == e) {
                        saveInBackground(saveCallback)
                    } else {
                        saveCallback?.done(e)
                    }
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun updateUserInfo() {
        val installation = AVInstallation.getCurrentInstallation()
        if (installation != null) {
            put(INSTALLATION, installation)
            saveInBackground()
        }
    }

    fun removeFriend(friendId: String, saveCallback: SaveCallback?) {
        unfollowInBackground(friendId, object : FollowCallback<TacitChatUser>() {
            override fun done(`object`: TacitChatUser?, e: AVException?) {
                saveCallback?.done(e)
            }
        })
    }

    fun findFriendsWithCachePolicy(cachePolicy: AVQuery.CachePolicy, findCallback: FindCallback<TacitChatUser>) {
        var q: AVQuery<TacitChatUser>? = null
        try { q = followeeQuery(TacitChatUser::class.java) }
        catch (e: Exception) { }
        q!!.cachePolicy = cachePolicy
        q.maxCacheAge = TimeUnit.MINUTES.toMillis(1)
        q.findInBackground(findCallback)
    }

    companion object {

        val USERNAME = "username"
        val AVATAR = "avatar"
        val INSTALLATION = "installation"

        val currentUser = AVUser.getCurrentUser(TacitChatUser::class.java)

        val currentUserId = currentUser?.getObjectId()

        fun signUpByNameAndPwd(name: String, password: String, callback: SignUpCallback) {
            val user = AVUser()
            user.username = name
            user.setPassword(password)
            user.signUpInBackground(callback)
        }


    }
}
