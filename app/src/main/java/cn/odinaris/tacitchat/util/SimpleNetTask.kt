package cn.odinaris.tacitchat.util

import android.content.Context

abstract class SimpleNetTask : NetAsyncTask {

    protected constructor(cxt: Context) : super(cxt) {}

    protected constructor(cxt: Context, openDialog: Boolean) : super(cxt, openDialog) {}


    override fun onPost(e: Exception?) {
        if (e != null) {
            e.printStackTrace()
            Utils.toast(ctx, e.message!!)
        } else {
            onSucceed()
        }
    }

    @Throws(Exception::class)
    abstract override fun doInBack()

    protected abstract fun onSucceed()
}
