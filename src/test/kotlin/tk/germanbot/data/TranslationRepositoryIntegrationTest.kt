package tk.germanbot.data

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
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
import java.util.concurrent.atomic.AtomicBoolean


/***
 * Add -Djava.library.path=build/libs to run profile in order to execute this test from IDE
 * (better add to IDEA defaults)
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(Application::class))
@WebAppConfiguration
@Import(IntegrationTestsConfig::class)
@ActiveProfiles("test")
class TranslationRepositoryIntegrationTest {
    private val EXPECTED_DE = "Hallo"
    private val EXPECTED_EN = "Hello"

    private var dynamoDBMapper: DynamoDBMapper? = null
    @Autowired
    private val db: AmazonDynamoDB? = null
    @Autowired
    internal var repository: TranslationRepository? = null

    companion object {
        private var tableCreated: AtomicBoolean = AtomicBoolean(false)
    }

    @Before
    @Throws(Exception::class)
    fun setup() {
        dynamoDBMapper = DynamoDBMapper(db)
        if (!tableCreated.get()) {
            val tableRequest = dynamoDBMapper!!
                    .generateCreateTableRequest(Translation::class.java)
            tableRequest.provisionedThroughput = ProvisionedThroughput(1L, 1L)
            db!!.createTable(tableRequest)
            tableCreated.set(true)
        }
        dynamoDBMapper!!.batchDelete(repository!!.findAll())

        val hello = Translation(de = EXPECTED_DE, en = EXPECTED_EN)
        repository?.save(hello)
    }

    @Test
    fun findAll() {
        val result = repository!!.findAll()
        assertTrue("Not empty", result.isNotEmpty())
        assertTrue("Contains item with expected translation",
                result[0].en == EXPECTED_EN)
    }

    @Test
    fun findByEnglish() {
        val result = repository!!.findByEn(EXPECTED_EN)
        assertTrue("Not empty", result.isNotEmpty())
        assertTrue("ID Not empty", result[0].id != null)
        assertTrue("Contains item with expected translation",
                result[0].de == EXPECTED_DE)
    }

    @Test
    fun findByGerman() {
        val result = repository!!.findByDe(EXPECTED_DE)
        assertTrue("Not empty", result.isNotEmpty())
        assertTrue("ID Not empty", result[0].id != null)
        assertTrue("Contains item with expected translation",
                result[0].de == EXPECTED_DE)
    }
}