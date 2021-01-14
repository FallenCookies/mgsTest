package com.example.demo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import kotlin.random.Random


class SequenceList {
    var content: List<List<Int>>? = null
        private set

    constructor() {}
    constructor(content: List<List<Int>>?) {
        this.content = content
    }
}

class GeneratingStatus {
    var status: Boolean = false;

}

class ArrayLength {
    var length: String = ""
}

var isGenerating = true;


//@EnableAsync
@Controller
class AutoSequenceController {
//    @Async
//    @Scheduled(fixedRate = 1000)
    @MessageMapping("/autoGenerate")
    @SendTo("/topic/greetings")
    @Throws(Exception::class)
    @Scheduled(fixedRate = 5000)
    fun test(generatingStatus: GeneratingStatus): SequenceList {
        if (primeMatrix.isEmpty())
            generatePrimeMatrix(Random.nextInt(10, 100))
        return SequenceList(getSequences())
    }
}

@Controller
class SequenceController {
    @MessageMapping("/generate")
    @SendTo("/topic/greetings")
    @Throws(Exception::class)
    fun sendSequences(arrayLength: ArrayLength): SequenceList {
        if (arrayLength.length.toIntOrNull() == null || arrayLength.length.toInt() < 6) {
            println("invalid argument")
            return SequenceList()
        }
        generatePrimeMatrix(arrayLength.length.toInt())
        return SequenceList(getSequences())
    }
}

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/gs-guide-websocket").withSockJS()
    }
}

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
