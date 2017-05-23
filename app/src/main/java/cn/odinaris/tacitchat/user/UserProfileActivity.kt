package cn.odinaris.tacitchat.user

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import cn.leancloud.chatkit.utils.LCIMConstants
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.message.ConversationActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.act_user_profile.*
import com.avos.avoscloud.AVException
import com.avos.avoscloud.FollowCallback

class UserProfileActivity : AppCompatActivity() {

    var objectId = ""
    var avatarUrl = ""
    var username = ""
    var isAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.act_user_profile)
        getUserData()
        initView()
        initOnClickListener()
    }

    private fun getUserData() {
        objectId = intent.getStringExtra("objectId")
        avatarUrl = intent.getStringExtra("avatarUrl")
        username = intent.getStringExtra("username")
        isAdded = intent.getBooleanExtra("isAdded",false)
    }

    private fun initOnClickListener() {
        tv_add_request.setOnClickListener{
            TacitUser.getCurrentUser().followInBackground(objectId, object : FollowCallback<TacitUser>() {
                override fun done(user: TacitUser?, e: AVException?) {
                    if (e == null) {
                        Toast.makeText(applicationContext,"添加成功!",Toast.LENGTH_SHORT).show()
                        finish()
                    } else if (e.code == AVException.DUPLICATE_VALUE) {
                        Toast.makeText(applicationContext,"好友已添加!",Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            })
            Toast.makeText(this,"添加好友!",Toast.LENGTH_SHORT).show()
        }
        tv_chat.setOnClickListener{
            val intent = Intent(this, ConversationActivity::class.java)
            intent.putExtra(LCIMConstants.PEER_ID, objectId)
            intent.putExtra(username, username)
            startActivity(intent)
        }
    }

    private fun initView() {
        Glide.with(this).load(avatarUrl).into(riv_user_avatar)
        tv_user_name.text = username
        if(isAdded){
            tb_user_profile.title = username
            tv_chat.visibility = View.VISIBLE
        }
        else{ tv_add_request.visibility = View.VISIBLE }
    }
}
