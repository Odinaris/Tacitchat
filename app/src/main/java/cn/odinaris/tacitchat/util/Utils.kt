package cn.odinaris.tacitchat.util

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v4.app.FragmentActivity
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object Utils {

    fun toast(context: Context, str: String) { Toast.makeText(context, str, Toast.LENGTH_SHORT).show() }

    fun showInfoDialog(cxt: Activity, msg: String, title: String) {
        val builder = getBaseDialogBuilder(cxt)
        builder.setMessage(msg).setPositiveButton("好的", null).setTitle(title).show()
    }

    fun getBaseDialogBuilder(ctx: Activity): AlertDialog.Builder {
        return AlertDialog.Builder(ctx).setTitle("提示")
    }

    fun fixAsyncTaskBug() {
        // android bug
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void): Void? {
                return null
            }
        }.execute()
    }

    fun doubleEqual(a: Double, b: Double): Boolean {
        return Math.abs(a - b) < 1E-8
    }


    fun showSpinnerDialog(activity: Activity): ProgressDialog {
        val dialog = ProgressDialog(activity)
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setCancelable(true)
        dialog.setMessage("加载中...")
        if (!activity.isFinishing) { dialog.show() }
        return dialog
    }

    fun showSpinnerDialog(activity: FragmentActivity): ProgressDialog {
        val dialog = ProgressDialog(activity)
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.setCancelable(true)
        dialog.setMessage("加载中...")
        if (!activity.isFinishing) { dialog.show() }
        return dialog
    }

    fun filterException(e: Exception?): Boolean {
        if (e != null) {
            e.printStackTrace()
            return false
        }
        else { return true }
    }

    fun saveBitmap(filePath: String, bitmap: Bitmap) {
        val file = File(filePath)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                out.flush()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                out!!.close()
            } catch (e: Exception) {
            }
        }
    }
}
