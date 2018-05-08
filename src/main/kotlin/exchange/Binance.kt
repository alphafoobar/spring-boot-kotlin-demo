package exchange

import info.bitrich.xchangestream.binance.BinanceStreamingExchange
import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.Ticker
import org.slf4j.LoggerFactory
import reactor.core.publisher.TopicProcessor
import reactor.core.publisher.TopicProcessor.create

class Binance {
    companion object {
        val logger = LoggerFactory.getLogger(Binance::class.java)!!
    }

    fun subscribe(): TopicProcessor<Ticker> {
        val exchange = StreamingExchangeFactory.INSTANCE.createExchange(BinanceStreamingExchange::class.java.name)

        // Connect to the Exchange WebSocket API. Blocking wait for the connection.
        val processor = create<Ticker>()
        exchange.connect(ProductSubscription.create().addTicker(CurrencyPair("BNB", "BTC")).build()).blockingAwait()
        exchange.streamingMarketDataService.getTicker(CurrencyPair("BNB", "BTC")).subscribe(
            { ticker -> processor.onNext(ticker) },
            { throwable -> processor.onError(throwable) },
            { processor.onComplete() }
        )

        return processor
    }


    private class TickerSubscriber : Observer<Ticker> {

        override fun onSubscribe(d: Disposable) {
            logger.info("subscribed")
        }

        override fun onNext(event: Ticker) {
            logger.info("message received $event")
        }

        override fun onError(error: Throwable) {
            logger.error("error", error)
        }

        override fun onComplete() {
            logger.info("complete")
        }
    }
}
