package tk.germanbot.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tk.germanbot.data.TopicStat
import tk.germanbot.data.UserQuizStat
import tk.germanbot.data.UserQuizStatRepository
import tk.germanbot.data.UserTopicStat
import tk.germanbot.data.UserTopicStatRepository
import java.time.Instant
import java.util.Date


@Service
class DynamoUserStatService(
        @Autowired private val quizStatRepo: UserQuizStatRepository,
        @Autowired private val topicStatRep: UserTopicStatRepository
):UserStatService {

    override fun updateQuizStat(userId: String, quizId: String, topics: Set<String>, isCorrect: Boolean): UserTopicStat {
        // save quiz stat
        val stat = quizStatRepo.findOneByUserIdAndQuizId(userId, quizId) ?: UserQuizStat(userId, quizId)
        stat.date = Date.from(Instant.now())
        stat.correct = isCorrect
        quizStatRepo.save(stat)

        return updateTopicStat(userId, topics, isCorrect)
    }

    override fun getTopicStat(userId: String): UserTopicStat? {
        return topicStatRep.findOneByUserId(userId)
    }

    private fun updateTopicStat(userId: String, topics: Set<String>, isCorrect: Boolean): UserTopicStat {
        val topicStat = topicStatRep.findOneByUserId(userId) ?: UserTopicStat(userId = userId)
        if (isCorrect) {
            topicStat.correctCount = (topicStat.correctCount ?: 0) + 1
        } else {
            topicStat.incorrectCount = (topicStat.incorrectCount ?: 0) + 1
        }

        topics.forEach {
            if (!topicStat.topics!!.contains(it)) {
                topicStat.topics = topicStat.topics!! + (it to TopicStat())
            }
            val ts = topicStat.topics!![it]!!
            if (isCorrect) {
                ts.correctCount = (ts.correctCount ?: 0) + 1
            } else {
                ts.incorrectCount = (ts.incorrectCount ?: 0) + 1
            }
        }

        return topicStatRep.save(topicStat)
    }


}