package tk.germanbot.flow

interface MessageGateway {
    fun textMessage(userId: String, message: String)
    fun messageWithCancelButton(userId: String, message: String)
}