package service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.knowm.xchange.dto.marketdata.Ticker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class OrderHandler(events: Flux<Ticker>) : WebSocketHandler {

    private val outputEvents: Flux<Ticker>
    private val mapper = ObjectMapper()

    init {
        this.outputEvents = Flux.from(events)
    }

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
                .map<String>(Function<WebSocketMessage, String> { it.getPayloadAsText() })
                .subscribe(Consumer<String> { subscriber.onNext(it) }, Consumer<Throwable> { subscriber.onError(it) }, Runnable { subscriber.onComplete() })

        return session.send(outputEvents.map<String>(Function<Ticker, String> { this.toJSON(it) }).map(Function<String, WebSocketMessage> { session.textMessage(it) }))
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

    companion object {

        private val logger = LoggerFactory.getLogger(OrderHandler::class.java.name)
    }
}
