package tk.germanbot.activity

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.service.MessageGateway
import java.util.UUID

data class WelcomeActivityData(
        override var userId: String) : ActivityData {
    var isHelloSaid: Boolean = false
    override var id: String = UUID.randomUUID().toString()
}

@Component
class WelcomeActivity(
        @Autowired val activityManager: ActivityManager,
        @Autowired override val messageGateway: MessageGateway
) : Activity<WelcomeActivityData>() {

    override val helpText = "#q - start quick session (5 questions)"

    override fun onEvent(event: Event, data: WelcomeActivityData): Boolean {
        if (!data.isHelloSaid) {
            data.isHelloSaid = true
            messageGateway.textMessage(data.userId, "Welcome! Type '?' for help")
            return true
        }

        if (isTextMessage(event, "#q")) {
            activityManager.startLessonActivity(data.userId)
            return true
        }

        return false
    }
}

