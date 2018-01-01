package tk.germanbot

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.util.StringUtils


@ConfigurationProperties(prefix = "aws.dynamodb")
class AwsProperties {
    var region: String = ""
    var endpoint: String = ""
    var accessKey: String = ""
    var secretKey: String = ""
}

@Configuration
@EnableConfigurationProperties(AwsProperties::class)
@Profile("!test")
class AwsConfig(@Autowired val props: AwsProperties,
                @Autowired val environment: Environment) {
    private val logger = LoggerFactory.getLogger(AwsConfig::class.java)

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB {
        val builder = AmazonDynamoDBClientBuilder.standard()
        if (!StringUtils.isEmpty(props.endpoint)) {
            if (environment.activeProfiles.contains("dev")) {
                builder.setEndpointConfiguration(
                        AwsClientBuilder.EndpointConfiguration(props.endpoint, props.region))
            } else {
                builder.region = props.region
            }
            logger.debug("Initializing AWS Dynamo db with endpoint {}, region {}, access key {}, secret {}",
                    props.endpoint, props.region, props.accessKey, props.secretKey)
        }
        return builder.build()
    }
}