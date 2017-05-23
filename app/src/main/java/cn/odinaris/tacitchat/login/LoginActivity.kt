package cn.odinaris.tacitchat.login

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.widget.Toast
import cn.leancloud.chatkit.LCChatKit
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.main.MainActivity
import cn.odinaris.tacitchat.user.TacitUser
import com.avos.avoscloud.AVException
import com.avos.avoscloud.LogInCallback
import com.avos.avoscloud.im.v2.AVIMClient
import com.avos.avoscloud.im.v2.AVIMException
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback


import kotlinx.android.synthetic.main.act_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.act_login)
        initOnClickListener()
    }

    private fun initOnClickListener() {
        btn_login.setOnClickListener { login() }
        tv_retrieve_password.setOnClickListener { Toast.makeText(this,"找回密码!",Toast.LENGTH_SHORT).show() }
        tv_new_user.setOnClickListener {
            startActivity(Intent(this@LoginActivity,RegisterActivity::class.java))
            finish()
        }
    }

    private fun login() {
        val name = et_username.text.toString().trim({ it <= ' ' })
        val password = et_password.text.toString().trim({ it <= ' ' })
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this,"用户名不能为空!",Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,"密码不能为空!",Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = ProgressDialog(this)
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setCancelable(true)
        dialog.setMessage("加载中...")
        if (!isFinishing) { dialog.show() }
        TacitUser.logInInBackground(name, password, object : LogInCallback<TacitUser>() {
            override fun done(avUser: TacitUser, e: AVException?) {
                if(e!=null){
                    e.printStackTrace()
                    Toast.makeText(applicationContext,e.message,Toast.LENGTH_SHORT).show()
                }else{
                    dialog.dismiss()
                    imLogin()
                }
            }
        }, TacitUser::class.java)
    }

    private fun imLogin() {
        LCChatKit.getInstance().open(TacitUser.getCurrentUserId(), object : AVIMClientCallback() {
            override fun done(avimClient: AVIMClient?, e: AVIMException?) {
                e?.printStackTrace()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        })
    }

}
