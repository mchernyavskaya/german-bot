package tk.germanbot.fsm

import tk.germanbot.flow.event.UserTextMessageEvent

open class Event {
}

fun isTextMessage(event: Event, value: String) =
        event is UserTextMessageEvent
                && event.message != null
                && event.message.trim() == value
