package tk.germanbot

import com.github.messenger4j.send.MessengerSendClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tk.germanbot.flow.FsmController
import tk.germanbot.flow.FsmFactory
import tk.germanbot.flow.MessageGateway
import tk.germanbot.flow.QuizService
import tk.germanbot.flow.StateService
import tk.germanbot.fsm.State
import tk.germanbot.messenger.MessengerGateway
import java.util.*
import kotlin.collections.HashMap

@Configuration
class InMemoryFlowConfig {

    @Bean
    fun msgGateway(@Autowired sendClient: MessengerSendClient): MessageGateway
            = MessengerGateway(sendClient)

    @Bean
    fun quizService(): QuizService = InMemoryQuizService()

    @Bean
    fun stateService(): StateService = InMemoryStateService()

    @Bean
    fun fsmController(@Autowired fsmFactory: FsmFactory, @Autowired stateService: StateService): FsmController
            = FsmController(fsmFactory, stateService)

    @Bean
    fun fsmFactory(@Autowired msgGateway: MessageGateway, @Autowired quizService: QuizService)
            = FsmFactory(msgGateway, quizService)

}

class InMemoryStateService : StateService {

    private val states = HashMap<String, State>()

    override fun getState(userId: String): Optional<State>
            = Optional.ofNullable(states.get(userId))

    override fun saveState(state: State) {
        states.put(state.stateData.userId, state)
    }

}

class InMemoryQuizService : QuizService {

    data class Quiz(
            val question: String,
            val answer: String)

    private val quizzes = HashMap<String, ArrayList<Quiz>>()

    override fun saveQuiz(userId: String, question: String, answer: String) {
        quizzes.computeIfAbsent(userId, { k -> ArrayList() })
                .add(Quiz(question, answer))
    }
}
