package tk.germanbot.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tk.germanbot.data.UserQuizStat
import tk.germanbot.data.UserQuizStatRepository
import java.time.Instant
import java.util.Date


@Service
class DynamoUserStatService(
        @Autowired private val statRep: UserQuizStatRepository
):UserStatService {

    override fun updateQuizStat(userId: String, quizId: String, isCorrect: Boolean): UserQuizStat {
        // todo: minor: move old stat to log
        val stat = statRep.findOneByUserIdAndQuizId(userId, quizId) ?: UserQuizStat(userId, quizId)
        stat.date = Date.from(Instant.now())
        stat.correct = isCorrect
        statRep.save(stat)
        return stat
    }

}