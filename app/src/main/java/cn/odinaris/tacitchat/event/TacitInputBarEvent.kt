package cn.odinaris.tacitchat.event


open class TacitInputBarEvent(action: Int, var tag: Any?) {
    val INPUTBOTTOMBAR_IMAGE_ACTION = 0
    val INPUTBOTTOMBAR_CAMERA_ACTION = 1
    val INPUTBOTTOMBAR_SEND_AUDIO_ACTION = 2
    val INPUTBOTTOMBAR_SEND_TEXT_ACTION = 3
    val INPUTBOTTOMBAR_FILE_ACTION = 4
    val INPUTBOTTOMBAR_FIRE_ACTION = 5
    val INPUTBOTTOMBAR_EMBED_ACTION = 6
    var eventAction: Int = action
}