package pet.pupgrammer.b4tea4t

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import pet.pupgrammer.b4tea4t.config.Config

@SpringBootApplication
class B4Tea4TApplication

fun main(args: Array<String>) {
    Config.init()

    runApplication<B4Tea4TApplication>(*args)
}
