package cn.odinaris.tacitchat.message

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import cn.leancloud.chatkit.utils.LCIMConstants
import cn.odinaris.tacitchat.R
import com.squareup.picasso.Picasso
import java.io.File
import kotlinx.android.synthetic.main.act_image.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.widget.Toast
import cn.odinaris.tacitchat.utils.CodeUtils
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


/**
 * 点击图片显示大图
 */


class ImageActivity : Activity() {
    var url = ""
    var bmp: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.act_image)
        val intent = this.intent
        val path = intent.getStringExtra(LCIMConstants.IMAGE_LOCAL_PATH)
        url = intent.getStringExtra(LCIMConstants.IMAGE_URL)
        //Bitmap解码配置
        val options = BitmapFactory.Options()
        options.inMutable = true
        options.inSampleSize = 1
        options.inScaled = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        if (TextUtils.isEmpty(path)) {
            Log.e("url", url)
            bmp = getBitmap(url)
            iv_image.setImageBitmap(bmp!!)
            //Picasso.with(this).load(url).into(iv_image)
        } else {
            Log.e("path", path)

            bmp = BitmapFactory.decodeFile(path,options)
            Picasso.with(this).load(File(path)).into(this.iv_image)
        }
        tv_extract.setOnClickListener {
            if (bmp != null) {
                // Todo 提取信息
                val width = bmp!!.width
                val sb = StringBuilder()
                for (i in 0..15) {
                    if (Color.red(bmp!!.getPixel(i, 0)) % 2 == 0) {
                        sb.append('0')
                    } else {
                        sb.append('1')
                    }
                }
                val secretInfo = StringBuilder()
                val size = CodeUtils.BinStr2Int(sb.toString())      //嵌入信息长度
                val rows = (size + 16) / width
                for (r in 0..rows - 1) {
                    (0..width - 1)
                            .filterNot { r == 0 && it in 0..15 }
                            .forEach {
                                if (Color.red(bmp!!.getPixel(it, r)) % 2 == 0) {
                                    secretInfo.append('0')
                                } else secretInfo.append('1')
                            }
                }
                var restBitsLength :Int = 0
                if(rows == 0){
                    restBitsLength = size  - rows * width
                }else{
                    restBitsLength = size + 16 - rows * width
                }
                if(restBitsLength > 0){
                    if(rows > 0){
                        (0..restBitsLength-1)
                                .forEach {
                                    if(Color.red(bmp!!.getPixel(it,rows)) % 2 == 0) {
                                        secretInfo.append('0')
                                    } else secretInfo.append('1') }
                    }else{
                        (16..restBitsLength+15)
                                .forEach {
                                    if(Color.red(bmp!!.getPixel(it,rows)) % 2 == 0) {
                                        secretInfo.append('0')
                                    } else secretInfo.append('1') }
                    }
                }
                Log.e("extraction", secretInfo.toString())
                val message = CodeUtils.BinStr2Str(secretInfo.toString())
                Toast.makeText(applicationContext, "秘密信息为:" + message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(IOException::class)
    fun getBitmap(path: String): Bitmap? {
        try {
            val url = URL(path)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.requestMethod = "GET"
            if (200 === conn.responseCode) {
                val inputStream = conn.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                return bitmap
            }
        } catch (e: IOException) { e.printStackTrace() }
        return null
    }
}
