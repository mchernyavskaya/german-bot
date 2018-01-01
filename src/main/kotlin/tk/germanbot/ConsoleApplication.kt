package tk.germanbot

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import tk.germanbot.flow.FsmController
import tk.germanbot.flow.MessageGateway
import tk.germanbot.flow.event.UserTextMessageEvent
import java.io.BufferedReader
import java.io.InputStreamReader

@SpringBootApplication
@EnableDynamoDBRepositories(basePackages = arrayOf("tk.germanbot.data"))
@EnableConfigurationProperties(MessengerProperties::class)
@Import(ConsoleConfig::class)
class ConsoleApplication(
        @Autowired val fsmController: FsmController
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val br = BufferedReader(InputStreamReader(System.`in`))
        do {
            val input = br.readLine()
            fsmController.acceptEvent("user", UserTextMessageEvent("user", input))
        } while (input != "quit")
    }

}

@Throws(Exception::class)
fun main(args: Array<String>) {
    SpringApplication.run(ConsoleApplication::class.java, *args)
}

class ConsoleConfig {

    @Bean
    @Primary
    fun msgGateway(): MessageGateway
            = object : MessageGateway {
        override fun textMessage(userId: String, message: String) {
            System.out.println(message)
        }

        override fun messageWithCancelButton(userId: String, message: String) {
            System.out.println(message + " [Cancel]")
        }

    }

}
