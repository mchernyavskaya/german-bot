package tk.germanbot.service

import tk.germanbot.data.UserTopicStat


interface UserStatService {

    fun updateQuizStat(userId: String, quizId: String, topics: Set<String>, isCorrect: Boolean): UserTopicStat

    fun getTopicStat(userId: String): UserTopicStat?

}