package service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.knowm.xchange.dto.marketdata.Ticker
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class OrderHandler(events: Flux<Ticker>) : WebSocketHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(OrderHandler::class.java.name)
    }

    private val outputEvents: Flux<Ticker> = Flux.from(events)
    private val mapper = ObjectMapper()

    private fun toJSON(ticker: Ticker): String {
        try {
            return mapper.writeValueAsString(ticker)
        } catch (e: JsonProcessingException) {
            logger.error("Couldn't write string [error=\"{}\"]", e)
        }

        return ""
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val subscriber = WebSocketMessageSubscriber()
        session.receive()
            .map<String>({ it.payloadAsText })
            .subscribe(
                { subscriber.onNext(it) },
                { subscriber.onError(it) },
                { subscriber.onComplete() })

        return session.send(outputEvents
            .map<String>({ this.toJSON(it) })
            .map({ session.textMessage(it) }))
    }

    private class WebSocketMessageSubscriber {

        fun onNext(event: String) {
            logger.info("message received $event")
        }

        fun onError(error: Throwable) {
            logger.error("error", error)
        }

        fun onComplete() {
            logger.info("complete")
        }
    }
}
