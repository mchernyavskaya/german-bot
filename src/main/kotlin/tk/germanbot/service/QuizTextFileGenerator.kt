package tk.germanbot.service

import tk.germanbot.data.Quiz


class QuizTextFileGenerator {

    fun generateFile(quizzes: List<Quiz>) :String {
        val builder = StringBuilder()
        for (quiz in quizzes) {
            if (quiz.id != null) {
                builder.append("ID:")
                builder.appendln(quiz.id)
            }
            builder.appendln(quiz.question)
            quiz.answers?.forEach { answer -> builder.appendln(answer) }
            builder.appendln(quiz.topics?.sorted()?.joinToString(" ") { "#$it" })
            builder.appendln()
        }
        return builder.toString()
    }

}