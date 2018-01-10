package tk.germanbot.service

interface QuizService {
    fun saveQuiz(quiz: String, answer: String)
    fun checkAnswer(quizId: String, answer: String): AnswerValidationResult
    fun getAnswer(quizId: String): String
    fun getQuiz(questionId: String): Quiz
    fun getQuestionIds(userId: String, totalQuestions: Int): List<String>
}

data class Quiz(
        val id :String,
        val question: String,
        val answer: String)

enum class Correctness {
    CORRECT,
    PARTIALLY_CORRECT,
    INCORRECT
}

class AnswerValidationResult(
        val result: Correctness,
        val correctAnswer: String = "")
