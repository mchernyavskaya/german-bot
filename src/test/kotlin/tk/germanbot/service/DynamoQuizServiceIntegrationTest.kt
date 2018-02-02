package tk.germanbot.service

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
import tk.germanbot.data.QUIZ_TABLE_NANE
import tk.germanbot.data.QUIZ_TOPIC_TABLE_NANE
import tk.germanbot.data.Quiz
import tk.germanbot.data.QuizTopic
import tk.germanbot.data.QuizTopicRepository
import tk.germanbot.data.USER_QUIIZ_STAT_TABLE_NANE
import tk.germanbot.data.UserQuizStat

/***
 * Add -Djava.library.path=build/libs to run profile in order to execute this test from IDE
 * (better add to IDEA defaults)
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(Application::class))
@WebAppConfiguration
@Import(IntegrationTestsConfig::class)
@ActiveProfiles("test")
class DynamoQuizServiceIntegrationTest {

    @Autowired
    private var mapper: DynamoDBMapper? = null
    @Autowired
    private val db: AmazonDynamoDB? = null
    @Autowired
    private var dynamoTools: DynamoTools? = null

    @Autowired
    private var quizService: QuizService? = null

    @Autowired
    private var quizTopicsRepo: QuizTopicRepository? = null

    // to test auto-generated Id
    private val q1 = Quiz(createdBy = "me", question = "Q1", answers = setOf("A"), topics = setOf("A"))

    @Before
    @Throws(Exception::class)
    fun setup() {
        TableUtils.deleteTableIfExists(db, mapper!!.generateDeleteTableRequest(Quiz::class.java))
        TableUtils.deleteTableIfExists(db, mapper!!.generateDeleteTableRequest(QuizTopic::class.java))
        TableUtils.deleteTableIfExists(db, mapper!!.generateDeleteTableRequest(UserQuizStat::class.java))
        dynamoTools!!.createTableIfNotExist(mapper!!, db!!, Quiz::class.java, QUIZ_TABLE_NANE)
        dynamoTools!!.createTableIfNotExist(mapper!!, db!!, QuizTopic::class.java, QUIZ_TOPIC_TABLE_NANE)
        dynamoTools!!.createTableIfNotExist(mapper!!, db!!, UserQuizStat::class.java, USER_QUIIZ_STAT_TABLE_NANE)
        TableUtils.waitUntilActive(db, QUIZ_TABLE_NANE)
        TableUtils.waitUntilActive(db, QUIZ_TOPIC_TABLE_NANE)
        TableUtils.waitUntilActive(db, USER_QUIIZ_STAT_TABLE_NANE)

        quizService!!.saveQuiz("me", q1)
        quizService!!.saveQuiz("me", Quiz(id = "2", createdBy = "me", question = "Q2", answers = setOf("A"), topics = setOf("B")))
        quizService!!.saveQuiz("me", Quiz(id = "3", createdBy = "me", question = "Q3", answers = setOf("A"), topics = setOf("A", "B")))
        quizService!!.saveQuiz("me", Quiz(id = "4", createdBy = "me", question = "Q4", answers = setOf("A"), topics = setOf("C", "A")))
        quizService!!.saveQuiz("me", Quiz(isPublished = true, id = "5", createdBy = "NOT_ME", question = "Q5", answers = setOf("A"), topics = setOf("A", "B", "C")))
        quizService!!.saveQuiz("me", Quiz(id = "6", createdBy = "NOT_ME", question = "Q5", answers = setOf("A"), topics = setOf("A", "B", "C")))

        TableUtils.waitUntilActive(db, QUIZ_TOPIC_TABLE_NANE)
    }

    @Test
    fun getQuestionIdsCanGetByTopics() {
        // all in one method because of heavy setup()

        val questionIds = quizService!!.selectQuizzesForUser("me", setOf("A"), 7)
        Assertions.assertThat(questionIds).hasSize(4)
        Assertions.assertThat(questionIds).contains(q1.id, "3", "4", "5")

        val questionIds2 = quizService!!.selectQuizzesForUser("me", setOf("B"), 7)
        Assertions.assertThat(questionIds2).hasSize(3)
        Assertions.assertThat(questionIds2).contains("2", "3", "5")

        val questionIds3 = quizService!!.selectQuizzesForUser("me", setOf("A", "B"), 7)
        Assertions.assertThat(questionIds3).hasSize(2)
        Assertions.assertThat(questionIds3).contains("3", "5")

        val questionIds4 = quizService!!.selectQuizzesForUser("me", setOf("A", "C", "B"), 7)
        Assertions.assertThat(questionIds4).hasSize(1)
        Assertions.assertThat(questionIds4).contains("5")

        val questionIds5 = quizService!!.selectQuizzesForUser("me", setOf("A"), 2)
        Assertions.assertThat(questionIds5).hasSize(2)

        val questionIds6 = quizService!!.selectQuizzesForUser("me", setOf(), 10)
        Assertions.assertThat(questionIds6).hasSize(5)

        val questionIds7 = quizService!!.selectQuizzesForUser("me", setOf("Z"), 2)
        Assertions.assertThat(questionIds7).hasSize(0)

        val questionIds8 = quizService!!.selectQuizzesForUser("somwOne", setOf(), 5)
        Assertions.assertThat(questionIds8).hasSize(1)

    }

}