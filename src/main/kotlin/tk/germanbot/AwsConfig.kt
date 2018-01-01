package tk.germanbot

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.StringUtils


@ConfigurationProperties(prefix = "aws.dynamodb")
class AwsProperties {
    var endpoint: String = ""
    var accessKey: String = ""
    var secretKey: String = ""
}

@Configuration
@EnableConfigurationProperties(AwsProperties::class)
class AwsConfig(@Autowired val props: AwsProperties) {
    private val logger = LoggerFactory.getLogger(AwsConfig::class.java)

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB {
        val amazonDynamoDB = AmazonDynamoDBClient(amazonAWSCredentials())
        if (!StringUtils.isEmpty(props.endpoint)) {
            logger.debug("Initializing AWS Dynamo db with endpoint {}, access key {}, secret {}",
                    props.endpoint, props.accessKey, props.secretKey)
            amazonDynamoDB.setEndpoint(props.endpoint)
        }
        return amazonDynamoDB
    }

    @Bean
    fun amazonAWSCredentials(): AWSCredentials {
        return BasicAWSCredentials(props.accessKey, props.secretKey)
    }
}