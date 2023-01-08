package pl.michalskrzypek.busevents

import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BusEventsApplication

fun main(args: Array<String>) {
	runBlocking {
		runApplication<BusEventsApplication>(*args)
	}
}
