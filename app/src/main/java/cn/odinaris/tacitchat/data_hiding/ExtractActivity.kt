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
import kotlinx.android.synthetic.main.act_extract.*

class ExtractActivity : AppCompatActivity() {
    val IMAGE_PICK_REQUEST = 1
    var stego: Bitmap? = null
    var coverPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.act_extract)
        ll_placeholder.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, IMAGE_PICK_REQUEST)
        }
        btn_extract.setOnClickListener {
            if(!et_key.text.toString().isEmpty()){
                val width = stego!!.width
                val height = stego!!.height
                val sb = StringBuilder()
                for(i in 0..15){
                    if(Color.red(stego!!.getPixel(i,0)) % 2 == 0){
                        sb.append('0')
                    }else{
                        sb.append('1')
                    }
                }
                val secretInfo = StringBuilder()
                val size = CodeUtils.BinStr2Int(sb.toString())      //嵌入信息长度
                val rows = (size+16) / width
                for(r in 0..rows-1){
                    (0..width-1)
                            .filterNot { r == 0 && it in 0..15 }
                            .forEach {
                                if(Color.red(stego!!.getPixel(it,r)) % 2 == 0) {
                                    secretInfo.append('0')
                                } else secretInfo.append('1') }
                }
                val restBitsLength = size+16 - rows * width
                if(restBitsLength != 0){
                    (0..restBitsLength-1)
                            .forEach {
                                if(Color.red(stego!!.getPixel(it,rows)) % 2 == 0) {
                                    secretInfo.append('0')
                                } else secretInfo.append('1') }
                }
                val message = CodeUtils.BinStr2Str(secretInfo.toString())
                Toast.makeText(applicationContext,"秘密信息为:"+message,Toast.LENGTH_SHORT).show()
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
        stego = BitmapFactory.decodeFile(file.absolutePath,options)
        coverPath = file.parent
        ll_placeholder.visibility = View.GONE
        iv_stego.setImageBitmap(stego)
        iv_stego.visibility = View.VISIBLE
    }
}
