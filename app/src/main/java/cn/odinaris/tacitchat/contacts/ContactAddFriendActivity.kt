package cn.odinaris.tacitchat.contacts

import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.user.TacitUser
import cn.odinaris.tacitchat.user.UserProfileActivity
import cn.odinaris.tacitchat.util.Utils
import com.avos.avoscloud.AVException
import com.avos.avoscloud.FindCallback
import kotlinx.android.synthetic.main.act_add_contact.*

class ContactAddFriendActivity : AppCompatActivity() {

    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.act_add_contact)
        btn_search.setOnClickListener{ search() }
    }

    private fun search() {
        val name = et_contact_name.text.toString().trim()
        if(name != TacitUser.getCurrentUser().username) {
            if(name != ""){
                dialog = Utils.showSpinnerDialog(this)
                searchContact(name)
            }
            else {
                Toast.makeText(this,"用户名不能为空!",Toast.LENGTH_SHORT).show()
                return
            }
        }
        else {
            Toast.makeText(this,"与当前用户重名!",Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchContact(name: String) {
        val q = TacitUser.getQuery(TacitUser::class.java)
        q.whereEqualTo(TacitUser.USERNAME, name)
        q.findInBackground(object : FindCallback<TacitUser>() {
            override fun done(list: List<TacitUser>, e: AVException?) {
                dialog?.dismiss()
                if(list.isNotEmpty()) {
                    val intent = Intent(this@ContactAddFriendActivity,UserProfileActivity::class.java)
                    intent.putExtra("avatarUrl", list[0].avatarUrl)
                    intent.putExtra("username", list[0].username)
                    intent.putExtra("objectId", list[0].objectId)
                    intent.putExtra("isAdded", false)
                    startActivity(intent)
                    finish()
                }
                if(e!=null){
                    Toast.makeText(applicationContext,e.message,Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
