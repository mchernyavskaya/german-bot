package tk.germanbot.activity

import tk.germanbot.service.MessageGateway

interface ActivityData {
    val id: String
    val userId: String
}

abstract class Activity<T: ActivityData> {

    abstract val messageGateway: MessageGateway

    open val helpText = "? - this message"

    open fun onStart(data: T) {}
    open fun onEnd(data: T) {}
    open protected fun onEvent(event: Event, data: T) :Boolean = false
    open fun onSubActivityFinished(data: T, subActivityData: ActivityData) {}

    fun handleEvent(event: Event, data: T) :Boolean {
        if (isTextMessage(event, "?")){
            messageGateway.textMessage(data.userId, helpText)
            return true
        }

        return if (!onEvent(event, data)) {
            messageGateway.textMessage(data.userId, "What?")
            false
        } else {
            true
        }
    }
}
