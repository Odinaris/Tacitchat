package cn.odinaris.tacitchat.login

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
import com.avos.avoscloud.SignUpCallback
import com.avos.avoscloud.im.v2.AVIMClient
import com.avos.avoscloud.im.v2.AVIMException
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback
import kotlinx.android.synthetic.main.act_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.act_register)
        initOnClickListener()
    }

    private fun initOnClickListener() {
        btn_register.setOnClickListener { register() }
        tv_retrieve_password.setOnClickListener { Toast.makeText(this,"找回密码!",Toast.LENGTH_SHORT).show() }
        tv_login.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
            finish()
        }
    }

    private fun register(){
        val name = et_username.text.toString().trim({ it <= ' ' })
        val password = et_password.text.toString().trim({ it <= ' ' })
        val confirmPassword = et_confirm.text.toString().trim({ it <= ' ' })
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this,"用户名不能为空!", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,"密码不能为空!", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this,"验证密码不能为空!", Toast.LENGTH_SHORT).show()
            return
        }
        if(password != confirmPassword) {
            Toast.makeText(this,"两次密码不相等!", Toast.LENGTH_SHORT).show()
            return
        }
        TacitUser.signUpByNameAndPwd(name, password, object : SignUpCallback() {
            override fun done(e: AVException?) {
                if (e != null) {
                    Toast.makeText(applicationContext,"注册失败!"+e.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext,"注册成功!", Toast.LENGTH_SHORT).show()
                    imLogin()
                }
            }
        })
    }

    private fun imLogin() {
        LCChatKit.getInstance().open(TacitUser.getCurrentUserId(), object : AVIMClientCallback() {
            override fun done(avimClient: AVIMClient, e: AVIMException?) {
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            }
        })
    }
}
