package tk.germanbot.flow

interface QuizService {
    fun saveQuiz(userId: String, quiz: String, answer: String)
}