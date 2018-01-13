package tk.germanbot.activity.lesson

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.activity.Activity
import tk.germanbot.activity.ActivityData
import tk.germanbot.activity.ActivityManager
import tk.germanbot.activity.Event
import tk.germanbot.activity.UserCommand
import tk.germanbot.activity.UserTextMessageEvent
import tk.germanbot.service.Correctness
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import java.util.UUID

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
        when (valuation.result) {
            Correctness.CORRECT -> {
                messageGateway.textMessage(data.userId, "Correct! (y)")
            }
            Correctness.PARTIALLY_CORRECT -> {
                messageGateway.textMessage(data.userId, "Almost! Answer:\n" + valuation.correctAnswer)
            }
            else -> {
                messageGateway.textMessage(data.userId, ":-( not quite. Answer:\n" + valuation.correctAnswer)
            }
        }
        data.result = valuation.result
        activityManager.endActivity(this, data)
        return true
    }
}