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
    private val EXPECTED_GE = "Hallo"
    private val EXPECTED_EN = "Hello"

    private var dynamoDBMapper: DynamoDBMapper? = null
    @Autowired
    private val amazonDynamoDB: AmazonDynamoDB? = null
    @Autowired
    internal var repository: TranslationRepository? = null

    @Before
    @Throws(Exception::class)
    fun setup() {
        dynamoDBMapper = DynamoDBMapper(amazonDynamoDB)
        val tableRequest = dynamoDBMapper!!
                .generateCreateTableRequest(Translation::class.java)
        tableRequest.provisionedThroughput = ProvisionedThroughput(1L, 1L)
        amazonDynamoDB!!.createTable(tableRequest)

        dynamoDBMapper!!.batchDelete(repository!!.findAll())
    }

    @Test
    fun sampleTestCase() {
        val hello = Translation("123", EXPECTED_GE, EXPECTED_EN)
        repository?.save(hello)

        val result = repository!!.findAll() as List<Translation>
        assertTrue("Not empty", result.isNotEmpty())
        assertTrue("Contains item with expected translation",
                result[0].en == EXPECTED_EN)
    }
}