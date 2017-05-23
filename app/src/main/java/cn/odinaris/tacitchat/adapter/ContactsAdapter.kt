package cn.odinaris.tacitchat.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.user.TacitUser
import cn.odinaris.tacitchat.view.RoundImageView
import com.bumptech.glide.Glide

class ContactsAdapter(val context: Context) : RecyclerView.Adapter<ContactsAdapter.CommonViewHolder>() {
    val HEADER_ITEM_TYPE = -1
    val FOOTER_ITEM_TYPE = -2
    val COMMON_ITEM_TYPE = 1
    var contacts = ArrayList<TacitUser>()
    var headerView: View? = null
    var footerView: View? = null
    var mOnItemlickListener: onItemClickListener? = null

    interface onItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onItemLongClick(view: View, position: Int)
    }

    fun setOnItemClickListener(onItemClickListener: onItemClickListener) {
        this.mOnItemlickListener = onItemClickListener
    }

    fun setUserList(users: List<TacitUser>?){
        contacts.clear()
        if(users != null){
            for(user in users){
                contacts.add(user)
            }
        }
    }

    override fun onBindViewHolder(holder: CommonViewHolder?, position: Int) {
        if(holder is CommonViewHolder){
            Glide.with(context).load(contacts[position].avatarUrl).into(holder.contact_avatar)
            holder.contact_name.text = contacts[position].username
            holder.itemView.setOnClickListener {
                mOnItemlickListener?.onItemClick(holder.itemView,position)
            }
            holder.itemView.setOnLongClickListener{
                mOnItemlickListener?.onItemLongClick(holder.itemView,position)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CommonViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_contact,parent,false)
        return CommonViewHolder(view)
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

    inner class CommonViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        val contact_avatar = itemView.findViewById(R.id.iv_contact_avatar) as RoundImageView
        val contact_name = itemView.findViewById(R.id.tv_contact_name) as TextView

    }
}