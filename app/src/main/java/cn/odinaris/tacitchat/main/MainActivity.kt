package cn.odinaris.tacitchat.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.contacts.ContactsFragment
import cn.odinaris.tacitchat.message.ConversationFragment
import cn.odinaris.tacitchat.user.UserFragment
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import kotlinx.android.synthetic.main.act_main.*



class MainActivity : AppCompatActivity() {

    private val fragments = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main)
        initView()
    }

    private fun initView() {
        initBottomNavigationBar()//初始化BottomNavigationBar
        initViewPager()//初始化ViewPager
    }

    //初始化BottomNavigationBar
    private fun initBottomNavigationBar() {
        bnb_navigator
                .addItem(BottomNavigationItem(R.drawable.ic_message,"消息"))
                .addItem(BottomNavigationItem(R.drawable.ic_friends,"联系人"))
                .addItem(BottomNavigationItem(R.drawable.ic_mine,"我"))
                .setFirstSelectedPosition(0).initialise()
        bnb_navigator.setTabSelectedListener(object: BottomNavigationBar.OnTabSelectedListener {
            override fun onTabReselected(position: Int) { }

            override fun onTabUnselected(position: Int) { }

            override fun onTabSelected(position: Int) {
                vp_container.currentItem = position
            }
        })
    }

    //初始化ViewPager
    private fun initViewPager() {
        fragments.add(0,ConversationFragment())
        fragments.add(1,ContactsFragment())
        fragments.add(2,UserFragment())
        vp_container.adapter = SectionAdapter(supportFragmentManager,fragments)
        vp_container.currentItem = 0
        vp_container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                bnb_navigator.selectTab(position)
            }
        })
    }
}
