package cn.odinaris.tacitchat.message.viewholder;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avos.avoscloud.AVCallback;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.leancloud.chatkit.LCChatKitUser;
import cn.leancloud.chatkit.cache.LCIMProfileCache;
import cn.leancloud.chatkit.event.LCIMMessageResendEvent;
import cn.leancloud.chatkit.utils.LCIMConstants;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/9/17.
 * 聊天 item 的 holder
 */
public class LCIMChatItemHolder extends LCIMCommonViewHolder {
  protected boolean isLeft;
  protected AVIMMessage message;
  protected ImageView avatarView;
  protected TextView timeView;
  protected TextView nameView;
  protected LinearLayout conventLayout;
  protected FrameLayout statusLayout;
  protected ProgressBar progressBar;
  protected TextView statusView;
  protected ImageView errorView;

  public LCIMChatItemHolder(Context context, ViewGroup root, boolean isLeft) {
    super(context, root, isLeft? cn.leancloud.chatkit.R.layout.lcim_chat_item_left_layout: cn.leancloud.chatkit.R.layout.lcim_chat_item_right_layout);
    this.isLeft = isLeft;
    this.initView();
  }

  public void initView() {
    if(this.isLeft) {
      this.avatarView = (ImageView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_iv_avatar);
      this.timeView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_tv_time);
      this.nameView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_tv_name);
      this.conventLayout = (LinearLayout)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_layout_content);
      this.statusLayout = (FrameLayout)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_layout_status);
      this.statusView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_tv_status);
      this.progressBar = (ProgressBar)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_progressbar);
      this.errorView = (ImageView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_left_tv_error);
    } else {
      this.avatarView = (ImageView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_iv_avatar);
      this.timeView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_tv_time);
      this.nameView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_tv_name);
      this.conventLayout = (LinearLayout)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_layout_content);
      this.statusLayout = (FrameLayout)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_layout_status);
      this.progressBar = (ProgressBar)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_progressbar);
      this.errorView = (ImageView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_tv_error);
      this.statusView = (TextView)this.itemView.findViewById(cn.leancloud.chatkit.R.id.chat_right_tv_status);
    }

    this.setAvatarClickEvent();
    this.setResendClickEvent();
  }

  public void bindData(Object o) {
    this.message = (AVIMMessage)o;
    this.timeView.setText(millisecsToDateString(this.message.getTimestamp()));
    this.nameView.setText("");
    this.avatarView.setImageResource(cn.leancloud.chatkit.R.drawable.lcim_default_avatar_icon);
    LCIMProfileCache.getInstance().getCachedUser(this.message.getFrom(), new AVCallback<LCChatKitUser>() {
      protected void internalDone0(LCChatKitUser userProfile, AVException e) {
        if(null != e) {
          LCIMLogUtils.logException(e);
        } else if(null != userProfile) {
          LCIMChatItemHolder.this.nameView.setText(userProfile.getUserName());
          String avatarUrl = userProfile.getAvatarUrl();
          if(!TextUtils.isEmpty(avatarUrl)) {
            Picasso.with(LCIMChatItemHolder.this.getContext()).load(avatarUrl).placeholder(cn.leancloud.chatkit.R.drawable.lcim_default_avatar_icon).into(LCIMChatItemHolder.this.avatarView);
          }
        }

      }
    });
    switch(this.message.getMessageStatus().ordinal()) {
      case 1:
        this.statusLayout.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.GONE);
        this.statusView.setVisibility(View.GONE);
        this.errorView.setVisibility(View.VISIBLE);
        break;
      case 2:
        this.statusLayout.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.GONE);
        this.statusView.setVisibility(View.VISIBLE);
        this.statusView.setVisibility(View.GONE);
        this.errorView.setVisibility(View.GONE);
        break;
      case 3:
        this.statusLayout.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.VISIBLE);
        this.statusView.setVisibility(View.GONE);
        this.errorView.setVisibility(View.GONE);
        break;
      case 4:
      case 5:
        this.statusLayout.setVisibility(View.GONE);
    }

  }

  public void showTimeView(boolean isShow) {
    this.timeView.setVisibility(isShow?View.VISIBLE:View.GONE);
  }

  public void showUserName(boolean isShow) {
    this.nameView.setVisibility(isShow?View.VISIBLE:View.GONE);
  }

  private void setAvatarClickEvent() {
    this.avatarView.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        try {
          Intent intent = new Intent();
          intent.setPackage(LCIMChatItemHolder.this.getContext().getPackageName());
          intent.setAction(LCIMConstants.AVATAR_CLICK_ACTION);
          intent.addCategory("android.intent.category.DEFAULT");
          LCIMChatItemHolder.this.getContext().startActivity(intent);
        } catch (ActivityNotFoundException var3) {
          Log.i(LCIMConstants.LCIM_LOG_TAG, var3.toString());
        }

      }
    });
  }

  private void setResendClickEvent() {
    this.errorView.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        LCIMMessageResendEvent event = new LCIMMessageResendEvent();
        event.message = LCIMChatItemHolder.this.message;
        EventBus.getDefault().post(event);
      }
    });
  }

  private static String millisecsToDateString(long timestamp) {
    SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
    return format.format(new Date(timestamp));
  }
}
