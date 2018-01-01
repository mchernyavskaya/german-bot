package tk.germanbot

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableDynamoDBRepositories(basePackages = arrayOf("tk.germanbot.data"))
@EnableConfigurationProperties(MessengerProperties::class)
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}