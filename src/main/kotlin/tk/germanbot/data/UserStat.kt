package tk.germanbot.data

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.annotation.Id
import org.springframework.data.repository.CrudRepository
import tk.germanbot.service.EntityValidationException
import java.io.Serializable
import java.util.Date


const val USER_QUIIZ_STAT_TABLE_NANE = "german_bot_user_stat"
const val USER_TOPIC_STAT_TABLE_NANE = "german_bot_user_topic_stat"

class UserQuizStatKey(
        @DynamoDBHashKey
        var userId: String? = null,

        @DynamoDBRangeKey
        var quizId: String? = null
) : Serializable

class UserQuizStatKeyConverter : DynamoDBTypeConverter<String, UserQuizStatKey> {
    override fun unconvert(obj: String?): UserQuizStatKey? {
        if (obj == null) return null
        val parts = obj.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return UserQuizStatKey(parts[0], if (parts.size == 2) parts[1] else null)
    }

    override fun convert(key: UserQuizStatKey?): String? = if (key != null) key!!.userId + "#" + key!!.quizId else null
}

@DynamoDBTable(tableName = USER_QUIIZ_STAT_TABLE_NANE)
data class UserQuizStat(
        @DynamoDBAttribute
        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
        @DynamoDBIndexRangeKey(globalSecondaryIndexName = "german_bot_idx_userIdCorrect_date")
        var date: Date? = Date(),

        @DynamoDBAttribute
        @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.N)
        var correct: Boolean? = true
) {

    constructor(userId: String, quizId: String, date: Date? = Date(), isCorrect: Boolean? = true) : this(date, isCorrect) {
        this.id = UserQuizStatKey(userId, quizId)
    }

    @Id
    var id: UserQuizStatKey? = null
        @DynamoDBTypeConverted(converter = UserQuizStatKeyConverter::class)
        get
        set

    var userId: String?
        @DynamoDBHashKey
        get() = this.id?.userId
        set(value) {
            if (this.id == null) {
                this.id = UserQuizStatKey()
            }

            this.id!!.userId = value
        }

    var quizId: String?
        @DynamoDBRangeKey
        get() = this.id?.quizId
        set(value) {
            if (this.id == null) {
                this.id = UserQuizStatKey()
            }

            this.id!!.quizId = value
        }

    var userIdCorrect: String?
        @DynamoDBAttribute
        @DynamoDBIndexHashKey(globalSecondaryIndexName = "german_bot_idx_userIdCorrect_date")
        get() = this.userId + "#" + this.correct
        set(value) {
            // do nothing
        }

    fun validate() {
        if (userId == null || quizId == null) throw EntityValidationException(UserQuizStat::class, "No id or quizId: ${userId}:${quizId}")
    }

}

@EnableScan
interface UserQuizStatRepository : CrudRepository<UserQuizStat, String> {

    fun save(userQuizStat: UserQuizStat): UserQuizStat

    fun findOneByUserIdAndQuizId(userId: String, quizId: String): UserQuizStat?

    fun findByUserIdCorrectOrderByDateDesc(userIdCorrect: String): List<UserQuizStat>?

}

fun UserQuizStatRepository.findByUserIdAndCorrectOrderByDateDesc(userId: String, correct: Boolean): List<UserQuizStat>? {
    return this.findByUserIdCorrectOrderByDateDesc(userId + "#" + correct)
}

@DynamoDBTable(tableName = USER_TOPIC_STAT_TABLE_NANE)
data class UserTopicStat(
        @Id
        @DynamoDBHashKey
        var userId: String? = null,

        @DynamoDBAttribute
        var correctCount: Int? = 0,

        @DynamoDBAttribute
        var incorrectCount: Int? = 0,

        @DynamoDBAttribute
        var topics: Map<String, TopicStat>? = mapOf()
)

@DynamoDBDocument
data class TopicStat(
        @DynamoDBAttribute
        var correctCount: Int? = 0,
        @DynamoDBAttribute
        var incorrectCount: Int? = 0
) {

    val totalCount: Int
        @DynamoDBIgnore
        get () = (correctCount ?: 0) + (incorrectCount ?: 0)

}

@EnableScan
interface UserTopicStatRepository : CrudRepository<UserTopicStat, String> {

    fun save(stat: UserTopicStat): UserTopicStat

    fun findOneByUserId(userId: String): UserTopicStat?

}
