package tk.germanbot

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.util.StringUtils
import tk.germanbot.data.QUIZ_TABLE_NANE
import tk.germanbot.data.QUIZ_TOPIC_TABLE_NANE
import tk.germanbot.data.Quiz
import tk.germanbot.data.QuizTopic
import tk.germanbot.data.USER_QUIIZ_STAT_TABLE_NANE
import tk.germanbot.data.UserQuizStat
import javax.annotation.PostConstruct


@ConfigurationProperties(prefix = "aws.dynamodb")
class AwsProperties {
    var region: String = ""
    var endpoint: String = ""
    var accessKey: String = ""
    var secretKey: String = ""
    var readThroughput: Long = 2
    var writeThroughput: Long = 3
}

@Configuration
@EnableConfigurationProperties(AwsProperties::class)
@Profile("!test")
class AwsConfig(
        @Autowired private val props: AwsProperties,
        @Autowired private val dynamoTools: DynamoTools) {
    private val logger = LoggerFactory.getLogger(AwsConfig::class.java)

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB {
        val builder = AmazonDynamoDBClientBuilder.standard()
        if (StringUtils.isEmpty(props.endpoint)) {
            builder.region = props.region
        } else {
            builder.setEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration(props.endpoint, props.region))
        }
        logger.debug("Initializing AWS Dynamo db with endpoint {}, region {}, access key {}, secret {}",
                props.endpoint, props.region, props.accessKey, props.secretKey)
        return builder.build()
    }

    @Bean
    fun dynamoDBMapper(@Autowired client: AmazonDynamoDB) : DynamoDBMapper{
        return DynamoDBMapper(client)
    }

    @PostConstruct
    fun initialize() {
        val dynamoDB = amazonDynamoDB()
        val dbMapper = DynamoDBMapper(dynamoDB)
        dynamoTools.createTableIfNotExist(dbMapper, dynamoDB, Quiz::class.java, QUIZ_TABLE_NANE)
        dynamoTools.createTableIfNotExist(dbMapper, dynamoDB, QuizTopic::class.java, QUIZ_TOPIC_TABLE_NANE)
        dynamoTools.createTableIfNotExist(dbMapper, dynamoDB, UserQuizStat::class.java, USER_QUIIZ_STAT_TABLE_NANE)
    }

}

