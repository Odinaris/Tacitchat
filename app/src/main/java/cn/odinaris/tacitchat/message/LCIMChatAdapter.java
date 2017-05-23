package cn.odinaris.tacitchat.message;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.leancloud.chatkit.LCChatKit;
import cn.odinaris.tacitchat.message.viewholder.ChatItemAudioHolder;
import cn.odinaris.tacitchat.message.viewholder.ChatItemHolder;
import cn.odinaris.tacitchat.message.viewholder.ChatItemImageHolder;
import cn.odinaris.tacitchat.message.viewholder.ChatItemLocationHolder;
import cn.odinaris.tacitchat.message.viewholder.ChatItemTextHolder;
import cn.odinaris.tacitchat.message.viewholder.BaseViewHolder;

public class LCIMChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private final int ITEM_LEFT = 100;
  private final int ITEM_LEFT_TEXT = 101;
  private final int ITEM_LEFT_IMAGE = 102;
  private final int ITEM_LEFT_AUDIO = 103;
  private final int ITEM_LEFT_LOCATION = 104;
  private final int ITEM_RIGHT = 200;
  private final int ITEM_RIGHT_TEXT = 201;
  private final int ITEM_RIGHT_IMAGE = 202;
  private final int ITEM_RIGHT_AUDIO = 203;
  private final int ITEM_RIGHT_LOCATION = 204;
  private final int ITEM_UNKNOWN = 300;
  private static final long TIME_INTERVAL = 180000L;
  private boolean isShowUserName = true;
  protected List<AVIMMessage> messageList = new ArrayList();

  public LCIMChatAdapter() {
  }

  public void setMessageList(List<AVIMMessage> messages) {
    this.messageList.clear();
    if(null != messages) {
      this.messageList.addAll(messages);
    }

  }

  public void addMessageList(List<AVIMMessage> messages) {
    this.messageList.addAll(0, messages);
  }

  public void addMessage(AVIMMessage message) {
    this.messageList.addAll(Arrays.asList(message));
  }

  public AVIMMessage getFirstMessage() {
    return null != this.messageList && this.messageList.size() > 0?(AVIMMessage)this.messageList.get(0):null;
  }

  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch(viewType) {
      case 100:
      case 101:
        return new ChatItemTextHolder(parent.getContext(), parent, true);
      case 102:
        return new ChatItemImageHolder(parent.getContext(), parent, true);
      case 103:
        return new ChatItemAudioHolder(parent.getContext(), parent, true);
      case 104:
        return new ChatItemLocationHolder(parent.getContext(), parent, true);
      case 200:
      case 201:
        return new ChatItemTextHolder(parent.getContext(), parent, false);
      case 202:
        return new ChatItemImageHolder(parent.getContext(), parent, false);
      case 203:
        return new ChatItemAudioHolder(parent.getContext(), parent, false);
      case 204:
        return new ChatItemLocationHolder(parent.getContext(), parent, false);
      default:
        return new ChatItemTextHolder(parent.getContext(), parent, true);
    }
  }

  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    ((BaseViewHolder)holder).bindData(this.messageList.get(position));
    if(holder instanceof ChatItemHolder) {
      ((ChatItemHolder)holder).showTimeView(this.shouldShowTime(position));
      ((ChatItemHolder)holder).showUserName(this.isShowUserName);
    }

  }

  public int getItemViewType(int position) {
    AVIMMessage message = (AVIMMessage)this.messageList.get(position);
    if(null != message && message instanceof AVIMTypedMessage) {
      AVIMTypedMessage typedMessage = (AVIMTypedMessage)message;
      boolean isMe = this.fromMe(typedMessage);
      return typedMessage.getMessageType() == AVIMReservedMessageType.TextMessageType.getType()?(isMe?201:101):(typedMessage.getMessageType() == AVIMReservedMessageType.AudioMessageType.getType()?(isMe?203:103):(typedMessage.getMessageType() == AVIMReservedMessageType.ImageMessageType.getType()?(isMe?202:102):(typedMessage.getMessageType() == AVIMReservedMessageType.LocationMessageType.getType()?(isMe?204:104):(isMe?200:100))));
    } else {
      return 300;
    }
  }

  public int getItemCount() {
    return this.messageList.size();
  }

  private boolean shouldShowTime(int position) {
    if(position == 0) {
      return true;
    } else {
      long lastTime = ((AVIMMessage)this.messageList.get(position - 1)).getTimestamp();
      long curTime = ((AVIMMessage)this.messageList.get(position)).getTimestamp();
      return curTime - lastTime > 180000L;
    }
  }

  public void showUserName(boolean isShow) {
    this.isShowUserName = isShow;
  }

  public void resetRecycledViewPoolSize(RecyclerView recyclerView) {
    recyclerView.getRecycledViewPool().setMaxRecycledViews(101, 25);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(102, 10);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(103, 15);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(104, 10);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(201, 25);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(202, 10);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(203, 15);
    recyclerView.getRecycledViewPool().setMaxRecycledViews(204, 10);
  }

  protected boolean fromMe(AVIMTypedMessage msg) {
    String selfId = LCChatKit.getInstance().getCurrentUserId();
    return msg.getFrom() != null && msg.getFrom().equals(selfId);
  }
}