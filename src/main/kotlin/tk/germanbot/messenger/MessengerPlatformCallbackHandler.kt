package tk.germanbot.messenger

import com.github.messenger4j.MessengerPlatform
import com.github.messenger4j.MessengerPlatform.CHALLENGE_REQUEST_PARAM_NAME
import com.github.messenger4j.MessengerPlatform.MODE_REQUEST_PARAM_NAME
import com.github.messenger4j.MessengerPlatform.SIGNATURE_HEADER_NAME
import com.github.messenger4j.MessengerPlatform.VERIFY_TOKEN_REQUEST_PARAM_NAME
import com.github.messenger4j.exceptions.MessengerVerificationException
import com.github.messenger4j.receive.MessengerReceiveClient
import com.github.messenger4j.receive.events.AttachmentMessageEvent
import com.github.messenger4j.receive.events.EchoMessageEvent
import com.github.messenger4j.receive.events.FallbackEvent
import com.github.messenger4j.receive.events.MessageDeliveredEvent
import com.github.messenger4j.receive.events.MessageReadEvent
import com.github.messenger4j.receive.events.QuickReplyMessageEvent
import com.github.messenger4j.receive.events.TextMessageEvent
import com.github.messenger4j.receive.handlers.AttachmentMessageEventHandler
import com.github.messenger4j.receive.handlers.EchoMessageEventHandler
import com.github.messenger4j.receive.handlers.FallbackEventHandler
import com.github.messenger4j.receive.handlers.MessageDeliveredEventHandler
import com.github.messenger4j.receive.handlers.MessageReadEventHandler
import com.github.messenger4j.receive.handlers.QuickReplyMessageEventHandler
import com.github.messenger4j.receive.handlers.TextMessageEventHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tk.germanbot.MessengerProperties
import tk.germanbot.activity.EventDispatcher
import tk.germanbot.activity.UserAttachmentEvent
import tk.germanbot.activity.UserButtonEvent
import tk.germanbot.activity.UserCommand
import tk.germanbot.activity.UserTextMessageEvent

@RestController
@RequestMapping("/webhook")
class MessengerPlatformCallbackHandler(
        @Autowired private val props: MessengerProperties,
        @Autowired private val eventDispatcher: EventDispatcher) {

    companion object {
        private val RESOURCE_URL = "https://raw.githubusercontent.com/fbsamples/messenger-platform-samples/master/node/public"
        private val logger = LoggerFactory.getLogger(MessengerPlatformCallbackHandler::class.java)
    }

    private val receiveClient: MessengerReceiveClient

    init {

        logger.debug("Initializing MessengerReceiveClient - appSecret: {} | verifyToken: {}", props.appSecret, props.verifyToken)
        this.receiveClient = MessengerPlatform.newReceiveClientBuilder(props.appSecret, props.verifyToken)
                .onTextMessageEvent(newTextMessageEventHandler())
                .onQuickReplyMessageEvent(newQuickReplyMessageEventHandler())
                .onAttachmentMessageEvent(newAttachmentMessageEventHandler())
//                .onPostbackEvent(newPostbackEventHandler())
//                .onAccountLinkingEvent(newAccountLinkingEventHandler())
//                .onOptInEvent(newOptInEventHandler())
                .onEchoMessageEvent(newEchoMessageEventHandler())
                .onMessageDeliveredEvent(newMessageDeliveredEventHandler())
                .onMessageReadEvent(newMessageReadEventHandler())
                .fallbackEventHandler(newFallbackEventHandler())
                .build()
    }

    /**
     * Webhook verification endpoint.
     *
     * The passed verification token (as query parameter) must match the configured verification token.
     * In case this is true, the passed challenge string must be returned by this endpoint.
     */
    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun verifyWebhook(@RequestParam(MODE_REQUEST_PARAM_NAME) mode: String,
                      @RequestParam(VERIFY_TOKEN_REQUEST_PARAM_NAME) verifyToken: String,
                      @RequestParam(CHALLENGE_REQUEST_PARAM_NAME) challenge: String): ResponseEntity<String> {

        logger.debug("Received Webhook verification request - mode: {} | verifyToken: {} | challenge: {}", mode,
                verifyToken, challenge)
        return try {
            ResponseEntity.ok(this.receiveClient.verifyWebhook(mode, verifyToken, challenge))
        } catch (e: MessengerVerificationException) {
            logger.warn("Webhook verification failed: {}", e.message)
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        }

    }

    /**
     * Callback endpoint responsible for processing the inbound messages and event.
     */
    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun handleCallback(@RequestBody payload: String,
                       @RequestHeader(SIGNATURE_HEADER_NAME) signature: String): ResponseEntity<Void> {

        logger.debug("Received Messenger Platform callback - payload: {} | signature: {}", payload, signature)
        return try {
            this.receiveClient.processCallbackPayload(payload, signature)
            logger.debug("Processed callback payload successfully")
            ResponseEntity.status(HttpStatus.OK).build()
        } catch (e: MessengerVerificationException) {
            logger.warn("Processing of callback payload failed: {}", e.message)
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

    }

    private fun newTextMessageEventHandler(): TextMessageEventHandler {
        fun handle(handler: (TextMessageEvent) -> Unit) = TextMessageEventHandler { event -> handler(event) }

        return handle({ event ->
            logger.debug("Received TextMessageEvent: {}", event)

            logger.info("Received userText '{}' with text '{}' from user '{}' at '{}'",
                    event.mid, event.text, event.sender.id, event.timestamp)

            eventDispatcher.handleEvent(event.sender.id, UserTextMessageEvent(event.sender.id, event.text))
        })
    }

    private fun newQuickReplyMessageEventHandler(): QuickReplyMessageEventHandler {
        fun handle(handler: (QuickReplyMessageEvent) -> Unit) = QuickReplyMessageEventHandler { event -> handler(event) }
        return handle({ event ->
            logger.debug("Received QuickReplyMessageEvent: {}", event)

            val quickReplyPayload = event.quickReply.payload

            logger.info("Received quick reply for userText '{}' with payload '{}'", event.mid, quickReplyPayload)

            UserCommand.parse(quickReplyPayload).ifPresent { button ->
                eventDispatcher.handleEvent(event.sender.id, UserButtonEvent(event.sender.id, button))
            }
        })
    }

    private fun newAttachmentMessageEventHandler(): AttachmentMessageEventHandler {
        fun handle(handler: (AttachmentMessageEvent) -> Unit) = AttachmentMessageEventHandler { event -> handler(event) }
        return handle({ event ->
            logger.debug("Received AttachmentMessageEvent: {}", event)

            logger.info("Received message with attachments from user '{}' at '{}':", event.sender.id, event.timestamp)

            event.attachments.forEach { attach ->

                if (attach.payload.isBinaryPayload) {
                    val url = attach.payload.asBinaryPayload().url
                    eventDispatcher.handleEvent(event.sender.id, UserAttachmentEvent(event.sender.id, url))
                } else if (attach.payload.isLocationPayload) {
                    val url = attach.payload.asLocationPayload().coordinates.toString()
                    eventDispatcher.handleEvent(event.sender.id, UserAttachmentEvent(event.sender.id, url))
                }
            }
        })
    }

//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendImageMessage(recipientId: String) {
//        this.sendClient.sendImageAttachment(recipientId, RESOURCE_URL + "/assets/rift.png")
//    }
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendGifMessage(recipientId: String) {
//        this.sendClient.sendImageAttachment(recipientId, "https://media.giphy.com/media/11sBLVxNs7v6WA/giphy.gif")
//    }
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendAudioMessage(recipientId: String) {
//        this.sendClient.sendAudioAttachment(recipientId, RESOURCE_URL + "/assets/sample.mp3")
//    }
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendVideoMessage(recipientId: String) {
//        this.sendClient.sendVideoAttachment(recipientId, RESOURCE_URL + "/assets/allofus480.mov")
//    }
//
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendButtonMessage(recipientId: String) {
//        val buttons = Button.newListBuilder()
//                .addUrlButton("Open Web URL", "https://www.oculus.com/en-us/rift/").toList()
//                .addPostbackButton("Trigger Postback", "DEVELOPER_DEFINED_PAYLOAD").toList()
//                .addCallButton("Call Phone Number", "+16505551234").toList()
//                .build()
//
//        val buttonTemplate = ButtonTemplate.newBuilder("Tap a button", buttons).build()
//        this.sendClient.sendTemplate(recipientId, buttonTemplate)
//    }
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun genericMessage(recipientId: String) {
//        val riftButtons = Button.newListBuilder()
//                .addUrlButton("Open Web URL", "https://www.oculus.com/en-us/rift/").toList()
//                .addPostbackButton("Call Postback", "Payload for first bubble").toList()
//                .build()
//
//        val touchButtons = Button.newListBuilder()
//                .addUrlButton("Open Web URL", "https://www.oculus.com/en-us/touch/").toList()
//                .addPostbackButton("Call Postback", "Payload for second bubble").toList()
//                .build()
//
//
//        val genericTemplate = GenericTemplate.newBuilder()
//                .addElements()
//                .addElement("rift")
//                .subtitle("Next-generation virtual reality")
//                .itemUrl("https://www.oculus.com/en-us/rift/")
//                .imageUrl(RESOURCE_URL + "/assets/rift.png")
//                .buttons(riftButtons)
//                .toList()
//                .addElement("touch")
//                .subtitle("Your Hands, Now in VR")
//                .itemUrl("https://www.oculus.com/en-us/touch/")
//                .imageUrl(RESOURCE_URL + "/assets/touch.png")
//                .buttons(touchButtons)
//                .toList()
//                .done()
//                .build()
//
//        this.sendClient.sendTemplate(recipientId, genericTemplate)
//    }
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendReceiptMessage(recipientId: String) {
//        val uniqueReceiptId = "order-" + Math.floor(Math.random() * 1000)
//
//        val receiptTemplate = ReceiptTemplate.newBuilder("Peter Chang", uniqueReceiptId, "USD", "Visa 1234")
//                .timestamp(1428444852L)
//                .addElements()
//                .addElement("Oculus Rift", 599.00f)
//                .subtitle("Includes: headset, sensor, remote")
//                .quantity(1)
//                .currency("USD")
//                .imageUrl(RESOURCE_URL + "/assets/riftsq.png")
//                .toList()
//                .addElement("Samsung Gear VR", 99.99f)
//                .subtitle("Frost White")
//                .quantity(1)
//                .currency("USD")
//                .imageUrl(RESOURCE_URL + "/assets/gearvrsq.png")
//                .toList()
//                .done()
//                .addAddress("1 Hacker Way", "Menlo Park", "94025", "CA", "US").done()
//                .addSummary(626.66f)
//                .subtotal(698.99f)
//                .shippingCost(20.00f)
//                .totalTax(57.67f)
//                .done()
//                .addAdjustments()
//                .addAdjustment().name("New Customer Discount").amount(-50f).toList()
//                .addAdjustment().name("$100 Off Coupon").amount(-100f).toList()
//                .done()
//                .build()
//
//        this.sendClient.sendTemplate(recipientId, receiptTemplate)
//    }
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendQuickReply(recipientId: String) {
//        val quickReplies = QuickReply.newListBuilder()
//                .addTextQuickReply("Action", "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_ACTION").toList()
//                .addTextQuickReply("Comedy", "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_COMEDY").toList()
//                .addTextQuickReply("Drama", "DEVELOPER_DEFINED_PAYLOAD_FOR_PICKING_DRAMA").toList()
//                .addLocationQuickReply().toList()
//                .build()
//
//        this.sendClient.sendTextMessage(recipientId, "What's your favorite movie genre?", quickReplies)
//    }
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendReadReceipt(recipientId: String) {
//        this.sendClient.sendSenderAction(recipientId, SenderAction.MARK_SEEN)
//    }
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendTypingOn(recipientId: String) {
//        this.sendClient.sendSenderAction(recipientId, SenderAction.TYPING_ON)
//    }
//
//    @Throws(MessengerApiException::class, MessengerIOException::class)
//    private fun sendTypingOff(recipientId: String) {
//        this.sendClient.sendSenderAction(recipientId, SenderAction.TYPING_OFF)
//    }
//
//    private fun sendAccountLinking(recipientId: String) {
//        // supported by messenger4j since 0.7.0
//        // sample implementation coming soon
//    }

//    private fun newPostbackEventHandler(): PostbackEventHandler {
//        fun handle(handler: (PostbackEvent) -> Unit) = PostbackEventHandler { event -> handler(event) }
//        return handle({ event ->
//            logger.debug("Received PostbackEvent: {}", event)
//
//            val senderId = event.getSender().getId()
//            val recipientId = event.getRecipient().getId()
//            val payload = event.getPayload()
//            val timestamp = event.getTimestamp()
//
//            logger.info("Received postback for user '{}' and page '{}' with payload '{}' at '{}'",
//                    senderId, recipientId, payload, timestamp)
//
//            sendTextMessage(senderId, "Postback called")
//        })
//    }
//
//    private fun newAccountLinkingEventHandler(): AccountLinkingEventHandler {
//        fun handle(handler: (AccountLinkingEvent) -> Unit) = AccountLinkingEventHandler { event -> handler(event) }
//        return handle({ event ->
//            logger.debug("Received AccountLinkingEvent: {}", event)
//
//            val senderId = event.getSender().getId()
//            val accountLinkingStatus = event.getStatus()
//            val authorizationCode = event.getAuthorizationCode()
//
//            logger.info("Received account linking event for user '{}' with status '{}' and auth code '{}'",
//                    senderId, accountLinkingStatus, authorizationCode)
//        })
//    }
//
//    private fun newOptInEventHandler(): OptInEventHandler {
//        fun handle(handler: (OptInEvent) -> Unit) = OptInEventHandler { event -> handler(event) }
//        return handle({ event ->
//            logger.debug("Received OptInEvent: {}", event)
//
//            val senderId = event.getSender().getId()
//            val recipientId = event.getRecipient().getId()
//            val passThroughParam = event.getRef()
//            val timestamp = event.getTimestamp()
//
//            logger.info("Received authentication for user '{}' and page '{}' with pass through param '{}' at '{}'",
//                    senderId, recipientId, passThroughParam, timestamp)
//
//            sendTextMessage(senderId, "Authentication successful")
//        })
//    }

    private fun newEchoMessageEventHandler(): EchoMessageEventHandler {
        fun handle(handler: (EchoMessageEvent) -> Unit) = EchoMessageEventHandler { event -> handler(event) }
        return handle({ event ->
            logger.debug("Received EchoMessageEvent: {}", event)

            val messageId = event.getMid()
            val recipientId = event.getRecipient().getId()
            val senderId = event.getSender().getId()
            val timestamp = event.getTimestamp()

            logger.info("Received echo for userText '{}' that has been sent to recipient '{}' by sender '{}' at '{}'",
                    messageId, recipientId, senderId, timestamp)
        })
    }

    private fun newMessageDeliveredEventHandler(): MessageDeliveredEventHandler {
        fun handle(handler: (MessageDeliveredEvent) -> Unit) = MessageDeliveredEventHandler { event -> handler(event) }
        return handle({ event ->
            logger.debug("Received MessageDeliveredEvent: {}", event)

            val watermark = event.getWatermark()
            val senderId = event.getSender().getId()

            event.mids?.forEach { messageId -> logger.info("Received delivery confirmation for userText '{}'", messageId) }
            logger.info("All messages before '{}' were delivered to user '{}'", watermark, senderId)
        })
    }

    private fun newMessageReadEventHandler(): MessageReadEventHandler {
        fun handle(handler: (MessageReadEvent) -> Unit) = MessageReadEventHandler { event -> handler(event) }
        return handle({ event ->
            logger.debug("Received MessageReadEvent: {}", event)

            val watermark = event.getWatermark()
            val senderId = event.getSender().getId()

            logger.info("All messages before '{}' were read by user '{}'", watermark, senderId)
        })
    }

    /**
     * This handler is called when either the userText is unsupported or when the event handler for the actual event type
     * is not registered. In this showcase all event handlers are registered. Hence only in case of an
     * unsupported userText the fallback event handler is called.
     */
    private fun newFallbackEventHandler(): FallbackEventHandler {
        fun handle(handler: (FallbackEvent) -> Unit) = FallbackEventHandler { event -> handler(event) }
        return handle({ event ->
            logger.debug("Received FallbackEvent: {}", event)

            val senderId = event.getSender().getId()
            logger.info("Received unsupported userText from user '{}'", senderId)
        })
    }

//    private fun sendTextMessage(recipientId: String, text: String) {
//        try {
//            val recipient = Recipient.newBuilder().recipientId(recipientId).build()
//            val notificationType = NotificationType.REGULAR
//            val metadata = "DEVELOPER_DEFINED_METADATA"
//
//            this.sendClient.sendTextMessage(recipient, notificationType, text, metadata)
//        } catch (e: MessengerApiException) {
//            handleSendException(e)
//        } catch (e: MessengerIOException) {
//            handleSendException(e)
//        }
//
//    }

//    private fun handleSendException(e: Exception) {
//        logger.error("Message could not be sent. An unexpected error occurred.", e)
//    }

}
