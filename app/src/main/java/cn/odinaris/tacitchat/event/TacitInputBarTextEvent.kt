package cn.odinaris.tacitchat.event


class TacitInputBarTextEvent(action: Int, content: String, tag: Any) : TacitInputBarEvent(action,tag) {
    var sendContent: String = content
    init {
        super.eventAction = action
        super.tag = tag
    }
}