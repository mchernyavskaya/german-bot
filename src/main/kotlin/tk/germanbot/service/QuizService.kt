package tk.germanbot.service

import tk.germanbot.data.Quiz
import java.util.*

interface QuizService {
    fun saveQuiz(userId: String, quiz: String, answer: String)
    fun checkAnswer(quizId: String, answer: String): AnswerValidationResult
    fun getAnswer(quizId: String): String
    fun getQuiz(questionId: String): Quiz
    fun getQuestionIds(userId: String, totalQuestions: Int): List<String>
}

enum class Correctness(val answers: Array<String>) {
    CORRECT(arrayOf(
            "Correct!",
            "Yes! Well done!",
            "Absolutely right!",
            "Cool! Have a cookie!"
    )),
    PARTIALLY_CORRECT(arrayOf(
            "Not quite. Expected answer:",
            "Almost there. Expected answer:",
            "Getting there. Expected answer:",
            "You were so close! Expected answer:"
    )),
    INCORRECT(arrayOf(
            "Better luck next time! Expected answer:",
            "Missed it! Expected answer:",
            ";-( Not quite. Expected answer:"
    ));

    private val r = Random(0)

    fun getAnswer(correctAnswer: String): String {
        val bound = this.answers.size
        val index = r.nextInt(bound)
        if (this == CORRECT) {
            return CORRECT.answers[index]
        }
        return this.answers[index] + " " + correctAnswer
    }

}

class AnswerValidationResult(
        val result: Correctness,
        val correctAnswer: String = "")
