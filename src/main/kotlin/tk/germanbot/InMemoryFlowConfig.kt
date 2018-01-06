package tk.germanbot

import com.github.messenger4j.send.MessengerSendClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tk.germanbot.activity.ActivityData
import tk.germanbot.service.StateService
import tk.germanbot.service.AnswerValidationResult
import tk.germanbot.service.Correctness
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.Quiz
import tk.germanbot.service.QuizService
import tk.germanbot.messenger.MessengerGateway
import java.util.UUID

@Configuration
class InMemoryFlowConfig {

    @Bean
    fun msgGateway(@Autowired sendClient: MessengerSendClient): MessageGateway
            = MessengerGateway(sendClient)

    @Bean
    fun quizService(): QuizService = InMemoryQuizService()

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

class InMemoryQuizService : QuizService {
    private val quizzes = HashMap<String, Quiz>()

    init {
        quizzes["-1"] = Quiz("-1", "Yes", "Ja")
        quizzes["-2"] = Quiz("-2", "Father", "Vatter")
        quizzes["-3"] = Quiz("-3", "Mother", "Mutter")
    }

    override fun getQuestionIds(userId: String, totalQuestions: Int): List<String> {
        if (userId == "user") {
            return listOf("-1", "-2", "-3")
        }
        return quizzes.values.map { q -> q.id }.take(totalQuestions)
    }

    override fun checkAnswer(quizId: String, answer: String): AnswerValidationResult {
        val correctAnswer = getAnswer(quizId)
        return if (answer == correctAnswer)
            AnswerValidationResult(Correctness.CORRECT)
        else
            AnswerValidationResult(Correctness.INCORRECT, correctAnswer)
    }

    override fun getAnswer(quizId: String): String {
        return if (quizzes[quizId] != null) {
            quizzes[quizId]!!.answer
        } else {
            "zzz..."
        }
    }

    override fun getQuiz(quizId: String): Quiz {
        return if (quizzes.containsKey(quizId)) {
            quizzes[quizId]!!
        } else {
            Quiz("-1", "Yes", "Ja")
        }
    }

    override fun saveQuiz(question: String, answer: String) {
        val id = UUID.randomUUID().toString()
        quizzes.put(id, Quiz(id, question, answer))
    }
}
