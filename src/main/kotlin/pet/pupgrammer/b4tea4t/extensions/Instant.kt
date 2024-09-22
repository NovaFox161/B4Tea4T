package pet.pupgrammer.b4tea4t.extensions

import java.time.Instant

fun Instant.isExpiredTtl(): Boolean = Instant.now().isAfter(this)