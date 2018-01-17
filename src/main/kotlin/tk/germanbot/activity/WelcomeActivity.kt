package tk.germanbot.activity

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import tk.germanbot.service.QuizTextFileParser
import java.net.URL
import java.nio.charset.Charset
import java.util.UUID

data class WelcomeActivityData(
        override var userId: String = "") : ActivityData {
    var isHelloSaid: Boolean = false
    override var id: String = UUID.randomUUID().toString()
}

@Component
class WelcomeActivity(
        @Autowired override val messageGateway: MessageGateway,
        @Autowired private val activityManager: ActivityManager,
        @Autowired private val quizService: QuizService
) : Activity<WelcomeActivityData>() {

    private val logger = LoggerFactory.getLogger(WelcomeActivity::class.java)

    override val helpText = "#q - start quick session (5 questions)\n" +
            "#a - add quiz\n" +
            "#aa - add multiple quizzes"

    override fun onEvent(event: Event, data: WelcomeActivityData): Boolean {

        if (isTextMessage(event, "#q")) {
            activityManager.startLessonActivity(data.userId)
            return true
        }

        if (isTextMessage(event, "#a")) {
            activityManager.startAddQuizActivity(data.userId)
            return true
        }

        if (isTextMessage(event, "#aa")) {
            activityManager.startAddQuizActivity(data.userId, true)
            return true
        }

        // todo: move to separated activity
        if (event is UserAttachmentEvent) {
            parseQuizFile(event.userId, event.fileUrl)
            return true
        }

        return false
    }

    fun parseQuizFile(userId: String, fileUrl: String) {
        val inStream = URL(fileUrl).openStream()
        try {
            val content = IOUtils.toString(inStream, Charset.defaultCharset())
            val quizzes = QuizTextFileParser(userId, content).getQuizzes()

            var saved = 0
            var errors = 0
            quizzes.forEach {
                try {
                    quizService.saveQuiz(userId, it)
                    saved++
                } catch (e: Exception) {
                    errors++
                    logger.error("Unable to save quiz ${it.question} for user: ${userId}", e)
                }
            }

            messageGateway.textMessage(userId, "Saved $saved questions!")
            if (errors > 0) {
                messageGateway.textMessage(userId, "Errors: $errors")
            }

        } finally {
            IOUtils.closeQuietly(inStream)
        }
    }
}

