package com.mgstest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random


class Message(val msgType: String, val data: Any)

class MessageHandler() : TextWebSocketHandler() {

    private var currentSession: WebSocketSession? = null
    private var isGenerating = AtomicBoolean(false)
    private var primeGenerator = PrimeGenerator()

    init {
        GlobalScope.launch {
            while (true) {
                delay(10000L)
                if (isGenerating.get()) {
                    emit(currentSession, Message("sequences", primeGenerator.getSequences()))
                }
            }
        }
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        currentSession = null
        isGenerating.set(false)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        currentSession = session

    }

    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val json = ObjectMapper().readTree(message.payload)
        println(json)
        when (json.get("type").asText()) {
            "/generate" -> {
                val length = json.get("length").asInt()
                if (length < 6) {
                    println("invalid argument")
                    emit(currentSession, Message("sequences", ""))
                    return
                }
                primeGenerator.generatePrimeMatrix(length)
                emit(currentSession, Message("sequences", primeGenerator.getSequences()))
            }
            "/autoGenerate" -> {
                isGenerating.set(json.get("isGenerating").asBoolean())
                if (primeGenerator.isMatrixEmpty())
                    primeGenerator.generatePrimeMatrix(Random.nextInt(10, 100))
            }
        }
    }

    private fun emit(session: WebSocketSession?, msg: Message) {
        session?.sendMessage(TextMessage(jacksonObjectMapper().writeValueAsString(msg)))
    }
}