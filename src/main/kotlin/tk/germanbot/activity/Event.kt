package tk.germanbot.activity

open class Event {
}

fun isTextMessage(event: Event, value: String) =
        event is UserTextMessageEvent
                && event.message != null
                && event.message.trim() == value

class UserTextMessageEvent(
        val userId: String,
        val message: String) : Event()

class UserButtonEvent(
        val userId: String,
        val button: UserCommand) : Event()