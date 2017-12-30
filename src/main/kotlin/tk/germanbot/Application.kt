package tk.germanbot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties(MessengerProperties::class)
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}