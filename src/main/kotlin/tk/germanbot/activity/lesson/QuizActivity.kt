package tk.germanbot.activity.lesson

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.activity.*
import tk.germanbot.service.Correctness
import tk.germanbot.service.HintService
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import java.util.*

data class QuizActivityData(
        override var userId: String = "",
        var quizId: String = "") : ActivityData {
    override var id: String = UUID.randomUUID().toString()
    var result: Correctness = Correctness.INCORRECT
    var isCancelled = false
    var correctAnswers: Set<String> = emptySet()
    var lastHint: String? = null
    var hintCount: Int = 0
}

@Component
class QuizActivity(
        @Autowired val activityManager: ActivityManager,
        @Autowired override val messageGateway: MessageGateway,
        @Autowired val quizService: QuizService,
        @Autowired val hintService: HintService
) : Activity<QuizActivityData>() {
    override val helpText = "Answer the question above or:\n" +
            "#h - get hint, #end - end lesson"

    override fun onStart(data: QuizActivityData) {
        val quiz = quizService.getQuiz(data.quizId)
        if (quiz.answers != null) {
            data.correctAnswers = quiz.answers!!
        }
        messageGateway.textMessage(data.userId, "${quiz.question!!} (#h for hint)")
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

        if (UserCommand.HINT.eq(event.message)) {
            val correctAnswer = data.correctAnswers.iterator().next()
            data.hintCount++
            data.lastHint = hintService.hint(correctAnswer, data.hintCount)
            if (correctAnswer == data.lastHint) {
                messageGateway.textMessage(data.userId, "Here's the answer: $correctAnswer")
                data.result = Correctness.INCORRECT
                activityManager.endActivity(this, data)
            } else {
                messageGateway.textMessage(data.userId, "Hint: ${data.lastHint.toString()}")
            }
            return true
        }

        val valuation = quizService.checkAnswer(data.userId, data.quizId, event.message)
        messageGateway.textMessage(data.userId, valuation.result.getAnswer(valuation.correctAnswer))
        data.result = valuation.result
        activityManager.endActivity(this, data)
        return true
    }
}