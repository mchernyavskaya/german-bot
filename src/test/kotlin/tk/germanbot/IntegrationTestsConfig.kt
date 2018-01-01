package tk.germanbot

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class IntegrationTestsConfig {

    @Bean
    fun amazonDynamoDB() : AmazonDynamoDB = DynamoDBEmbedded.create().amazonDynamoDB()

}
