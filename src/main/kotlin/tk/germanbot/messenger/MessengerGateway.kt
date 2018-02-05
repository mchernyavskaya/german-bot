package tk.germanbot.messenger

import com.github.messenger4j.exceptions.MessengerApiException
import com.github.messenger4j.exceptions.MessengerIOException
import com.github.messenger4j.send.MessengerSendClient
import com.github.messenger4j.send.QuickReply
import com.github.messenger4j.send.templates.GenericTemplate
import org.slf4j.LoggerFactory
import tk.germanbot.activity.UserCommand
import tk.germanbot.service.MessageGateway

class MessageButton(
        val caption: String,
        val action: String
) {
    constructor(caption: String, command: UserCommand)
            : this(caption, command.textCommand)
}

class MessengerGateway(val sendClient: MessengerSendClient) : MessageGateway {

    private val logger = LoggerFactory.getLogger(MessengerGateway::class.java)

    override fun textMessage(userId: String, message: String) {
        try {
            sendClient.sendTextMessage(userId, message);
        } catch (e: MessengerApiException) {
            handleSendException(e)
        } catch (e: MessengerIOException) {
            handleSendException(e)
        }
    }

    override fun messageWithEndButton(userId: String, message: String) {
        val quickReplies = QuickReply.newListBuilder()
                .addTextQuickReply("End", UserCommand.END.name).toList()
                .build()

        try {
            this.sendClient.sendTextMessage(userId, message, quickReplies)
        } catch (e: MessengerApiException) {
            handleSendException(e)
        } catch (e: MessengerIOException) {
            handleSendException(e)
        }
    }

    override fun messageWithButtons(userId: String, message: String, buttons: List<MessageButton>) {
        val quickReplies = QuickReply.newListBuilder();
        buttons.forEach { button ->
            quickReplies.addTextQuickReply(button.caption, button.action).toList()
        }

        try {
            this.sendClient.sendTextMessage(userId, message, quickReplies.build())
        } catch (e: MessengerApiException) {
            handleSendException(e)
        } catch (e: MessengerIOException) {
            handleSendException(e)
        }
    }

    override fun genericMessage(userId: String, title: String, subtitle: String) {

        val genericTemplate = GenericTemplate.newBuilder()
                .addElements()
                .addElement(title)
                .subtitle(subtitle)
                .toList()
                .done()
                .build()

        try {
            this.sendClient.sendTemplate(userId, genericTemplate)
        } catch (e: MessengerApiException) {
            handleSendException(e)
        } catch (e: MessengerIOException) {
            handleSendException(e)
        }
    }

    override fun fileMessage(userId: String, fileUrl: String) {
        try {
            this.sendClient.sendFileAttachment(userId, fileUrl)
        } catch (e: MessengerApiException) {
            handleSendException(e)
        } catch (e: MessengerIOException) {
            handleSendException(e)
        }
    }

    private fun handleSendException(e: Exception) {
        logger.error("Unable to send message.", e)
    }

}

