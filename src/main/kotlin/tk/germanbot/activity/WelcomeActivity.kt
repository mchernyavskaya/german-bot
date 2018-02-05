package tk.germanbot.activity

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tk.germanbot.data.QuizTopic
import tk.germanbot.service.MessageGateway
import tk.germanbot.service.QuizService
import tk.germanbot.service.QuizTextFileGenerator
import tk.germanbot.service.QuizTextFileParser
import tk.germanbot.service.S3Service
import tk.germanbot.service.UserStatService
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
        @Autowired private val userStatService: UserStatService,
        @Autowired private val s3Service: S3Service
) : Activity<WelcomeActivityData>() {

    private val logger = LoggerFactory.getLogger(WelcomeActivity::class.java)

    override val helpText = "Now you can:" +
            "#q [topics] - start quick quiz\n" +
            "#a - add quiz\n" +
            "#aa - add multiple quizzes\n" +
            "#e [my] [topics] - export quizzes\n" +
            "#s - show stat\n" +
            "or upload file with quizzes"

    override fun onEvent(event: Event, data: WelcomeActivityData): Boolean {

        if (isTextMessage(event, "#q")) {
            val topics = extractParams(event)
            activityManager.startLessonActivity(data.userId, topics)
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
            val topics = extractParams(event)
            exportQuizzes(data.userId, topics)
            return true
        }

        if (isTextMessage(event, "#s")) {
            showStat(data.userId)
            return true
        }

        // todo: move to separated activity
        if (event is UserAttachmentEvent) {
            loadQuizzesFromFile(event.userId, event.fileUrl)
            return true
        }

        return false
    }

    private fun extractParams(event: Event): Set<String> {
        val message = (event as UserTextMessageEvent).message
        if (message.indexOf(" ") < 0) {
            return setOf()
        }

        return message
                .substring(message.indexOf(" "))
                .trim()
                .split("\\s+")
                .filter(String::isNotBlank)
                .toSet()
    }

    private fun showStat(userId: String) {
        val topicStat = userStatService.getTopicStat(userId)
        if (topicStat?.topics != null && topicStat.topics!!.isNotEmpty()) {

            val topStat = topicStat.topics?.entries?.
                    sortedWith(compareByDescending { (topic, count) -> count.totalCount })?.
                    take(10)?.
                    joinToString("\n") { (topic, count) ->
                        "#$topic: ${count.correctCount} / ${count.incorrectCount}"
                    }?.replace(QuizTopic.UNDEFINED, "other")

            messageGateway.textMessage(userId, "Topic: correct / incorrect")
            if (topStat != null) {
                messageGateway.textMessage(userId, topStat)
            }
            messageGateway.textMessage(userId, "Total: ${topicStat.correctCount} / ${topicStat.incorrectCount}")
        } else {
            messageGateway.textMessage(userId, "Nothing yet =)")
        }
    }

    private fun exportQuizzes(userId: String, topics: Set<String>) {
        val quizzes = quizService.getQuizzesByTopics(userId, topics - "my", topics.contains("my"))

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

