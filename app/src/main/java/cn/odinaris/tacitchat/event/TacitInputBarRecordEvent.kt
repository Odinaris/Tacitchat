package cn.odinaris.tacitchat.event


class TacitInputBarRecordEvent(action: Int, path: String, duration: Int, tag: Any) : TacitInputBarEvent(action,tag) {
    var audioPath: String = path
    var audioDuration: Int = duration
}