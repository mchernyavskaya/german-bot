package tk.germanbot.activity

open class Event {
}

fun isTextMessage(event: Event, value: String) =
        event is UserTextMessageEvent
                && event.message != null
                && (event.message.trim() == value || event.message.trim().startsWith(value + " "))

class UserTextMessageEvent(
        val userId: String,
        val message: String) : Event()

class UserButtonEvent(
        val userId: String,
        val button: UserCommand) : Event()

class UserAttachmentEvent(
        val userId: String,
        val fileUrl: String) : Event()
