package tk.germanbot.activity.add

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.activity.Activity
import tk.germanbot.activity.ActivityData
import tk.germanbot.activity.ActivityManager
import tk.germanbot.activity.Event
import tk.germanbot.activity.UserCommand
import tk.germanbot.activity.UserTextMessageEvent
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import java.util.UUID

data class AddQuizActivityData(
        override var userId: String = "",
        var question: String = "",
        var answer: String = "",
        var multiple: Boolean = false) : ActivityData {
    override var id: String = UUID.randomUUID().toString()
    var isCancelled = false
    var questionEntered = false

    fun clean() {
        question = ""
        answer = ""
        questionEntered = false
    }
}

@Component
class AddQuizActivity(
        @Autowired val activityManager: ActivityManager,
        @Autowired override val messageGateway: MessageGateway,
        @Autowired val quizService: QuizService
) : Activity<AddQuizActivityData>() {

    override val helpText = "Enter the question, answer or:\n" +
            "#cancel - cancel\n" +
            "You can use #some_tag to add tags to question"

    override fun onStart(data: AddQuizActivityData) {
        messageGateway.textMessage(data.userId, "What is the question?")
    }

    override fun onEvent(event: Event, data: AddQuizActivityData): Boolean {
        if (event !is UserTextMessageEvent) {
            return false
        }

        if (UserCommand.CANCEL.eq(event.message)) {
            data.isCancelled = true
            messageGateway.textMessage(data.userId, "Ok.")
            activityManager.endActivity(this, data)
            return true
        }

        // todo: refactor to two activities?
        if (data.questionEntered) {
            data.answer = event.message
            saveAndEnd(data)
        } else {

            if (event.message.contains("=")) {
                val split = event.message.split("=")
                if (split.size == 2) {
                    data.question = split[0]
                    data.answer = split[1]
                    saveAndEnd(data)
                    return true
                }
            }

            messageGateway.textMessage(data.userId, "Ok, and the answer?")
            data.question = event.message
            data.questionEntered = true
        }

        return true
    }

    private fun saveAndEnd(data: AddQuizActivityData) {
        val quiz = quizService.saveQuiz(data.userId, data.question, data.answer)
        val answers = quiz.answers!!.joinToString("\n")
        val topics = if (quiz.topics!!.first() != "undefined")
            "\n" + quiz.topics!!.joinToString(" ") { "#$it" }
        else
            ""
        messageGateway.genericMessage(data.userId, quiz.question!!, answers + topics)
        if (!data.multiple) {
            activityManager.endActivity(this, data)
        } else {
            messageGateway.textMessage(data.userId, "Next question?")
            data.clean()
        }
    }
}