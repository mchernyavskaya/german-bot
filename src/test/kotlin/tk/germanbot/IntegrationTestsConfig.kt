package tk.germanbot

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class IntegrationTestsConfig {

    @Bean
    fun amazonDynamoDB() : AmazonDynamoDB = DynamoDBEmbedded.create().amazonDynamoDB()

    @Bean
    fun mapper(@Autowired db: AmazonDynamoDB): DynamoDBMapper = DynamoDBMapper(db)
}
