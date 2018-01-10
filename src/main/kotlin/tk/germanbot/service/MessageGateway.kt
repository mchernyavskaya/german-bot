package tk.germanbot.service

interface MessageGateway {
    fun textMessage(userId: String, message: String)
    fun messageWithEndButton(userId: String, message: String)
}