package tk.germanbot.data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.util.TableUtils
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import tk.germanbot.Application
import tk.germanbot.IntegrationTestsConfig
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

/***
 * Add -Djava.library.path=build/libs to run profile in order to execute this test from IDE
 * (better add to IDEA defaults)
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(Application::class))
@WebAppConfiguration
@Import(IntegrationTestsConfig::class)
@ActiveProfiles("test")
class UserQuizStatRepositoryIntegrationTest {

    private var dynamoDBMapper: DynamoDBMapper? = null
    @Autowired
    private val db: AmazonDynamoDB? = null
    @Autowired
    internal var repository: UserQuizStatRepository? = null

    @Before
    @Throws(Exception::class)
    fun setup() {
        dynamoDBMapper = DynamoDBMapper(db)
        val tableRequest = dynamoDBMapper!!
                .generateCreateTableRequest(UserQuizStat::class.java)
        tableRequest.provisionedThroughput = ProvisionedThroughput(1L, 1L)
        tableRequest.globalSecondaryIndexes?.forEach { i -> i.provisionedThroughput = ProvisionedThroughput(1L, 1L) }
        TableUtils.createTableIfNotExists(db, tableRequest)
        TableUtils.waitUntilActive(db, USER_QUIIZ_STAT_TABLE_NANE)
        dynamoDBMapper!!.batchDelete(repository!!.findAll())

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS)
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS)
        val now = Instant.now()

        repository?.save(UserQuizStat("user1", "quiz1", Date.from(tomorrow), isCorrect = false))
        repository?.save(UserQuizStat("user2", "quiz2", Date.from(yesterday), isCorrect = true))
        repository?.save(UserQuizStat("user2", "quiz3", Date.from(now), isCorrect = true))
        repository?.save(UserQuizStat("user2", "quiz4", Date.from(now), isCorrect = false))
    }

    @Test
    fun findOne() {
        val result = repository!!.findOneByUserIdAndQuizId("user1", "quiz1")
        Assertions.assertThat(result).isNotNull()
        Assertions.assertThat(result!!.correct).isFalse()
        Assertions.assertThat(result!!.userId).isEqualTo("user1")
        Assertions.assertThat(result!!.quizId).isEqualTo("quiz1")

        val result2 = repository!!.findOneByUserIdAndQuizId("user2", "quiz2")
        Assertions.assertThat(result2).isNotNull()
        Assertions.assertThat(result2!!.correct).isTrue()
        Assertions.assertThat(result2!!.userId).isEqualTo("user2")
        Assertions.assertThat(result2!!.quizId).isEqualTo("quiz2")
    }

    @Test
    fun findLastCorrectSorted() {
        val result = repository!!.findByUserIdAndCorrectOrderByDateDesc("user2", true)
        Assertions.assertThat(result).hasSize(2)
        Assertions.assertThat(result!!.map { it.quizId }).containsExactly("quiz3", "quiz2")
    }

}