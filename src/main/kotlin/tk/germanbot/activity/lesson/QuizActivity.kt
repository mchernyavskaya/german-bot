package tk.germanbot.activity.lesson

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.activity.*
import tk.germanbot.service.Correctness
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import java.util.*

data class QuizActivityData(
        override var userId: String = "",
        var quizId: String = "") : ActivityData {
    override var id: String = UUID.randomUUID().toString()
    var result: Correctness = Correctness.INCORRECT
    var isCancelled = false
}

@Component
class QuizActivity(
        @Autowired val activityManager: ActivityManager,
        @Autowired override val messageGateway: MessageGateway,
        @Autowired val quizService: QuizService
) : Activity<QuizActivityData>() {

    override val helpText = "Answer the question above or:\n" +
            "#end - end lesson"

    override fun onStart(data: QuizActivityData) {
        val quiz = quizService.getQuiz(data.quizId)
        messageGateway.textMessage(data.userId, quiz.question!!)
    }

    override fun onEvent(event: Event, data: QuizActivityData): Boolean {
        if (event !is UserTextMessageEvent) {
            return false
        }

        if (UserCommand.END.eq(event.message)) {
            data.isCancelled = true
            activityManager.endActivity(this, data)
            return true
        }

        val valuation = quizService.checkAnswer(data.quizId, event.message)
        messageGateway.textMessage(data.userId, valuation.result.getAnswer(valuation.correctAnswer))
        data.result = valuation.result
        activityManager.endActivity(this, data)
        return true
    }
}