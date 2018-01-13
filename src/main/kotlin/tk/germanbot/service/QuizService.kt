package tk.germanbot.service

import tk.germanbot.data.Quiz

interface QuizService {
    fun saveQuiz(userId: String, quiz: String, answer: String)
    fun checkAnswer(quizId: String, answer: String): AnswerValidationResult
    fun getAnswer(quizId: String): String
    fun getQuiz(questionId: String): Quiz
    fun getQuestionIds(userId: String, totalQuestions: Int): List<String>
}

enum class Correctness {
    CORRECT,
    PARTIALLY_CORRECT,
    INCORRECT
}

class AnswerValidationResult(
        val result: Correctness,
        val correctAnswer: String = "")
