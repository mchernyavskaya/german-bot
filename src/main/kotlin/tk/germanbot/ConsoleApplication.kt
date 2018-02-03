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
import tk.germanbot.activity.ActivityManager
import tk.germanbot.activity.UserTextMessageEvent
import tk.germanbot.messenger.MessageButton
import tk.germanbot.service.MessageGateway
import java.io.BufferedReader
import java.io.InputStreamReader

@SpringBootApplication
@EnableDynamoDBRepositories(basePackages = arrayOf("tk.germanbot.data"))
@EnableConfigurationProperties(MessengerProperties::class)
@Import(ConsoleConfig::class)
class ConsoleApplication(
        @Autowired val activityManager: ActivityManager
) : CommandLineRunner {

    override fun run(vararg args: String) {
        val br = BufferedReader(InputStreamReader(System.`in`))
        do {
            val input = br.readLine()
            activityManager.handleEvent("user", UserTextMessageEvent("user", input))
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

        override fun messageWithButtons(userId: String, message: String, buttons: List<MessageButton>) {
            println(message)
            println("[" + buttons.map(MessageButton::action).joinToString(" ") + "]")
        }

        override fun fileMessage(userId: String, fileUrl: String) {
            println(fileUrl)
        }

        override fun genericMessage(userId: String, title: String, subtitle: String) {
            println(title)
            println("->" + subtitle)
        }

        override fun textMessage(userId: String, message: String) {
            println(message)
        }

        override fun messageWithEndButton(userId: String, message: String) {
            println(message + " [Cancel]")
        }

    }

}
