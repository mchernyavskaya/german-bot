package tk.germanbot

import com.github.messenger4j.send.MessengerSendClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tk.germanbot.flow.AnswerValidationResult
import tk.germanbot.flow.Correctness
import tk.germanbot.flow.FsmController
import tk.germanbot.flow.FsmFactory
import tk.germanbot.flow.MessageGateway
import tk.germanbot.flow.Quiz
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
            = Optional.ofNullable(states[userId])

    override fun saveState(state: State) {
        states.put(state.stateData.userId, state)
    }

}

class InMemoryQuizService : QuizService {
    private val quizzes = HashMap<String, ArrayList<Quiz>>()

    override fun checkAnswer(userId: String, quizId: String, answer: String): AnswerValidationResult {
        val correctAnswer = getAnswer(userId, quizId)
        return if (answer == correctAnswer)
            AnswerValidationResult(Correctness.CORRECT)
        else
            AnswerValidationResult(Correctness.INCORRECT, correctAnswer)
    }

    override fun getAnswer(userId: String, quizId: String): String {
        return if (quizzes[userId] != null) {
            quizzes[userId]!!
                    .filter { q -> q.id == quizId }
                    .map { q -> q.answer }
                    .first()
        } else {
            "zzz..."
        }
    }

    override fun getQuestion(userId: String): Quiz {
        return if (quizzes[userId] != null) {
            val qq = quizzes[userId]!!
            val idx = Random().nextInt(qq.size)
            qq[idx]
        } else {
            Quiz("-1", "Yes", "Ja")
        }
    }

    override fun saveQuiz(userId: String, question: String, answer: String) {
        quizzes.computeIfAbsent(userId, { ArrayList() })
                .add(Quiz(UUID.randomUUID().toString(), question, answer))
    }
}
