package cn.odinaris.tacitchat.user;


import android.os.Parcelable;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FollowCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SignUpCallback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class TacitUser extends AVUser implements Parcelable{

    public static final String USERNAME = "username";
    private static final String AVATAR = "avatar";
    private static final String INSTALLATION = "installation";

    public static String getCurrentUserId () {
        TacitUser currentUser = getCurrentUser(TacitUser.class);
        return (null != currentUser ? currentUser.getObjectId() : null);
    }


    public String getAvatarUrl() {
        AVFile avatar = getAVFile(AVATAR);
        if (avatar != null) {
            return avatar.getUrl();
        } else {
            return "http://ac-nswg7wb6.clouddn.com/IpeTmBOu13dIKJpVPjOW32FFVSIq0tTV7VbMS1kj";
        }
    }


    public void saveAvatar(String path, final SaveCallback saveCallback) {
        final AVFile file;
        try {
            file = AVFile.withAbsoluteLocalPath(getUsername(), path);
            put(AVATAR, file);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (null == e) {
                        saveInBackground(saveCallback);
                    } else {
                        if (null != saveCallback) {
                            saveCallback.done(e);
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TacitUser getCurrentUser() {
        return getCurrentUser(TacitUser.class);
    }

    public void updateUserInfo() {
        AVInstallation installation = AVInstallation.getCurrentInstallation();
        if (installation != null) {
            put(INSTALLATION, installation);
            saveInBackground();
        }
    }


    // 根据用户名和密码注册
    public static void signUpByNameAndPwd(String name, String password, SignUpCallback callback) {
        AVUser user = new AVUser();
        user.setUsername(name);
        user.setPassword(password);
        user.signUpInBackground(callback);
    }

    public void removeFriend(String friendId, final SaveCallback saveCallback) {
        unfollowInBackground(friendId, new FollowCallback() {
            @Override
            public void done(AVObject object, AVException e) {
                if (saveCallback != null) {
                    saveCallback.done(e);
                }
            }
        });
    }

    public void findFriendsWithCachePolicy(AVQuery.CachePolicy cachePolicy, FindCallback<TacitUser>
            findCallback) {
        AVQuery<TacitUser> q = null;
        try {
            q = followeeQuery(TacitUser.class);
        } catch (Exception ignored) {
        }
        assert q != null;
        q.setCachePolicy(cachePolicy);
        q.setMaxCacheAge(TimeUnit.MINUTES.toMillis(1));
        q.findInBackground(findCallback);
    }
}