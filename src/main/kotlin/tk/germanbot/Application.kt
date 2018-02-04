package tk.germanbot

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

@SpringBootApplication
@EnableCaching
@ComponentScan(excludeFilters = arrayOf(
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value =  ConsoleConfig::class),
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value =  ConsoleApplication::class)
) )
@EnableDynamoDBRepositories(basePackages = arrayOf("tk.germanbot.data"))
@EnableConfigurationProperties(MessengerProperties::class)
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}