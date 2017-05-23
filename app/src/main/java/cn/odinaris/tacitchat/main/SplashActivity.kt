package cn.odinaris.tacitchat.main

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.Toast
import cn.leancloud.chatkit.LCChatKit

import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.login.LoginActivity
import cn.odinaris.tacitchat.user.TacitChatUser
import cn.odinaris.tacitchat.util.ImageUtils
import com.avos.avoscloud.im.v2.AVIMClient
import com.avos.avoscloud.im.v2.AVIMException
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback
import kotlinx.android.synthetic.main.act_splash.*

class SplashActivity : Activity() {

    val SPLASH_DURATION = 2000
    private val GO_MAIN_MSG = 1
    private val GO_LOGIN_MSG = 2
    private var bmp: Bitmap? = null

    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GO_MAIN_MSG -> imLogin()
                GO_LOGIN_MSG -> {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_splash)
        bmp = ImageUtils.readBitMap(R.drawable.welcome,applicationContext)
        iv_welcome.setImageBitmap(bmp)
        if (TacitChatUser.currentUser != null) {
            TacitChatUser.currentUser.updateUserInfo()
            handler.sendEmptyMessageDelayed(GO_MAIN_MSG, SPLASH_DURATION.toLong())
        } else {
            handler.sendEmptyMessageDelayed(GO_LOGIN_MSG, SPLASH_DURATION.toLong())
        }
    }

    private fun imLogin() {
        LCChatKit.getInstance().open(TacitChatUser.currentUserId, object : AVIMClientCallback() {
            override fun done(avimClient: AVIMClient, e: AVIMException?) {
                e?.printStackTrace()
                if(e?.message != "" && e!=null){
                    Toast.makeText(applicationContext,e.message, Toast.LENGTH_SHORT).show()
                }
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        bmp!!.recycle()
        System.gc()
    }
}

