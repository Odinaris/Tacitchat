package cn.odinaris.tacitchat.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.model.ContactItem
import cn.odinaris.tacitchat.user.TacitUser
import com.bumptech.glide.Glide

class SearchListAdapter(val context: Context) : RecyclerView.Adapter<SearchListAdapter.CommonViewHolder>() {
    val HEADER_ITEM_TYPE = -1
    val FOOTER_ITEM_TYPE = -2
    val COMMON_ITEM_TYPE = 1
    var contacts = ArrayList<ContactItem>()
    var headerView: View? = null
    var footerView: View? = null

    fun setUserList(users: List<TacitUser>?){
        if(users != null){
            for(user in users){
                val item = ContactItem()
                item.user = user
                contacts.add(item)
            }
        }

    }

    override fun onBindViewHolder(holder: CommonViewHolder?, position: Int) {
        if(holder is CommonViewHolder){
            Glide.with(context).load(contacts[position].user?.avatarUrl).into(holder.contact_avatar)
            holder.contact_name.text = contacts[position].user?.username

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CommonViewHolder {
        return CommonViewHolder(LayoutInflater.from(context).inflate(R.layout.item_contact,parent,false))
    }

    override fun getItemCount(): Int {
        var itemCount = contacts.size
        if (null != headerView) { ++itemCount }
        if (null != footerView) { ++itemCount }
        return itemCount
    }

    override fun getItemId(position: Int): Long {
        if (null != headerView && 0 == position) { return -1 }
        if (null != footerView && position == itemCount - 1) { return -2 }
        return super.getItemId(position - 1)
    }

    override fun getItemViewType(position: Int): Int {
        if (null != headerView && 0 == position) { return HEADER_ITEM_TYPE }
        if (null != footerView && position == itemCount - 1) { return FOOTER_ITEM_TYPE }
        return COMMON_ITEM_TYPE
    }

    class CommonViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {
        val contact_avatar = itemView.findViewById(R.id.iv_contact_avatar) as ImageView
        val contact_name = itemView.findViewById(R.id.tv_contact_name) as TextView
    }
}