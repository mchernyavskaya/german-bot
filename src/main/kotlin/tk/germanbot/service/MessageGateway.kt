package tk.germanbot.service

import tk.germanbot.messenger.MessageButton

interface MessageGateway {
    fun textMessage(userId: String, message: String)
    fun messageWithEndButton(userId: String, message: String)
    fun genericMessage(userId: String, title: String, subtitle: String)
    fun fileMessage(userId: String, fileUrl: String)
    fun messageWithButtons(userId: String, message: String, buttons: List<MessageButton>)
}