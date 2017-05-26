package cn.odinaris.tacitchat.data_hiding

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.Toast
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.utils.CodeUtils
import cn.odinaris.tacitchat.utils.ImageUtils
import kotlinx.android.synthetic.main.act_embed.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class EmbedActivity : AppCompatActivity() {
    val IMAGE_PICK_REQUEST = 1
    var cover: Bitmap? = null
    var stego: Bitmap? = null
    var coverPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.act_embed)
        ll_placeholder.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, IMAGE_PICK_REQUEST)
        }
        btn_embed.setOnClickListener {
            val secretInfo = et_secret_info.text.toString()
            if(ll_placeholder.visibility != View.VISIBLE){
                if(!et_key.text.toString().isEmpty()){
                    if(!et_secret_info.text.toString().isEmpty()){
                        val secretBits = CodeUtils.Str2BinStr(secretInfo)
                        embedSecretInfo(secretBits,cover)
                        val file = File(coverPath + "/stego1.png")
                        val out: FileOutputStream
                        try {
                            out = FileOutputStream(file)
                            if (stego!!.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                                out.flush()
                                out.close()
                            }
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        val uri = Uri.fromFile(File(file.absolutePath))
                        intent.data = uri
                        applicationContext.sendBroadcast(intent)//通知图库更新
                        Toast.makeText(applicationContext,"秘密信息嵌入成功!",Toast.LENGTH_SHORT).show()
                    }
                    else{ Toast.makeText(applicationContext,"请输入秘密信息!",Toast.LENGTH_SHORT).show() }
                }
                else{ Toast.makeText(applicationContext,"请输入密钥!",Toast.LENGTH_SHORT).show() }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data != null){
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == IMAGE_PICK_REQUEST) {
                    showCoverImage(data.data!!)
                }
            }
        }
    }

    private fun showCoverImage(data: Uri) {
        ll_placeholder.visibility = View.GONE
        val file = ImageUtils.switchUri2File(this, data)
        //Bitmap解码配置
        val options = BitmapFactory.Options()
        options.inMutable = true
        options.inSampleSize = 1
        options.inScaled = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        cover = BitmapFactory.decodeFile(file.absolutePath,options)
        coverPath = file.parent
        ll_placeholder.visibility = View.GONE
        iv_cover.setImageBitmap(cover)
        iv_cover.visibility = View.VISIBLE
    }

    private fun embedSecretInfo(secretBits: String, cover: Bitmap?) {
        stego = cover
        val secretLength = Integer.toBinaryString(secretBits.length)
        val sb = StringBuilder(16)
        if(secretLength.length<16){ (0..16-secretLength.length-1).forEach { sb.append("0") } }
        sb.append(secretLength)
        val secretInfo = sb.toString() + secretBits
        val width = cover!!.width
        val height = cover.height
        val rows = secretInfo.length / width
        val restBitsLength = secretInfo.length - rows * width
        for(r in 0..rows-1){
            for(w in 0..width-1){
                val red = Color.red(cover.getPixel(w,r))
                if(secretInfo[(r*width)+w]=='0'){
                    if(red % 2 !=0){
                        stego!!.setPixel(w,r,Color.argb(0xFF,red-1,
                                Color.green(cover.getPixel(w,r)),
                                Color.blue(cover.getPixel(w,r))))
                    }
                }else{
                    if(red % 2 == 0){
                        if(red != 0){
                            stego!!.setPixel(w,r,Color.argb(0xFF,red-1,
                                    Color.green(cover.getPixel(w,r)),
                                    Color.blue(cover.getPixel(w,r))))
                        }else{
                            stego!!.setPixel(w,r,Color.argb(0xFF,1,
                                    Color.green(cover.getPixel(w,r)),
                                    Color.blue(cover.getPixel(w,r))))
                        }
                    }
                }
            }
        }
        //最后一行
        if(restBitsLength != 0){
            for(w in 0..restBitsLength-1){
                val red = Color.red(cover.getPixel(w,rows))
                if(secretInfo[(rows*width)+w]=='0'){
                    if(red % 2 !=0){
                        stego!!.setPixel(w,rows,Color.argb(0xFF,red-1,
                                Color.green(cover.getPixel(w,rows)),
                                Color.blue(cover.getPixel(w,rows))))
                    }
                }else{
                    if(red % 2 == 0){
                        if(red != 0){
                            stego!!.setPixel(w,rows,Color.argb(0xFF,red-1,
                                    Color.green(cover.getPixel(w,rows)),
                                    Color.blue(cover.getPixel(w,rows))))
                        }else{
                            stego!!.setPixel(w,rows,Color.argb(0xFF,1,
                                    Color.green(cover.getPixel(w,rows)),
                                    Color.blue(cover.getPixel(w,rows))))
                        }
                    }
                }
            }
        }
    }
}
