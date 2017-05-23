package cn.odinaris.tacitchat.message.viewholder;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.leancloud.chatkit.LCChatMessageInterface;
import cn.leancloud.chatkit.cache.LCIMConversationItemCache;
import cn.leancloud.chatkit.event.LCIMConversationItemLongClickEvent;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.chatkit.utils.LCIMConversationUtils;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import cn.odinaris.tacitchat.message.ConversationActivity;
import de.greenrobot.event.EventBus;

/**
 * 会话 item 对应的 holder
 */
public class LCIMConversationItemHolder extends BaseViewHolder {
  ImageView avatarView;
  TextView unreadView;
  TextView messageView;
  TextView timeView;
  TextView nameView;
  RelativeLayout avatarLayout;
  LinearLayout contentLayout;
  public static ViewHolderCreator HOLDER_CREATOR = new ViewHolderCreator<LCIMConversationItemHolder>() {
    public LCIMConversationItemHolder createByViewGroupAndType(ViewGroup parent, int viewType) {
      return new LCIMConversationItemHolder(parent);
    }
  };

  public LCIMConversationItemHolder(ViewGroup root) {
    super(root.getContext(), root, cn.leancloud.chatkit.R.layout.lcim_conversation_item);
    this.initView();
  }

  public void initView() {
    this.avatarView = (ImageView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.conversation_item_iv_avatar);
    this.nameView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.conversation_item_tv_name);
    this.timeView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.conversation_item_tv_time);
    this.unreadView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.conversation_item_tv_unread);
    this.messageView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.conversation_item_tv_message);
    this.avatarLayout = (RelativeLayout)this.itemView.findViewById(cn.leancloud.chatkit.R.id.conversation_item_layout_avatar);
    this.contentLayout = (LinearLayout)this.itemView.findViewById(cn.leancloud.chatkit.R.id.conversation_item_layout_content);
  }

  public void bindData(Object o) {
    this.reset();
    final AVIMConversation conversation = (AVIMConversation)o;
    if(null != conversation) {
      if(null == conversation.getCreatedAt()) {
        conversation.fetchInfoInBackground(new AVIMConversationCallback() {
          public void done(AVIMException e) {
            if(e != null) {
              LCIMLogUtils.logException(e);
            } else {
              LCIMConversationItemHolder.this.updateName(conversation);
              LCIMConversationItemHolder.this.updateIcon(conversation);
            }

          }
        });
      } else {
        this.updateName(conversation);
        this.updateIcon(conversation);
      }

      this.updateUnreadCount(conversation);
      this.updateLastMessage(conversation.getLastMessage());
      this.itemView.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          LCIMConversationItemHolder.this.onConversationItemClick(conversation);
        }
      });
      this.itemView.setOnLongClickListener(new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
          AlertDialog.Builder builder = new AlertDialog.Builder(LCIMConversationItemHolder.this.getContext());
          builder.setItems(new String[]{"删除该聊天"}, new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              EventBus.getDefault().post(new LCIMConversationItemLongClickEvent(conversation));
            }
          });
          AlertDialog dialog = builder.create();
          dialog.show();
          return false;
        }
      });
    }

  }

  private void reset() {
    this.avatarView.setImageResource(0);
    this.nameView.setText("");
    this.timeView.setText("");
    this.messageView.setText("");
    this.unreadView.setVisibility(View.GONE);
  }

  private void updateName(AVIMConversation conversation) {
    LCIMConversationUtils.getConversationName(conversation, new AVCallback<String>() {
      protected void internalDone0(String s, AVException e) {
        if(null != e) {
          LCIMLogUtils.logException(e);
        } else {
          LCIMConversationItemHolder.this.nameView.setText(s);
        }

      }
    });
  }

  private void updateIcon(AVIMConversation conversation) {
    if(null != conversation) {
      if(!conversation.isTransient() && conversation.getMembers().size() <= 2) {
        LCIMConversationUtils.getConversationPeerIcon(conversation, new AVCallback<String>() {
          protected void internalDone0(String s, AVException e) {
            if(null != e) {
              LCIMLogUtils.logException(e);
            }

            if(!TextUtils.isEmpty(s)) {
              Picasso.with(LCIMConversationItemHolder.this.getContext()).load(s).placeholder(cn.leancloud.chatkit.R.drawable.lcim_default_avatar_icon).into(LCIMConversationItemHolder.this.avatarView);
            } else {
              LCIMConversationItemHolder.this.avatarView.setImageResource(cn.leancloud.chatkit.R.drawable.lcim_default_avatar_icon);
            }

          }
        });
      } else {
        this.avatarView.setImageResource(cn.leancloud.chatkit.R.drawable.lcim_group_icon);
      }
    }

  }

  private void updateUnreadCount(AVIMConversation conversation) {
    int num = LCIMConversationItemCache.getInstance().getUnreadCount(conversation.getConversationId());
    this.unreadView.setText(num + "");
    this.unreadView.setVisibility(num > 0?View.VISIBLE:View.GONE);
  }

  private void updateLastMessage(AVIMMessage message) {
    if(null != message) {
      Date date = new Date(message.getTimestamp());
      SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
      this.timeView.setText(format.format(date));
      this.messageView.setText(getMessageeShorthand(this.getContext(), message));
    }

  }

  private void onConversationItemClick(AVIMConversation conversation) {
    try {
      Intent intent = new Intent(getContext(), ConversationActivity.class);
      intent.putExtra(LCIMConstants.CONVERSATION_ID, conversation.getConversationId());
      this.getContext().startActivity(intent);
    } catch (ActivityNotFoundException var3) {
      Log.i(LCIMConstants.LCIM_LOG_TAG, var3.toString());
    }

  }

  private static CharSequence getMessageeShorthand(Context context, AVIMMessage message) {
    if(message instanceof AVIMTypedMessage) {
      AVIMReservedMessageType type = AVIMReservedMessageType.getAVIMReservedMessageType(((AVIMTypedMessage)message).getMessageType());
      switch(type.ordinal()) {
        case 1:
          return ((AVIMTextMessage)message).getText();
        case 2:
          return context.getString(cn.leancloud.chatkit.R.string.lcim_message_shorthand_image);
        case 3:
          return context.getString(cn.leancloud.chatkit.R.string.lcim_message_shorthand_location);
        case 4:
          return context.getString(cn.leancloud.chatkit.R.string.lcim_message_shorthand_audio);
        default:
          CharSequence shortHand = "";
          if(message instanceof LCChatMessageInterface) {
            LCChatMessageInterface messageInterface = (LCChatMessageInterface)message;
            shortHand = messageInterface.getShorthand();
          }

          if(TextUtils.isEmpty(shortHand)) {
            shortHand = context.getString(cn.leancloud.chatkit.R.string.lcim_message_shorthand_unknown);
          }

          return shortHand;
      }
    } else {
      return message.getContent();
    }
  }
}
