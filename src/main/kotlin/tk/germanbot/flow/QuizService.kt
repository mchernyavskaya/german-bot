package tk.germanbot.flow

interface QuizService {
    fun saveQuiz(userId: String, quiz: String, answer: String)
    fun checkAnswer(userId: String, quizId: String, answer: String): AnswerValidationResult
    fun getAnswer(userId: String, quizId: String): String
    fun getQuestion(userId: String): Quiz
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
