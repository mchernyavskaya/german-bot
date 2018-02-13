package tk.germanbot.activity

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import tk.germanbot.activity.add.AddQuizActivity
import tk.germanbot.activity.add.AddQuizActivityData
import tk.germanbot.activity.lesson.LessonActivity
import tk.germanbot.activity.lesson.LessonActivityData
import tk.germanbot.activity.lesson.QuizActivity
import tk.germanbot.activity.lesson.QuizActivityData
import tk.germanbot.messenger.MessageButton
import tk.germanbot.service.StateService

@Component
class ActivityManager(
        @Autowired private val stateService: StateService,
        @Autowired @Lazy private var welcomeActivity: WelcomeActivity,
        @Autowired @Lazy private var lessonActivity: LessonActivity,
        @Autowired @Lazy private var quizActivity: QuizActivity,
        @Autowired @Lazy private var confirmationActivity: ConfirmationActivity,
        @Autowired @Lazy private var addQuizActivity: AddQuizActivity) : EventDispatcher {

    private val logger = LoggerFactory.getLogger(ActivityManager::class.java)

    override fun handleEvent(userId: String, event: Event) {
        val storedStack = stateService.getActivityStack(userId)
        val stack = if (storedStack.isEmpty())
            listOf(startWelcomeActivity(userId))
        else
            storedStack

        val activityData = stack.last()
        val currentActivity = getActivity(activityData)
        onEvent(currentActivity, event, activityData)

        stateService.updateActivityData(activityData)
    }

    fun <T : ActivityData> endActivity(endActivity: Activity<T>, endActivityData: T) {
        val activityStack = stateService.getActivityStack(endActivityData.userId)
        val newStack = activityStack.filter { a -> a.id != endActivityData.id }
        stateService.saveActivityStack(endActivityData.userId, newStack)

        endActivity.onEnd(endActivityData)

        val parentData = newStack.last()
        val parentActivity = getActivity(parentData)
        onSubActivityFinished(parentActivity, parentData, endActivityData)
        stateService.updateActivityData(parentData)
    }

    fun startQuizActivity(userId: String, quizId: String) {
        startActivity(quizActivity, QuizActivityData(userId, quizId))
    }

    fun startLessonActivity(userId: String, topics: Set<String>) {
        startActivity(lessonActivity, LessonActivityData(userId, topics = topics))
    }

    fun startLessonActivity(userId: String, data: LessonActivityData) {
        startActivity(lessonActivity, data)
    }

    fun startAddQuizActivity(userId: String, multiple: Boolean = false, defaultTopics: Set<String> = setOf()) {
        startActivity(addQuizActivity, AddQuizActivityData(userId, multiple = multiple, defaultTopics = defaultTopics))
    }

    fun startWelcomeActivity(userId: String): ActivityData {
        return startActivity(welcomeActivity, WelcomeActivityData(userId))
    }

    fun startConfirmationActivity(userId: String, message: String, buttons: List<MessageButton>): ActivityData {
        return startActivity(confirmationActivity, ConfirmationActivityData(userId, message, buttons))
    }

    private fun <T : ActivityData> startActivity(a: Activity<T>, data: T): T {
        val activityStack = stateService.getActivityStack(data.userId)
        stateService.saveActivityStack(data.userId, activityStack + data)

        logger.info("Starting activity: ${a::class.java.simpleName} with data: $data")
        a.onStart(data)
        stateService.updateActivityData(data)

        return data
    }

    private fun <T : ActivityData> onEvent(a: Activity<T>, event: Event, data: ActivityData) {
        a.handleEvent(event, data as T)
    }

    private fun <T : ActivityData> onSubActivityFinished(activity: Activity<T>, activityData: ActivityData, subActivityData: ActivityData) {
        activity.onSubActivityFinished(activityData as T, subActivityData)
    }

    private fun getActivity(data: ActivityData): Activity<*> {
        return when (data) {
            is QuizActivityData -> quizActivity
            is LessonActivityData -> lessonActivity
            is WelcomeActivityData -> welcomeActivity
            is AddQuizActivityData -> addQuizActivity
            is ConfirmationActivityData -> confirmationActivity
            else -> throw Exception("Unknown activity data: " + data.toString())
        }
    }

}