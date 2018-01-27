package tk.germanbot.data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.util.TableUtils
import com.google.common.collect.Sets
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

/***
 * Add -Djava.library.path=build/libs to run profile in order to execute this test from IDE
 * (better add to IDEA defaults)
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(Application::class))
@WebAppConfiguration
@Import(IntegrationTestsConfig::class)
@ActiveProfiles("test")
class QuizTopicRepositoryIntegrationTest {

    @Autowired
    private val db: AmazonDynamoDB? = null
    @Autowired
    private val repo: QuizTopicRepository? = null

    private var mapper: DynamoDBMapper? = null

    @Before
    @Throws(Exception::class)
    fun setup() {
        mapper = DynamoDBMapper(db)

        TableUtils.deleteTableIfExists(db, mapper!!.generateDeleteTableRequest(QuizTopic::class.java))

        val tableRequest = mapper!!
                .generateCreateTableRequest(QuizTopic::class.java)
        tableRequest.provisionedThroughput = ProvisionedThroughput(1L, 1L)
        tableRequest.globalSecondaryIndexes?.forEach { i -> i.provisionedThroughput = ProvisionedThroughput(1L, 1L) }
        TableUtils.createTableIfNotExists(db, tableRequest)

        TableUtils.waitUntilActive(db, QUIZ_TOPIC_TABLE_NANE)
    }

    @Test
    fun canCreateTopicsForNewQuiz() {
        val q1 = Quiz(id = "q1", createdBy = "me", question = "Q", answers = setOf("A"), topics = setOf("A", "B"))
        repo!!.saveTopics(q1)
        val q2 = Quiz(id = "q2", createdBy = "me", question = "Q2", answers = setOf("A2"), topics = setOf("A1", "B1"))
        repo!!.saveTopics(q2)

        val saved = repo!!.findTopics(q1)
        Assertions.assertThat(saved).hasSize(3)
        Assertions.assertThat(saved).contains(QuizTopic("A", q1.id), QuizTopic("B", q1.id), QuizTopic("A#B", q1.id))
    }

    @Test
    fun canUpdateTopicsForExistingQuiz() {
        val q1 = Quiz(id = "q1", createdBy = "me", question = "Q", answers = setOf("A"), topics = setOf("A", "B"))
        repo!!.saveTopics(q1)

        val saved = repo!!.findTopics(q1)
        Assertions.assertThat(saved).hasSize(3)
        Assertions.assertThat(saved).contains(QuizTopic("A", q1.id), QuizTopic("B", q1.id), QuizTopic("A#B", q1.id))

        q1.topics = setOf("A", "C")
        repo!!.saveTopics(q1)

        val updated = repo!!.findTopics(q1)
        Assertions.assertThat(updated).hasSize(3)
        Assertions.assertThat(updated).contains(QuizTopic("A", q1.id), QuizTopic("C", q1.id), QuizTopic("A#C", q1.id))
    }

    @Test
    fun canGetQuizByTopic() {
        val q1 = Quiz(id = "q1", createdBy = "me", question = "Q", answers = setOf("A"), topics = setOf("A", "B"))
        repo!!.saveTopics(q1)
        val q2 = Quiz(id = "q2", createdBy = "me", question = "Q2", answers = setOf("A2"), topics = setOf("A", "C"))
        repo!!.saveTopics(q2)
        val q3 = Quiz(id = "q3", createdBy = "me", question = "Q3", answers = setOf("A3"), topics = setOf("A", "C", "B"))
        repo!!.saveTopics(q3)

        val qA = repo!!.findQuizIdsByTopics(setOf("B"))
        Assertions.assertThat(qA).hasSize(2)
        Assertions.assertThat(qA).contains(QuizTopic(quizId = q1.id), QuizTopic(quizId = q3.id))

        val qAB = repo!!.findQuizIdsByTopics(setOf("B", "A"))
        Assertions.assertThat(qAB).hasSize(2)
        Assertions.assertThat(qAB).contains(QuizTopic(quizId = q1.id), QuizTopic(quizId = q3.id))

        val qABC = repo!!.findQuizIdsByTopics(setOf("C", "A", "B"))
        Assertions.assertThat(qABC).hasSize(1)
        Assertions.assertThat(qABC).contains(QuizTopic(quizId = q3.id))
    }

    @Test
    fun canDoPowerset() {
        val perms = Sets.powerSet(setOf("B", "A", "C"))
                .flatMap { set -> setOf(set.sorted().joinToString("")) }
                .filter(String::isNotBlank)
        Assertions.assertThat(perms).contains("B", "A", "AB", "C", "BC", "AC", "ABC")
    }


}