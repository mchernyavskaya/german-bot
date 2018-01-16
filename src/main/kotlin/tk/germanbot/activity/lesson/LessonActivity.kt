package tk.germanbot.activity.lesson

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.activity.Activity
import tk.germanbot.activity.ActivityData
import tk.germanbot.activity.ActivityManager
import tk.germanbot.activity.Event
import tk.germanbot.service.Correctness
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import java.util.*

data class LessonActivityData(
        override var userId: String = "",
        var desiredQuestions: Int = 5
) : ActivityData {
    override var id: String = UUID.randomUUID().toString()
    var answeredQuestions: Int = 0
    var totalQuestions: Int = desiredQuestions
    var questionIds: List<String> = listOf()
    var correctAnswers: List<String> = listOf()
    var wrongAnswers: List<String> = listOf()
}

@Component
class LessonActivity(
        @Autowired val activityManager: ActivityManager,
        @Autowired override val messageGateway: MessageGateway,
        @Autowired val quizService: QuizService
) : Activity<LessonActivityData>() {

    override fun onStart(data: LessonActivityData) {
        data.questionIds = quizService.getQuestionIds(data.userId, data.desiredQuestions)
        data.totalQuestions = data.questionIds.size
        if (data.totalQuestions > 0) {
            messageGateway.textMessage(data.userId, "Lets do ${data.totalQuestions} exercises")
            activityManager.startQuizActivity(data.userId, data.questionIds[0])
        } else {
            messageGateway.textMessage(data.userId, "Sorry, we have no questions yet! Please add some.")
            activityManager.startWelcomeActivity(data.userId)
        }
    }

    override fun onEvent(event: Event, data: LessonActivityData): Boolean {
        return false
    }

    override fun onSubActivityFinished(data: LessonActivityData, subActivityData: ActivityData) {
        if (subActivityData !is QuizActivityData) {
            return
        }

        if (subActivityData.isCancelled) {
            messageGateway.textMessage(data.userId, "Ok, done ${data.answeredQuestions} of ${data.totalQuestions}")
            activityManager.endActivity(this, data)
            return
        }

        if (subActivityData.result == Correctness.CORRECT){
            data.correctAnswers  = data.correctAnswers + subActivityData.quizId
        } else {
            data.wrongAnswers = data.wrongAnswers + subActivityData.quizId
        }

        data.answeredQuestions++
        if (data.answeredQuestions < data.totalQuestions) {
            messageGateway.textMessage(data.userId, "You have done ${data.answeredQuestions} of ${data.totalQuestions}")
            activityManager.startQuizActivity(data.userId, data.questionIds[data.answeredQuestions])
        } else {
            messageGateway.textMessage(data.userId, "All done! ${data.correctAnswers.size} of ${data.answeredQuestions} isCorrect!")
            activityManager.endActivity(this, data)
        }
    }
}