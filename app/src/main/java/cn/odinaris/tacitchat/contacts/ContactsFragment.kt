package cn.odinaris.tacitchat.contacts

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.adapter.ContactsAdapter
import cn.odinaris.tacitchat.user.TacitUser
import cn.odinaris.tacitchat.user.UserProfileActivity
import cn.odinaris.tacitchat.utils.Utils.filterException
import cn.odinaris.tacitchat.utils.Utils.showSpinnerDialog
import com.avos.avoscloud.AVException
import com.avos.avoscloud.FindCallback
import com.avos.avoscloud.SaveCallback
import kotlinx.android.synthetic.main.frg_contacts.*
import kotlinx.android.synthetic.main.item_new_request.view.*


class ContactsFragment : Fragment() {

    var contactsAdapter: ContactsAdapter? = null
    var contacts = ArrayList<TacitUser>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view : View = inflater!!.inflate(R.layout.frg_contacts,container,false)
        return view
    }
    override fun onViewCreated(view: View,savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        contactsAdapter = ContactsAdapter(context)
        contactsAdapter?.setOnItemClickListener(object:ContactsAdapter.onItemClickListener{
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(context, UserProfileActivity::class.java)
                intent.putExtra("objectId",contacts[position].objectId)
                intent.putExtra("avatarUrl",contacts[position].avatarUrl)
                intent.putExtra("username",contacts[position].username)
                intent.putExtra("isAdded",true)
                context.startActivity(intent)
            }
            override fun onItemLongClick(view: View, position: Int) {
                AlertDialog.Builder(context).setMessage("确认删除联系人?")
                        .setPositiveButton("确定", { _, _ ->
                            val dialog1 = showSpinnerDialog(activity)
                            TacitUser.getCurrentUser().removeFriend(contacts[position].objectId, object : SaveCallback() {
                                override fun done(e: AVException?) {
                                    dialog1.dismiss()
                                    if (filterException(e)) {
                                        getMembers(true)
                                    }
                                }
                            })
                        }).setNegativeButton("取消", null).show()
            }
        })
        val toolbar = tb_contacts as Toolbar
        toolbar.inflateMenu(R.menu.contacts_menu)
        toolbar.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.add -> { startActivity(Intent(activity,ContactAddFriendActivity::class.java)) }
                else -> { }
            }
            true
        }

        rv_contacts.adapter = contactsAdapter
        rv_contacts.layoutManager = LinearLayoutManager(context) as RecyclerView.LayoutManager?
        srl_contacts.setOnRefreshListener { getMembers(true) }
    }

    private fun updateNewRequestBadge() {
        if(AddRequestManager.instance.hasUnreadRequests()){
            item_new_request.iv_tips.visibility = View.VISIBLE
        }else{
            item_new_request.iv_tips.visibility = View.GONE
        }
    }

    private fun initData() { getMembers(false) }

    private fun getMembers(isforce: Boolean) {
        ContactsManager.fetchContacts(isforce, object : FindCallback<TacitUser>() {
            override fun done(list: List<TacitUser>, e: AVException?) {
                if(e == null){
                    contacts = list as ArrayList<TacitUser>
                    srl_contacts.isRefreshing = false
                    contactsAdapter!!.setUserList(list)
                    contactsAdapter!!.notifyDataSetChanged()
                }else{
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateNewRequestBadge()
        getMembers(false)
    }
}