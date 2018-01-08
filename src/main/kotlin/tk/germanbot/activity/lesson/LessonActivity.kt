package tk.germanbot.activity.lesson

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.activity.Activity
import tk.germanbot.activity.ActivityData
import tk.germanbot.activity.ActivityManager
import tk.germanbot.activity.Event
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import java.util.UUID

data class LessonActivityData(
        override var userId: String = "",
        var desiredQuestions: Int = 5
) : ActivityData {
    override var id: String = UUID.randomUUID().toString()
    var answeredQuestions: Int = 0
    var totalQuestions: Int = desiredQuestions
    var questionIds: List<String> = listOf()
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
        messageGateway.textMessage(data.userId, "Lets do ${data.totalQuestions} exercises")
        activityManager.startQuizActivity(data.userId, data.questionIds[0])
    }

    override fun onEvent(event: Event, data: LessonActivityData): Boolean {
        return false
    }

    override fun onSubActivityFinished(data: LessonActivityData, subActivityData: ActivityData) {
        if (subActivityData !is QuizActivityData){
            return
        }

        if (subActivityData.isCancelled){
            messageGateway.textMessage(data.userId, "Ok, done ${data.answeredQuestions} of ${data.totalQuestions}")
            activityManager.endActivity(this, data)
            return
        }

        data.answeredQuestions++
        if (data.answeredQuestions < data.totalQuestions) {
            messageGateway.textMessage(data.userId, "Good, done ${data.answeredQuestions} of ${data.totalQuestions}")
            activityManager.startQuizActivity(data.userId, data.questionIds[data.answeredQuestions])
        } else {
            messageGateway.textMessage(data.userId, "All done!")
            activityManager.endActivity(this, data)
        }
    }
}