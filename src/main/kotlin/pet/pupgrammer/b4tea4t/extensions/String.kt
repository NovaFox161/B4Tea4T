package pet.pupgrammer.b4tea4t.extensions

fun String.embedTitleSafe(): String = this.substring(0, (256).coerceAtMost(this.length))

fun String.embedDescriptionSafe(): String = this.substring(0, (4096).coerceAtMost(this.length))

fun String.embedFieldSafe(): String = this.substring(0, (1024).coerceAtMost(this.length))

fun String.messageContentSafe(): String = this.substring(0, (2000).coerceAtMost(this.length))

fun String.textInputPlaceholderSafe(): String = this.substring(0, (100).coerceAtMost(this.length))