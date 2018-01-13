package tk.germanbot

import com.github.messenger4j.send.MessengerSendClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tk.germanbot.activity.ActivityData
import tk.germanbot.messenger.MessengerGateway
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.StateService

@Configuration
class InMemoryFlowConfig {

    @Bean
    fun msgGateway(@Autowired sendClient: MessengerSendClient): MessageGateway
            = MessengerGateway(sendClient)

    @Bean
    fun stateService(): StateService = InMemoryStateService()

}

class InMemoryStateService : StateService {
    private val states = HashMap<String, List<ActivityData>>()

    override fun getActivityStack(userId: String) = states.getOrElse(userId, { listOf() })

    override fun saveActivityStack(userId: String, stack: List<ActivityData>) {
        states[userId] = stack
    }

}

