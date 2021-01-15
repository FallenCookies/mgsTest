package com.example.demo

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

class MessageHandler : TextWebSocketHandler() {

    var currentSession: WebSocketSession? = null
    var isGenerating = AtomicBoolean(false)
    var watcher = GlobalScope.launch { // launch a new coroutine in background and continue
        while (true) {
            delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
            if (isGenerating.get()) {
                emit(currentSession,Message("sequences", getSequences())) // print after delay
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
                generatePrimeMatrix(length)
                emit(currentSession, Message("sequences", getSequences()))
            }
            "/autoGenerate" -> {
                isGenerating.set(json.get("isGenerating").asBoolean())
                if (primeMatrix.isEmpty())
                    generatePrimeMatrix(Random.nextInt(10, 100))
            }
        }
    }

    private fun emit(session: WebSocketSession?, msg: Message){
        if(session != null)
            session.sendMessage(TextMessage(jacksonObjectMapper().writeValueAsString(msg)))
    }
}