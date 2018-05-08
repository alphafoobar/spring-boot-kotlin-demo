package service

import exchange.Binance
import org.knowm.xchange.dto.marketdata.Ticker
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Flux
import reactor.core.publisher.TopicProcessor

@Configuration
class Configuration {

    @Bean
    fun subscribe(): Flux<Ticker> {
        return Binance().subscribe()
    }

    @Bean
    fun eventPublisher(): TopicProcessor<Ticker> {
        return TopicProcessor.create<Ticker>()
    }

    @Bean
    fun events(eventPublisher: TopicProcessor<Ticker>): Flux<Ticker> {
        return eventPublisher.replay(5).autoConnect()
    }

    @Bean
    fun webSocketMapping(events: Flux<Ticker>): HandlerMapping {
        val map = HashMap<String, Any>()
        map["/websocket/binance"] = OrderHandler(events = events)
        val simpleUrlHandlerMapping = SimpleUrlHandlerMapping()
        simpleUrlHandlerMapping.urlMap = map

        //Without the order things break :-/
        simpleUrlHandlerMapping.order = 10
        return simpleUrlHandlerMapping
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }
}

