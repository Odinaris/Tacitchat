package cn.odinaris.tacitchat.message;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import cn.odinaris.tacitchat.message.viewholder.BaseViewHolder;
import cn.odinaris.tacitchat.message.viewholder.ConversationItemHolder;

public class ConversationListAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {
    private Class<?> vhClass;
    private List<T> dataList = new ArrayList();

    public ConversationListAdapter(Class<?> vhClass) {
        this.vhClass = vhClass;
    }

    public List<T> getDataList() {
        return this.dataList;
    }

    public void setDataList(List<T> datas) {
        this.dataList.clear();
        if(null != datas) {this.dataList.addAll(datas);}
    }

    public void addDataList(List<T> datas) {
        this.dataList.addAll(datas);
    }

    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationItemHolder(parent);
    }

    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if(position >= 0 && position < this.dataList.size()) {
            holder.bindData(this.dataList.get(position));
        }

    }

    public int getItemCount() {
        return this.dataList.size();
    }
}
