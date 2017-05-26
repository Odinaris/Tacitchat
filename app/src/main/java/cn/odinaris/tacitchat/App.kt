package cn.odinaris.tacitchat

import com.avos.avoscloud.AVOSCloud
import android.app.Application
import android.content.Context
import cn.leancloud.chatkit.LCChatKit
import cn.odinaris.tacitchat.contacts.AddRequest
import cn.odinaris.tacitchat.user.TacitUser
import cn.odinaris.tacitchat.utils.TacitUserProvider
import com.avos.avoscloud.AVObject

class App : Application() {
    var debug = true
    val appId = "NSwg7Wb6p1mSna80WEeNp0lK-gzGzoHsz"
    val appKey = "nYD3Kv2HztIKMCWuBOiILumq"
    var ctx: Context? = null
    override fun onCreate() {
        super.onCreate()
        //AVOSCloud.initialize(this, appId, appKey)       //初始化LeanCloud
        AVOSCloud.setDebugLogEnabled(true)              //开启调试日志
        TacitUser.alwaysUseSubUserClass(TacitUser::class.java)
        AVObject.registerSubclass(AddRequest::class.java)//添加好友
//        AVObject.registerSubclass(UpdateInfo::class.java!!)//更新信息
        AVOSCloud.setLastModifyEnabled(true)// 节省流量
        LCChatKit.getInstance().profileProvider = TacitUserProvider()
        LCChatKit.getInstance().init(applicationContext, appId, appKey)
    }

}
