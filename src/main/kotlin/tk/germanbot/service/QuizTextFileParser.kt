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
            val question = scanner.nextLine().trim()
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
            while (scanner.hasNextLine()) {
                val answer = scanner.nextLine().trim()
                if (answer.isBlank()) {
                    break
                }

                if (answer.startsWith("#")) {
                    topics += getTopics(answer)
                } else {
                    answers += answer
                }
            }
            
            quizzes += Quiz(createdBy = userId, question = question, answers = answers, topics = topics + globalTopics)
        }

        return quizzes
    }

    private fun getTopics(line: String) = line.split("\\s+".toPattern()).map { it.replace("#", "").trim() }
}