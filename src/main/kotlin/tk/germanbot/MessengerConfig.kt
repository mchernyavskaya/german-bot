package tk.germanbot

import com.github.messenger4j.MessengerPlatform
import com.github.messenger4j.send.MessengerSendClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "messenger4j")
class MessengerProperties {
    var pageAccessToken: String = ""
    var appSecret: String = ""
    var verifyToken: String = ""
}

@Configuration
class MessengerConfig(@Autowired val props: MessengerProperties) {

    private val logger = LoggerFactory.getLogger(MessengerConfig::class.java)

    @Bean
    fun messengerSendClient(): MessengerSendClient {
        logger.debug("Initializing MessengerSendClient - pageAccessToken: {}", props.pageAccessToken)
        return MessengerPlatform.newSendClientBuilder(props.pageAccessToken).build()
    }
}