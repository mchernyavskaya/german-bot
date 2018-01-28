package tk.germanbot.service

interface MessageGateway {
    fun textMessage(userId: String, message: String)
    fun messageWithEndButton(userId: String, message: String)
    fun genericMessage(userId: String, title: String, subtitle: String)
    fun fileMessage(userId: String, fileUrl: String)
}