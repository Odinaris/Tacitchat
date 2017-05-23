package cn.odinaris.tacitchat.message;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMAudioMessage;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cn.leancloud.chatkit.event.LCIMIMTypeMessageEvent;
import cn.leancloud.chatkit.event.LCIMInputBottomBarEvent;
import cn.leancloud.chatkit.event.LCIMInputBottomBarRecordEvent;
import cn.leancloud.chatkit.event.LCIMInputBottomBarTextEvent;
import cn.leancloud.chatkit.event.LCIMMessageResendEvent;
import cn.leancloud.chatkit.utils.LCIMAudioHelper;
import cn.leancloud.chatkit.utils.LCIMLogUtils;
import cn.leancloud.chatkit.utils.LCIMNotificationUtils;
import cn.leancloud.chatkit.utils.LCIMPathUtils;
import cn.leancloud.chatkit.view.LCIMInputBottomBar;
import cn.odinaris.tacitchat.R;
import de.greenrobot.event.EventBus;

public class LCIMConversationFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    protected AVIMConversation imConversation;
    protected LCIMChatAdapter itemAdapter;
    protected RecyclerView recyclerView;
    protected LinearLayoutManager layoutManager;
    protected SwipeRefreshLayout refreshLayout;
    protected LCIMInputBottomBar inputBottomBar;
    protected String localCameraPath;

    public LCIMConversationFragment() {
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frg_chat, container, false);
        this.recyclerView = (RecyclerView)view.findViewById(R.id.rv_chat);
        this.refreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.srl_chat);
        this.refreshLayout.setEnabled(false);
        this.inputBottomBar = (LCIMInputBottomBar)view.findViewById(R.id.inputbottombar);
        this.layoutManager = new LinearLayoutManager(this.getActivity());
        this.recyclerView.setLayoutManager(this.layoutManager);
        this.itemAdapter = this.getAdpter();
        this.itemAdapter.resetRecycledViewPoolSize(this.recyclerView);
        this.recyclerView.setAdapter(this.itemAdapter);
        EventBus.getDefault().register(this);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                AVIMMessage message = LCIMConversationFragment.this.itemAdapter.getFirstMessage();
                if(null == message) {
                    LCIMConversationFragment.this.refreshLayout.setRefreshing(false);
                } else {
                    LCIMConversationFragment.this.imConversation.queryMessages(message.getMessageId(), message.getTimestamp(), 20, new AVIMMessagesQueryCallback() {
                        public void done(List<AVIMMessage> list, AVIMException e) {
                            LCIMConversationFragment.this.refreshLayout.setRefreshing(false);
                            if(LCIMConversationFragment.this.filterException(e) && null != list && list.size() > 0) {
                                LCIMConversationFragment.this.itemAdapter.addMessageList(list);
                                LCIMConversationFragment.this.itemAdapter.notifyDataSetChanged();
                                LCIMConversationFragment.this.layoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
                            }

                        }
                    });
                }

            }
        });
    }

    protected LCIMChatAdapter getAdpter() {
        return new LCIMChatAdapter();
    }

    public void onResume() {
        super.onResume();
        if(null != this.imConversation) {
            LCIMNotificationUtils.addTag(this.imConversation.getConversationId());
        }

    }

    public void onPause() {
        super.onPause();
        LCIMAudioHelper.getInstance().stopPlayer();
        if(null != this.imConversation) {
            LCIMNotificationUtils.removeTag(this.imConversation.getConversationId());
        }

    }

    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void setConversation(final AVIMConversation conversation) {
        this.imConversation = conversation;
        this.refreshLayout.setEnabled(true);
        this.inputBottomBar.setTag(this.imConversation.getConversationId());
        this.fetchMessages();
        LCIMNotificationUtils.addTag(conversation.getConversationId());
        if(!conversation.isTransient()) {
            if(conversation.getMembers().size() == 0) {
                conversation.fetchInfoInBackground(new AVIMConversationCallback() {
                    public void done(AVIMException e) {
                        if(null != e) {
                            LCIMLogUtils.logException(e);
                        }

                        LCIMConversationFragment.this.itemAdapter.showUserName(conversation.getMembers().size() > 2);
                    }
                });
            } else {
                this.itemAdapter.showUserName(conversation.getMembers().size() > 2);
            }
        } else {
            this.itemAdapter.showUserName(true);
        }

    }

    private void fetchMessages() {
        this.imConversation.queryMessages(new AVIMMessagesQueryCallback() {
            public void done(List<AVIMMessage> messageList, AVIMException e) {
                if(LCIMConversationFragment.this.filterException(e)) {
                    LCIMConversationFragment.this.itemAdapter.setMessageList(messageList);
                    LCIMConversationFragment.this.recyclerView.setAdapter(LCIMConversationFragment.this.itemAdapter);
                    LCIMConversationFragment.this.itemAdapter.notifyDataSetChanged();
                    LCIMConversationFragment.this.scrollToBottom();
                }

            }
        });
    }

    public void onEvent(LCIMInputBottomBarTextEvent textEvent) {
        if(null != this.imConversation && null != textEvent && !TextUtils.isEmpty(textEvent.sendContent) && this.imConversation.getConversationId().equals(textEvent.tag)) {
            this.sendText(textEvent.sendContent);
        }

    }

    public void onEvent(LCIMIMTypeMessageEvent messageEvent) {
        if(null != this.imConversation && null != messageEvent && this.imConversation.getConversationId().equals(messageEvent.conversation.getConversationId())) {
            this.itemAdapter.addMessage(messageEvent.message);
            this.itemAdapter.notifyDataSetChanged();
            this.scrollToBottom();
        }

    }

    public void onEvent(LCIMMessageResendEvent resendEvent) {
        if(null != this.imConversation && null != resendEvent && null != resendEvent.message && this.imConversation.getConversationId().equals(resendEvent.message.getConversationId()) && AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed == resendEvent.message.getMessageStatus() && this.imConversation.getConversationId().equals(resendEvent.message.getConversationId())) {
            this.sendMessage(resendEvent.message, false);
        }

    }

    public void onEvent(LCIMInputBottomBarEvent event) {
        if(null != this.imConversation && null != event && this.imConversation.getConversationId().equals(event.tag)) {
            switch(event.eventAction) {
                case 0:
                    this.dispatchPickPictureIntent();
                    break;
                case 1:
                    this.dispatchTakePictureIntent();
            }
        }

    }

    public void onEvent(LCIMInputBottomBarRecordEvent recordEvent) {
        if(null != this.imConversation && null != recordEvent && !TextUtils.isEmpty(recordEvent.audioPath) && this.imConversation.getConversationId().equals(recordEvent.tag)) {
            this.sendAudio(recordEvent.audioPath);
        }

    }

    private void dispatchTakePictureIntent() {
        this.localCameraPath = LCIMPathUtils.getPicturePathByCurrentTime(this.getContext());
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        Uri imageUri = Uri.fromFile(new File(this.localCameraPath));
        takePictureIntent.putExtra("return-data", false);
        takePictureIntent.putExtra("output", imageUri);
        if(takePictureIntent.resolveActivity(this.getActivity().getPackageManager()) != null) {
            this.startActivityForResult(takePictureIntent, 1);
        }

    }

    private void dispatchPickPictureIntent() {
        Intent photoPickerIntent = new Intent("android.intent.action.PICK", (Uri)null);
        photoPickerIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        this.startActivityForResult(photoPickerIntent, 2);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(-1 == resultCode) {
            switch(requestCode) {
                case 1:
                    this.sendImage(this.localCameraPath);
                    break;
                case 2:
                    this.sendImage(this.getRealPathFromURI(this.getActivity(), data.getData()));
            }
        }

    }

    private void scrollToBottom() {
        this.layoutManager.scrollToPositionWithOffset(this.itemAdapter.getItemCount() - 1, 0);
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        if(contentUri.getScheme().equals("file")) {
            return contentUri.getEncodedPath();
        } else {
            Cursor cursor = null;

            String var5;
            try {
                String[] proj = new String[]{"_data"};
                cursor = context.getContentResolver().query(contentUri, proj, (String)null, (String[])null, (String)null);
                if(null != cursor) {
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    cursor.moveToFirst();
                    String var6 = cursor.getString(column_index);
                    return var6;
                }

                var5 = "";
            } finally {
                if(cursor != null) {
                    cursor.close();
                }

            }

            return var5;
        }
    }

    protected void sendText(String content) {
        AVIMTextMessage message = new AVIMTextMessage();
        message.setText(content);
        this.sendMessage(message);
    }

    protected void sendImage(String imagePath) {
        try {
            this.sendMessage(new AVIMImageMessage(imagePath));
        } catch (IOException var3) {
            LCIMLogUtils.logException(var3);
        }

    }

    protected void sendAudio(String audioPath) {
        try {
            AVIMAudioMessage audioMessage = new AVIMAudioMessage(audioPath);
            this.sendMessage(audioMessage);
        } catch (IOException var3) {
            LCIMLogUtils.logException(var3);
        }

    }

    public void sendMessage(AVIMMessage message) {
        this.sendMessage(message, true);
    }

    public void sendMessage(AVIMMessage message, boolean addToList) {
        if(addToList) {
            this.itemAdapter.addMessage(message);
        }

        this.itemAdapter.notifyDataSetChanged();
        this.scrollToBottom();
        this.imConversation.sendMessage(message, new AVIMConversationCallback() {
            public void done(AVIMException e) {
                LCIMConversationFragment.this.itemAdapter.notifyDataSetChanged();
                if(null != e) {
                    LCIMLogUtils.logException(e);
                }

            }
        });
    }

    private boolean filterException(Exception e) {
        if(null != e) {
            LCIMLogUtils.logException(e);
            Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return null == e;
    }
}
