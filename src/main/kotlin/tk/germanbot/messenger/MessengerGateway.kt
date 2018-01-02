package tk.germanbot.messenger

import com.github.messenger4j.exceptions.MessengerApiException
import com.github.messenger4j.exceptions.MessengerIOException
import com.github.messenger4j.send.MessengerSendClient
import com.github.messenger4j.send.QuickReply
import org.slf4j.LoggerFactory
import tk.germanbot.flow.MessageGateway
import tk.germanbot.flow.event.UserCommand

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

    override fun messageWithCancelButton(userId: String, message: String) {
        val quickReplies = QuickReply.newListBuilder()
                .addTextQuickReply("Cancel", UserCommand.CANCEL.name).toList()
                .build()

        try {
            this.sendClient.sendTextMessage(userId, message, quickReplies)
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