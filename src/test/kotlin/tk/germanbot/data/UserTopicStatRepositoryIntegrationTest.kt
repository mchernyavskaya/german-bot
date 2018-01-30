package tk.germanbot.data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
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
import tk.germanbot.DynamoTools
import tk.germanbot.IntegrationTestsConfig

/***
 * Add -Djava.library.path=build/libs to run profile in order to execute this test from IDE
 * (better add to IDEA defaults)
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(Application::class))
@WebAppConfiguration
@Import(IntegrationTestsConfig::class)
@ActiveProfiles("test")
class UserTopicStatRepositoryIntegrationTest {

    private var mapper: DynamoDBMapper? = null
    @Autowired
    private val db: AmazonDynamoDB? = null
    @Autowired
    internal var repository: UserTopicStatRepository? = null

    @Autowired
    private var dynamoTools: DynamoTools? = null

    @Before
    @Throws(Exception::class)
    fun setup() {
        mapper = DynamoDBMapper(db)

        dynamoTools!!.createTableIfNotExist(mapper!!, db!!, UserTopicStat::class.java, USER_TOPIC_STAT_TABLE_NANE)
        TableUtils.waitUntilActive(db, USER_TOPIC_STAT_TABLE_NANE)

    }

    @Test
    fun canSaveAndFindOne() {

        val stat1 = UserTopicStat("user1", correctCount = 1, incorrectCount = 2, topics = mapOf(
                "A1" to TopicStat(10, 20),
                "A2" to TopicStat(30, 40)
        ))

        repository!!.save(stat1)

        val saved = repository!!.findOneByUserId("user1")!!

        Assertions.assertThat(saved.userId).isEqualTo(stat1.userId)
        Assertions.assertThat(saved.correctCount).isEqualTo(stat1.correctCount)
        Assertions.assertThat(saved.incorrectCount).isEqualTo(stat1.incorrectCount)
        Assertions.assertThat(saved.topics).isEqualTo(stat1.topics)
    }

}