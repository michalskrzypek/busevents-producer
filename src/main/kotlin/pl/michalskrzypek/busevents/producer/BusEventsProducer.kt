package pl.michalskrzypek.busevents.producer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.List
import kotlin.random.Random

@Service
class BusEventsProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    companion object {
        private const val PASSENGERS_MAX_COUNT = 500
        private const val BUS_STOP_INTERVAL_IN_MS: Long = 1000 * 60 * 1
        private val LOG = LoggerFactory.getLogger(BusEventsProducer::class.java)
        private const val LOG_FORMAT = "{} - {}"
    }

    private val passengersInBusMap = Collections.synchronizedMap<Int, MutableList<Int>>(mapOf())

    @Async
    fun start(busId: Int) {
        LOG.info(LOG_FORMAT, "$busId", "Starting bus entrance events producer process...")
        passengersInBusMap[busId] = mutableListOf()
        val passengersInBus = passengersInBusMap[busId]!!

        runBlocking {
            while (true) {
                LOG.info(LOG_FORMAT, "$busId", "NEW STOP!")
                val passengersInBusCount = AtomicInteger(passengersInBus.size)
                LOG.info(LOG_FORMAT, "$busId", "Passengers in a bus: $passengersInBusCount")
                val passengersToExit = Random.nextInt(0, passengersInBusCount.get() + 1)
                LOG.info(LOG_FORMAT, "$busId", "Passengers to exit: $passengersToExit")
                for (i in 1..passengersToExit) {
                    launch {
                        val busEntranceData = BusEntranceData(
                            LocalDateTime.now().toString(),
                            passengersInBus.removeFirst(),
                            busId,
                            EntranceType.OUT
                        )
                        kafkaTemplate.send(KafkaTopics.BUS_ENTRANCE, busEntranceData)
                    }
                }
                passengersInBusCount.updateAndGet { it - passengersToExit }
                LOG.info(LOG_FORMAT, "$busId", "$passengersInBusCount passengers stayed after exit")

                val newPassengersCount = Random.nextInt(0, PASSENGERS_MAX_COUNT - passengersInBusCount.get() + 1)
                LOG.info(LOG_FORMAT, "$busId", "New stop! $newPassengersCount passengers coming!")
                for (i in 1..newPassengersCount) {
                    launch(Dispatchers.IO) {
                        val pId = Random.nextInt(10000)
                        passengersInBus.add(pId)

                        val busEntranceData = BusEntranceData(
                            LocalDateTime.now().toString(),
                            pId,
                            busId,
                            EntranceType.IN
                        )
                        kafkaTemplate.send(KafkaTopics.BUS_ENTRANCE, busEntranceData)
                    }
                }
                delay(BUS_STOP_INTERVAL_IN_MS)
            }
        }
    }

    @Async
    fun stop() {
        kafkaTemplate.flush()
        LOG.info("KAFKA PRODUCERS STOPPED!")
    }
}