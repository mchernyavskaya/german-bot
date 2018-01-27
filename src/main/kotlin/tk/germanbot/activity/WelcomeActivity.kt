package tk.germanbot.activity

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import tk.germanbot.service.QuizTextFileGenerator
import tk.germanbot.service.QuizTextFileParser
import tk.germanbot.service.S3Service
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
        @Autowired private val quizService: QuizService,
        @Autowired private val s3Service: S3Service
) : Activity<WelcomeActivityData>() {

    private val logger = LoggerFactory.getLogger(WelcomeActivity::class.java)

    override val helpText = "Now you can:" +
            "#q - start quick session (5 questions)\n" +
            "#a - add quiz\n" +
            "#aa - add multiple quizzes\n" +
            "#e - export quizzes\n" +
            "or upload file with quizzes"

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

        if (isTextMessage(event, "#e")) {
            exportQuizzes(data.userId)
            return true
        }

        // todo: move to separated activity
        if (event is UserAttachmentEvent) {
            loadQuizzesFromFile(event.userId, event.fileUrl)
            return true
        }

        return false
    }

    private fun exportQuizzes(userId: String) {
        // todo: limit export to only user's quizzes
        val quizzes = quizService.getAll()
        val quizFileContent = QuizTextFileGenerator().generateFile(quizzes)
        val uploadedFileUrl = s3Service.uploadFile(userId + ".txt", quizFileContent)
        messageGateway.textMessage(userId, "Exported ${quizzes.size} quizzes:")
        messageGateway.fileMessage(userId, uploadedFileUrl)
    }

    private fun loadQuizzesFromFile(userId: String, fileUrl: String) {
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

