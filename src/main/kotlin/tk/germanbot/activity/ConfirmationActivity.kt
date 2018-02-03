package tk.germanbot.activity

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.messenger.MessageButton
import tk.germanbot.service.MessageGateway
import java.util.UUID

data class ConfirmationActivityData(
        override var userId: String = "",
        var message: String = "",
        var buttons: List<MessageButton> = listOf(),
        var userCommand: UserCommand? = null

) : ActivityData {
    override var id: String = UUID.randomUUID().toString()
}

@Component
class ConfirmationActivity(
        @Autowired val activityManager: ActivityManager,
        @Autowired override val messageGateway: MessageGateway
) : Activity<ConfirmationActivityData>() {

    override val helpText: String
        get() = super.helpText

    override fun onStart(data: ConfirmationActivityData) {
        messageGateway.messageWithButtons(data.userId, data.message, data.buttons)
    }

    override fun onEvent(event: Event, data: ConfirmationActivityData): Boolean {
        if (event is UserButtonEvent) {
            data.userCommand = event.button
            activityManager.endActivity(this, data)
            return true
        }

        if (event is UserTextMessageEvent) {
            val command = UserCommand.values().find { command -> command.textCommand == event.message.trim() }
            if (command != null){
                data.userCommand = command
                activityManager.endActivity(this, data)
                return true
            }
        }

        activityManager.endActivity(this, data)
        return false
    }
}
