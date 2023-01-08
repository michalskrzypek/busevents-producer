package pl.michalskrzypek.busevents.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import pl.michalskrzypek.busevents.producer.BusEventsProducer

@RestController
class BusEventController(
    private val busEventsProducer: BusEventsProducer
) {

    @GetMapping("/start/{busId}")
    fun publish(@PathVariable busId: Int) {
        busEventsProducer.start(busId)
    }

    @GetMapping("/stop/{busId}")
    fun stop(@PathVariable busId: Int) {
        busEventsProducer.stop()
    }
}