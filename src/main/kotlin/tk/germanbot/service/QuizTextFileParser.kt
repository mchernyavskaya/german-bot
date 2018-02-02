package tk.germanbot.service

import tk.germanbot.data.Quiz
import java.util.Scanner


interface QuizSource {
    fun getQuizzes(): List<Quiz>
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
            while (scanner.hasNextLine()) {
                val answer = scanner.nextLine().trim()
                if (answer.isBlank()) {
                    break
                }

                if (answer.equals("!Published", true)) {
                    isPublished = true
                    continue
                }

                if (answer.startsWith("#")) {
                    topics += getTopics(answer)
                } else {
                    answers += answer
                }
            }

            quizzes += Quiz(id = id, createdBy = userId, question = question,
                    answers = answers, topics = topics + globalTopics,
                    isPublished = isPublished)
        }

        return quizzes
    }

    private fun getTopics(line: String) = line.split("\\s+".toPattern()).map { it.replace("#", "").trim() }
}