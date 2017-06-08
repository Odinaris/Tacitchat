package cn.odinaris.tacitchat.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 *  SectionAdapter
 */
class SectionAdapter(val fm: FragmentManager, val fragments: List<Fragment>) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment { return fragments[position] }

    override fun getCount(): Int { return fragments.size }
}