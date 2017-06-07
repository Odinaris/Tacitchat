package cn.odinaris.tacitchat.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.contacts.ContactsFragment
import cn.odinaris.tacitchat.message.ConversationFragment
import cn.odinaris.tacitchat.user.UserFragment
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import kotlinx.android.synthetic.main.act_main.*



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        initView()
        initOnClickListener()
    }

    private fun initView() {
        bnb_navigator
                .addItem(BottomNavigationItem(R.drawable.ic_message,"消息"))
                .addItem(BottomNavigationItem(R.drawable.ic_friends,"联系人"))
                .addItem(BottomNavigationItem(R.drawable.ic_mine,"我"))
                .setFirstSelectedPosition(0).initialise()
        setDefaultFragment()
    }

    private fun setDefaultFragment() {
        val SFM = supportFragmentManager!!
        val transaction = SFM.beginTransaction()!!
        transaction.add(R.id.ll_container, ConversationFragment())
        transaction.commit()
        bnb_navigator.setFirstSelectedPosition(0)
    }

    private fun initOnClickListener() {

        bnb_navigator.setTabSelectedListener(object: BottomNavigationBar.OnTabSelectedListener {
            override fun onTabReselected(position: Int) { }

            override fun onTabUnselected(position: Int) { }

            override fun onTabSelected(position: Int) {
                val SFM = supportFragmentManager!!
                val transaction = SFM.beginTransaction()!!
                hideAllFragments(transaction)
                when(position){
                    0 -> {
                        if(ConversationFragment().isAdded) transaction.show(ConversationFragment())
                        else{
                            transaction.add(R.id.ll_container, ConversationFragment()).show(ConversationFragment())
                        }
                    }
                    1 -> {
                        if(ContactsFragment().isAdded) transaction.show(ContactsFragment())
                        else{
                            transaction.add(R.id.ll_container, ContactsFragment()).show(ContactsFragment())
                        }
                    }
                    2 -> {
                        if(UserFragment().isAdded) transaction.show(UserFragment())
                        else{
                            transaction.add(R.id.ll_container, UserFragment()).show(UserFragment())
                        }
                    }
                }
                transaction.commit()
            }
        })
    }

    //隐藏所有Fragment
    private fun  hideAllFragments(transaction: FragmentTransaction) {
        for (i : Fragment in supportFragmentManager.fragments){ transaction.hide(i) }
    }
}
