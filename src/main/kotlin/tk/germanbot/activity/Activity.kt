package tk.germanbot.activity

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import tk.germanbot.activity.lesson.LessonActivityData
import tk.germanbot.activity.lesson.QuizActivityData
import tk.germanbot.service.MessageGateway

@JsonSubTypes(
        JsonSubTypes.Type(WelcomeActivityData::class, name = "welcome"),
        JsonSubTypes.Type(LessonActivityData::class, name = "lesson"),
        JsonSubTypes.Type(QuizActivityData::class, name = "quiz"))
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
interface ActivityData {
    val id: String
    val userId: String
}

abstract class Activity<T : ActivityData> {

    abstract val messageGateway: MessageGateway

    open val helpText = "? - this message"

    open fun onStart(data: T) {}
    open fun onEnd(data: T) {}
    open protected fun onEvent(event: Event, data: T): Boolean = false
    open fun onSubActivityFinished(data: T, subActivityData: ActivityData) {}

    fun handleEvent(event: Event, data: T): Boolean {
        if (isTextMessage(event, "?")) {
            messageGateway.textMessage(data.userId, helpText)
            return true
        }

        return if (!onEvent(event, data)) {
            messageGateway.textMessage(data.userId, "What?")
            false
        } else {
            true
        }
    }
}
