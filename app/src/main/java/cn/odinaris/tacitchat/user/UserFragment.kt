package cn.odinaris.tacitchat.user

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.leancloud.chatkit.LCChatKit
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.data_hiding.EmbedActivity
import cn.odinaris.tacitchat.data_hiding.ExtractActivity
import cn.odinaris.tacitchat.login.LoginActivity
import cn.odinaris.tacitchat.service.PushManager
import cn.odinaris.tacitchat.utils.PathUtils
import cn.odinaris.tacitchat.utils.Utils
import com.avos.avoscloud.AVException
import com.avos.avoscloud.SaveCallback
import com.avos.avoscloud.im.v2.AVIMClient
import com.avos.avoscloud.im.v2.AVIMException
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.frg_user.*
import java.io.File

class UserFragment : Fragment() {

    private val IMAGE_PICK_REQUEST = 1
    private val CROP_REQUEST = 2

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view : View = inflater!!.inflate(R.layout.frg_user,container,false)
        return view
    }
    override fun onViewCreated(view: View,savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
    }

    private fun initView() {
        Glide.with(context).load(TacitUser.getCurrentUser().avatarUrl).into(iv_user_avatar)
        tv_username.text = TacitUser.getCurrentUser().username
        btn_log_out.setOnClickListener {
            LCChatKit.getInstance().close(object : AVIMClientCallback() {
                override fun done(avimClient: AVIMClient?, e: AVIMException) {}
            })
            PushManager().unsubscribeCurrentUserChannel()
            TacitUser.logOut()
            Toast.makeText(activity,"注销成功",Toast.LENGTH_SHORT).show()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity.finish()
        }
        btn_update_avatar.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, IMAGE_PICK_REQUEST)
        }
        btn_embed.setOnClickListener { startActivity(Intent(activity,EmbedActivity::class.java)) }
        btn_extract.setOnClickListener { startActivity(Intent(activity,ExtractActivity::class.java)) }
    }

    private fun initData() { PathUtils.ctx = context }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data != null){
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == IMAGE_PICK_REQUEST) {
                    val uri = data.data
                    startImageCrop(uri, 200, 200, CROP_REQUEST)
                } else if (requestCode == CROP_REQUEST) {
                    val path = saveCropAvatar(data)
                    val user = TacitUser.getCurrentUser()
                    //Todo 添加上传头像回调反馈
                    user.saveAvatar(path, object:SaveCallback(){
                        override fun done(p0: AVException?) {
                            Glide.with(context).load(user.avatarUrl).into(iv_user_avatar)
                            Toast.makeText(activity,"头像上传成功!",Toast.LENGTH_SHORT).show()
                        }
                    } )
                }
            }
        }
    }

    fun startImageCrop(uri: Uri, outputX: Int, outputY: Int, requestCode: Int): Uri {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", outputX)
        intent.putExtra("outputY", outputY)
        intent.putExtra("scale", true)
        val outputPath = PathUtils.avatarTmpPath()
        val outputUri = Uri.fromFile(File(outputPath))
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        intent.putExtra("return-data", true)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", false) // face detection
        startActivityForResult(intent, requestCode)
        return outputUri
    }

    private fun saveCropAvatar(data: Intent): String {
        val extras = data.extras
        var path: String? = null
        if (extras != null) {
            val bitmap = extras.getParcelable<Bitmap>("data")
            if (bitmap != null) {
                path = PathUtils.avatarCropPath()
                Utils.saveBitmap(path, bitmap)
                if (!bitmap.isRecycled) { bitmap.recycle() }
            }
        }
        return path!!
    }
}