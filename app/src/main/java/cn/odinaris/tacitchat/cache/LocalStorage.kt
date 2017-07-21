//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.odinaris.tacitchat.cache

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import com.avos.avoscloud.AVCallback
import com.avos.avoscloud.AVException
import com.avos.avoscloud.AVUtils
import java.util.ArrayList
import java.util.Arrays

internal class LocalStorage(context: Context, clientId: String, tableName: String) : SQLiteOpenHelper(context, "LeanCloudChatKit_DB", null as CursorFactory?, 1) {
    private var tableName: String? = null
    private var readDbThread: HandlerThread? = null
    private var readDbHandler: Handler? = null

    init {
        if (TextUtils.isEmpty(tableName)) {
            throw IllegalArgumentException("tableName can not be null")
        } else if (TextUtils.isEmpty(clientId)) {
            throw IllegalArgumentException("clientId can not be null")
        } else {
            val md5ClientId = AVUtils.md5(clientId)
            this.tableName = tableName + "_" + md5ClientId
            this.createTable()
            this.readDbThread = HandlerThread("LCIMLocalStorageReadThread")
            this.readDbThread!!.start()
            this.readDbHandler = Handler(this.readDbThread!!.looper)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s(id TEXT PRIMARY KEY NOT NULL, content TEXT )", *arrayOf<Any>(this.tableName!!)))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (!this.isIgnoreUpgrade) {
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", *arrayOf<Any>(this.tableName!!)))
            this.onCreate(db)
        }

    }

    private fun createTable() {
        this.writableDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s(id TEXT PRIMARY KEY NOT NULL, content TEXT )", *arrayOf<Any>(this.tableName!!)))
    }

    protected val isIgnoreUpgrade: Boolean
        get() = true

    fun getIds(callback: AVCallback<List<String>>?) {
        if (null != callback) {
            this.readDbHandler!!.post { callback.internalDone(this@LocalStorage.idsSync, null as AVException?) }
        }

    }

    fun getData(ids: List<String>?, callback: AVCallback<List<String>>?) {
        if (null != callback) {
            if (null != ids && ids.isNotEmpty()) {
                this.readDbHandler!!.post { callback.internalDone(this@LocalStorage.getDataSync(ids), null as AVException?) }
            } else {
                callback.internalDone(null as List<String>?, null as AVException?)
            }
        }

    }

    fun insertData(idList: List<String>?, valueList: List<String>?) {
        if (null != idList && null != valueList && idList.size == valueList.size) {
            this.readDbHandler!!.post { this@LocalStorage.insertSync(idList, valueList) }
        }

    }

    fun insertData(id: String, value: String) {
        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(value)) {
            this.insertData(Arrays.asList(*arrayOf(id)), Arrays.asList(*arrayOf(value)))
        }

    }

    fun deleteData(ids: List<String>?) {
        if (null != ids && !ids.isEmpty()) {
            this.readDbHandler!!.post { this@LocalStorage.deleteSync(ids) }
        }

    }

    private val idsSync: List<String>
        get() {
            val queryString = "SELECT id FROM " + this.tableName!!
            val database = this.readableDatabase
            val cursor = database.rawQuery(queryString, null as Array<String>?)
            val dataList = ArrayList<String>()

            while (cursor.moveToNext()) {
                dataList.add(cursor.getString(cursor.getColumnIndex("id")))
            }

            cursor.close()
            return dataList
        }

    private fun getDataSync(ids: List<String>?): List<String> {
        var queryString = "SELECT * FROM " + this.tableName!!
        if (null != ids && !ids.isEmpty()) {
            queryString = queryString + " WHERE id in ('" + AVUtils.joinCollection(ids, "','") + "')"
        }

        val database = this.readableDatabase
        val cursor = database.rawQuery(queryString, null as Array<String>?)
        val dataList = ArrayList<String>()

        while (cursor.moveToNext()) {
            dataList.add(cursor.getString(cursor.getColumnIndex("content")))
        }

        cursor.close()
        return dataList
    }

    private fun insertSync(idList: List<String>, valueList: List<String>) {
        val db = this.writableDatabase
        db.beginTransaction()

        for (i in valueList.indices) {
            val values = ContentValues()
            values.put("id", idList[i])
            values.put("content", valueList[0])
            db.insertWithOnConflict(this.tableName, null as String?, values, 5)
        }

        db.setTransactionSuccessful()
        db.endTransaction()
    }

    private fun deleteSync(ids: List<String>?) {
        if (null != ids && !ids.isEmpty()) {
            val queryString = joinListWithApostrophe(ids)
            this.writableDatabase.delete(this.tableName, "id in ($queryString)", null as Array<String>?)
        }

    }

    companion object {
        private val DB_NAME_PREFIX = "LeanCloudChatKit_DB"
        private val TABLE_KEY_ID = "id"
        private val TABLE_KEY_CONTENT = "content"
        private val SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s(id TEXT PRIMARY KEY NOT NULL, content TEXT )"
        private val SQL_DROP_TABLE = "DROP TABLE IF EXISTS %s"
        private val DB_VERSION = 1

        private fun joinListWithApostrophe(strList: List<String>): String {
            var queryString = TextUtils.join("','", strList)
            if (!TextUtils.isEmpty(queryString)) {
                queryString = "'$queryString'"
            }

            return queryString
        }
    }
}
