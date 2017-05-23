package cn.odinaris.tacitchat.message;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.leancloud.chatkit.utils.LCIMLogUtils;
import cn.odinaris.tacitchat.message.viewholder.LCIMCommonViewHolder;
import cn.odinaris.tacitchat.message.viewholder.LCIMCommonViewHolder.ViewHolderCreator;

public class LCIMCommonListAdapter<T> extends RecyclerView.Adapter<LCIMCommonViewHolder> {
    private static HashMap<String, ViewHolderCreator> creatorHashMap = new HashMap();
    private Class<?> vhClass;
    protected List<T> dataList = new ArrayList();

    public LCIMCommonListAdapter() {
    }

    public LCIMCommonListAdapter(Class<?> vhClass) {
        this.vhClass = vhClass;
    }

    public List<T> getDataList() {
        return this.dataList;
    }

    public void setDataList(List<T> datas) {
        this.dataList.clear();
        if(null != datas) {
            this.dataList.addAll(datas);
        }

    }

    public void addDataList(List<T> datas) {
        this.dataList.addAll(datas);
    }

    public LCIMCommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(null == this.vhClass) {
            try {
                throw new IllegalArgumentException("please use CommonListAdapter(Class<VH> vhClass)");
            } catch (Exception var7) {
                LCIMLogUtils.logException(var7);
            }
        }

        ViewHolderCreator<?> creator = null;
        if(creatorHashMap.containsKey(this.vhClass.getName())) {
            creator = (ViewHolderCreator)creatorHashMap.get(this.vhClass.getName());
        } else {
            try {
                creator = (ViewHolderCreator)this.vhClass.getField("HOLDER_CREATOR").get((Object)null);
                creatorHashMap.put(this.vhClass.getName(), creator);
            } catch (IllegalAccessException var5) {
                LCIMLogUtils.logException(var5);
            } catch (NoSuchFieldException var6) {
                LCIMLogUtils.logException(var6);
            }
        }

        if(null != creator) {
            return creator.createByViewGroupAndType(parent, viewType);
        } else {
            throw new IllegalArgumentException(this.vhClass.getName() + " HOLDER_CREATOR should be instantiated");
        }
    }

    public void onBindViewHolder(LCIMCommonViewHolder holder, int position) {
        if(position >= 0 && position < this.dataList.size()) {
            holder.bindData(this.dataList.get(position));
        }

    }

    public int getItemCount() {
        return this.dataList.size();
    }
}
