package cn.odinaris.tacitchat.service

import android.content.Context
import android.text.TextUtils

import com.avos.avoscloud.AVInstallation
import com.avos.avoscloud.AVPush
import com.avos.avoscloud.PushService
import java.util.HashMap

import cn.odinaris.tacitchat.main.SplashActivity
import cn.odinaris.tacitchat.user.TacitUser

class PushManager {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context
        PushService.setDefaultPushCallback(context, SplashActivity::class.java)
        subscribeCurrentUserChannel()
    }

    private fun subscribeCurrentUserChannel() {
        val currentUserId = TacitUser.getCurrentUserId()
        if (!TextUtils.isEmpty(currentUserId)) {
            PushService.subscribe(context, currentUserId, SplashActivity::class.java)
        }
    }

    fun unsubscribeCurrentUserChannel() {
        val currentUserId = TacitUser.getCurrentUserId()
        if (!TextUtils.isEmpty(currentUserId)) {
            PushService.unsubscribe(context, currentUserId)
        }
    }

    fun pushMessage(userId: String, message: String, action: String) {
        val query = AVInstallation.getQuery()
        query.whereContains(INSTALLATION_CHANNELS, userId)
        val push = AVPush()
        push.setQuery(query)
        val dataMap = HashMap<String, Any>()
        dataMap.put(AVOS_ALERT, message)
        dataMap.put(AVOS_PUSH_ACTION, action)
        push.setData(dataMap)
        push.sendInBackground()
    }

    companion object {
        val AVOS_ALERT = "alert"

        private val AVOS_PUSH_ACTION = "action"
        val INSTALLATION_CHANNELS = "channels"
        private var pushManager: PushManager? = null

        val instance: PushManager
            @Synchronized get() {
                if (pushManager == null) {
                    pushManager = PushManager()
                }
                return pushManager!!
            }
    }
}