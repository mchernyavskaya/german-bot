package tk.germanbot.service

import tk.germanbot.data.UserQuizStat


interface UserStatService {

    fun updateQuizStat(userId: String, quizId: String, isCorrect: Boolean): UserQuizStat

}