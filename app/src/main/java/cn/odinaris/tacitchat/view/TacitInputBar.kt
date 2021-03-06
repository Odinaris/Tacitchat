package cn.odinaris.tacitchat.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.*
import cn.leancloud.chatkit.utils.LCIMPathUtils
import cn.leancloud.chatkit.utils.LCIMPathUtils.*
import cn.leancloud.chatkit.view.LCIMRecordButton
import cn.odinaris.tacitchat.R
import cn.odinaris.tacitchat.event.EmbedEvent
import cn.odinaris.tacitchat.event.SelectImageEvent
import cn.odinaris.tacitchat.event.TacitInputBarEvent
import cn.odinaris.tacitchat.event.TacitInputBarTextEvent
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.bar_input.view.*


class TacitInputBar : LinearLayout {
    private var tib_input : EditText  = EditText(context)
    private var tib_send : Button = Button(context)
    private var tib_audio : ImageView = ImageView(context)
    private var tib_image : ImageView = ImageView(context)
    private var tib_camera : ImageView = ImageView(context)
    private var tib_file : ImageView = ImageView(context)
    private var tib_embed : ImageView = ImageView(context)
    private val MIN_INTERVAL_SEND_MESSAGE = 1000

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { initView(context) }

    constructor(context: Context) : super(context) { initView(context) }

    private fun initView(context: Context) {
        View.inflate(context, R.layout.bar_input, this)
        this.tib_input = this.findViewById(R.id.tib_input) as EditText
        this.tib_send = this.findViewById(R.id.tib_send) as Button
        this.tib_audio = this.findViewById(R.id.tib_audio) as ImageView
        this.tib_image = this.findViewById(R.id.tib_image) as ImageView
        this.tib_file = this.findViewById(R.id.tib_file) as ImageView
        this.tib_embed = this.findViewById(R.id.tib_embed) as ImageView
        this.tib_send.background?.alpha = 100
        this.setEditTextChangeListener()
        this.tib_send.setOnClickListener{
            val content = this.tib_input.text.toString()
            if(!content.isEmpty()){
                if(ll_placeholder.visibility == View.VISIBLE){
                    this.tib_input.setText("")
                    EventBus.getDefault().post(TacitInputBarTextEvent(3, content, this@TacitInputBar.tag))
                }else{
                    // 如果已选择载体图像，则嵌入数据
                    EventBus.getDefault().post(EmbedEvent())
                }
            }
            else{ toast("输入信息!") }
        }
        this.tib_image.setOnClickListener { EventBus.getDefault().post(TacitInputBarEvent(0, this@TacitInputBar.tag)) }
        this.tib_audio.setOnClickListener {
//            private fun initRecordBtn() {
//                this.recordBtn.setSavePath(getRecordPathByCurrentTime(this.context))
//                this.recordBtn.setRecordEventListener(object : LCIMRecordButton.RecordEventListener {
//                    override fun onFinishedRecord(audioPath: String, secs: Int) {
//                        EventBus.getDefault().post(LCIMInputBottomBarRecordEvent(4, audioPath, secs, this@LCIMInputBottomBar.getTag()))
//                        this@LCIMInputBottomBar.recordBtn.setSavePath(getRecordPathByCurrentTime(this@LCIMInputBottomBar.getContext()))
//                    }
//
//                    override fun onStartRecord() {}
//                })
//            }
            EventBus.getDefault().post(TacitInputBarEvent(2, this@TacitInputBar.tag))
        }
        this.tib_file.setOnClickListener { EventBus.getDefault().post(TacitInputBarEvent(4, this@TacitInputBar.tag)) }
        this.tib_embed.setOnClickListener { EventBus.getDefault().post(TacitInputBarEvent(6, this@TacitInputBar.tag)) }
    }

    // 设置输入栏状态
    private fun setEditTextChangeListener() {
        this.tib_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                val showSend = charSequence.isNotEmpty()
                tib_send.background?.alpha = if(showSend) 255 else 100 //若输入文字非空则发送图标不透明
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun toast(content : String){ Toast.makeText(context,content,Toast.LENGTH_SHORT).show() }
}