package tk.germanbot.service

import tk.germanbot.data.Quiz
import java.util.Scanner


interface QuizFileGenerator {

    fun generateFile(quizzes: List<Quiz>): String

}

interface QuizSource {
    fun getQuizzes(): List<Quiz>
}

class QuizTextFileGenerator : QuizFileGenerator {

    override fun generateFile(quizzes: List<Quiz>): String {
        val builder = StringBuilder()
        for (quiz in quizzes) {
            if (quiz.id != null) {
                builder.append("ID:")
                builder.appendln(quiz.id)
            }

            builder.appendln(quiz.question)

            quiz.answers?.forEach { answer -> builder.appendln(answer) }

            builder.appendln(quiz.topics?.sorted()?.joinToString(" ") { "#$it" })

            if (quiz.example != null) {
                builder.append("E:")
                builder.appendln(quiz.example)
            }

            if (quiz.isPublished) {
                builder.appendln("!Published")
            }

            builder.appendln()
        }
        return builder.toString()
    }

}

class QuizTextFileParser(
        private val userId: String,
        private val content: String
) : QuizSource {
    override fun getQuizzes(): List<Quiz> {
        val scanner = Scanner(content)

        var globalTopics = listOf<String>()
        var quizzes = listOf<Quiz>()
        var isFirstLine = true
        while (scanner.hasNextLine()) {
            val firstLine = scanner.nextLine().trim()

            val id = if (firstLine.startsWith("id:", ignoreCase = true)) firstLine.substring(3).trim() else null

            val question = if (id == null) firstLine else scanner.nextLine().trim()
            if (question.isBlank()) {
                continue
            }

            // if first line contains topics - make this topics global for all quizzes
            if (isFirstLine && question.startsWith("#")) {
                isFirstLine = false
                globalTopics = getTopics(question)
                continue
            }

            var answers = setOf<String>()
            var topics = setOf<String>()
            var isPublished = false
            var example: String? = null
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine().trim()
                if (line.isBlank()) {
                    break
                }

                if (line.equals("!Published", true)) {
                    isPublished = true
                    continue
                }

                if (line.startsWith("E:", true)) {
                    example = line.substring(2).trim()
                    continue
                }

                if (line.startsWith("#")) {
                    topics += getTopics(line)
                    continue
                }

                answers += line
            }

            quizzes += Quiz(id = id, createdBy = userId, question = question,
                    answers = answers, topics = topics + globalTopics,
                    isPublished = isPublished, example = example)
        }

        return quizzes
    }

    private fun getTopics(line: String) = line.split("\\s+".toPattern()).map { it.replace("#", "").trim() }
}