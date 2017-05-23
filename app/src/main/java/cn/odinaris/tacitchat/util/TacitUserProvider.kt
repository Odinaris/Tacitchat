package cn.odinaris.tacitchat.util

import java.util.ArrayList
import cn.leancloud.chatkit.LCChatKitUser
import cn.leancloud.chatkit.LCChatProfileProvider
import cn.leancloud.chatkit.LCChatProfilesCallBack
import cn.odinaris.tacitchat.user.TacitUser

class TacitUserProvider : LCChatProfileProvider {

    private fun getThirdPartUser(tacitUser: TacitUser?): LCChatKitUser {
        return LCChatKitUser(tacitUser!!.objectId, tacitUser.username, tacitUser.avatarUrl)
    }

    private fun getThirdPartUsers(tacitUsers: List<TacitUser?>): List<LCChatKitUser> {
        val thirdPartUsers = ArrayList<LCChatKitUser>()
        for (user in tacitUsers) {
            thirdPartUsers.add(getThirdPartUser(user))
        }
        return thirdPartUsers
    }

    override fun fetchProfiles(list: List<String>, callBack: LCChatProfilesCallBack) {
        UserCacheUtils.fetchUsers(list, object : UserCacheUtils.CacheUserCallback() {
            override fun done(userList: List<TacitUser?>, e: Exception?) {
                callBack.done(getThirdPartUsers(userList), e)
            }
        })
    }
}
