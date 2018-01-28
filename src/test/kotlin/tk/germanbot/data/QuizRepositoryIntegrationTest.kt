package tk.germanbot.data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.util.TableUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertTrue
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

/***
 * Add -Djava.library.path=build/libs to run profile in order to execute this test from IDE
 * (better add to IDEA defaults)
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(Application::class))
@WebAppConfiguration
@Import(IntegrationTestsConfig::class)
@ActiveProfiles("test")
class QuizRepositoryIntegrationTest {
    private val EXPECTED_Q = "Hallo"
    private val EXPECTED_A = "Hello"

    private var dynamoDBMapper: DynamoDBMapper? = null
    @Autowired
    private val db: AmazonDynamoDB? = null
    @Autowired
    internal var repository: QuizRepository? = null

    var hello: Quiz? = null

    @Before
    @Throws(Exception::class)
    fun setup() {
        dynamoDBMapper = DynamoDBMapper(db)
        val tableRequest = dynamoDBMapper!!
                .generateCreateTableRequest(Quiz::class.java)
        tableRequest.provisionedThroughput = ProvisionedThroughput(1L, 1L)
        TableUtils.createTableIfNotExists(db, tableRequest)
        TableUtils.waitUntilActive(db, QUIZ_TABLE_NANE)
        dynamoDBMapper!!.batchDelete(repository!!.findAll())

        hello = repository?.save(Quiz(createdBy = "user", question = EXPECTED_Q, answers = setOf(EXPECTED_A), topics = setOf("A", "B")))
    }

    @Test
    fun findAll() {
        val result = repository!!.findAll()
        assertTrue("Not empty", result.isNotEmpty())
        assertTrue("Contains item with expected translation",
                result[0].question == EXPECTED_Q)
    }

    @Test
    fun findById() {
        val result = repository!!.findOneById(hello!!.id!!)
        assertTrue("ID Not empty", result!!.id != null)
        assertTrue("Contains item with expected translation",
                result!!.question == EXPECTED_Q)
    }

    @Test
    fun updateById() {
        repository!!.save(Quiz(id = "123", createdBy = "me", question = "Q", answers = setOf("A1"), topics = setOf("A", "B")))

        val savedQ = repository!!.findOneById("123")
        assertThat(savedQ).isNotNull()
        assertThat(savedQ!!.question).isEqualTo("Q")

        repository!!.save(Quiz(id = "123", createdBy = "me", question = "QQ", answers = setOf("A1"), topics = setOf("A", "B")))

        val updatedQ = repository!!.findOneById("123")
        assertThat(updatedQ).isNotNull()
        assertThat(updatedQ!!.question).isEqualTo("QQ")
    }

    @Test
    fun findByTopicCanContainOne() {
        val result = repository!!.findByTopicsContaining("A")
        assertTrue("Not empty", result.isNotEmpty())
        assertTrue("ID Not empty", result[0].id != null)
        assertTrue("Contains item with expected translation",
                result[0].question == EXPECTED_Q)
    }

    @Test
    fun findByTopicCanContainOther() {
        val result = repository!!.findByTopicsContaining("B")
        assertTrue("Not empty", result.isNotEmpty())
        assertTrue("ID Not empty", result[0].id != null)
        assertTrue("Contains item with expected translation",
                result[0].question == EXPECTED_Q)
    }

    @Test
    fun findByTopicCanContainNone() {
        val result = repository!!.findByTopicsContaining("C")
        assertTrue("Empty", result.isEmpty())
    }

    @Test
    fun findTop5ByIdGreaterThan() {
        val result1 = repository!!.findTop5ByIdGreaterThan("random_UUID")
        val result2 = repository!!.findTop5ByIdLessThan("random_UUID")
        assertTrue(result1.isNotEmpty() || result2.isNotEmpty())
    }

}