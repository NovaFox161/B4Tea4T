package pet.pupgrammer.b4tea4t.logger

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import org.slf4j.event.Level
import pet.pupgrammer.`4tea4t`.GitProperty
import pet.pupgrammer.b4tea4t.config.Config.APP_NAME
import pet.pupgrammer.b4tea4t.config.Config.LOGGING_WEBHOOKS_ALL_ERRORS
import pet.pupgrammer.b4tea4t.config.Config.LOGGING_WEBHOOKS_USE
import pet.pupgrammer.b4tea4t.config.Config.SECRET_WEBHOOK_DEBUG
import pet.pupgrammer.b4tea4t.config.Config.SECRET_WEBHOOK_STATUS
import pet.pupgrammer.b4tea4t.extensions.embedDescriptionSafe
import pet.pupgrammer.b4tea4t.extensions.embedFieldSafe
import pet.pupgrammer.b4tea4t.utils.GlobalValues
import pet.pupgrammer.b4tea4t.utils.GlobalValues.DEFAULT
import pet.pupgrammer.b4tea4t.utils.GlobalValues.STATUS
import java.time.Instant

class DiscordWebhookAppender : AppenderBase<ILoggingEvent>() {
    private val defaultHook: WebhookClient?
    private val statusHook: WebhookClient?
    private val useWebhooks: Boolean = LOGGING_WEBHOOKS_USE.getBoolean()
    private val allErrorsWebhook: Boolean
    private val appName: String = APP_NAME.getString()

    init {

        if (useWebhooks) {
            defaultHook = WebhookClient.withUrl(SECRET_WEBHOOK_DEBUG.getString())
            statusHook = WebhookClient.withUrl(SECRET_WEBHOOK_STATUS.getString())
            allErrorsWebhook = LOGGING_WEBHOOKS_ALL_ERRORS.getBoolean()
        } else {
            defaultHook = null
            statusHook = null
            allErrorsWebhook = false
        }
    }

    override fun append(eventObject: ILoggingEvent) {
        if (!useWebhooks) return

        when {
            eventObject.level.equals(Level.ERROR) && allErrorsWebhook -> {
                executeDefault(eventObject)
                return
            }

            eventObject.markerList.contains(STATUS) -> {
                executeStatus(eventObject)
                return
            }

            eventObject.markerList.contains(DEFAULT) -> {
                executeDefault(eventObject)
                return
            }
        }
    }

    private fun executeStatus(event: ILoggingEvent) {
        val content = WebhookEmbedBuilder()
            .setTitle(WebhookEmbed.EmbedTitle("Status", null))
            .addField(WebhookEmbed.EmbedField(true, "Application", appName))
            .addField(WebhookEmbed.EmbedField(true, "Time", "<t:${event.timeStamp / 1000}:f>"))
            .addField(WebhookEmbed.EmbedField(false, "Logger", event.loggerName.embedFieldSafe()))
            .addField(WebhookEmbed.EmbedField(true, "Level", event.level.levelStr))
            .addField(WebhookEmbed.EmbedField(true, "Thread", event.threadName.embedFieldSafe()))
            .setDescription(event.formattedMessage.embedDescriptionSafe())
            .setColor(getEmbedColor(event))
            .setFooter(WebhookEmbed.EmbedFooter("v${GitProperty.BOT_VERSION.value}", null))
            .setTimestamp(Instant.now())

        if (event.throwableProxy != null) {
            content.addField(WebhookEmbed.EmbedField(false, "Error Message", event.throwableProxy.message.embedFieldSafe()))
            content.addField(WebhookEmbed.EmbedField(false, "Stacktrace", "Stacktrace can be found in exceptions log file"))
        }

        this.statusHook?.send(content.build())
    }

    private fun executeDefault(event: ILoggingEvent) {
        val content = WebhookEmbedBuilder()
            .setTitle(WebhookEmbed.EmbedTitle(event.level.levelStr, null))
            .addField(WebhookEmbed.EmbedField(true, "Application", appName))
            .addField(WebhookEmbed.EmbedField(true, "Time", "<t:${event.timeStamp / 1000}:f>"))
            .addField(WebhookEmbed.EmbedField(false, "Logger", event.loggerName.embedFieldSafe()))
            .addField(WebhookEmbed.EmbedField(true, "Level", event.level.levelStr))
            .addField(WebhookEmbed.EmbedField(true, "Thread", event.threadName.embedFieldSafe()))
            .setDescription(event.formattedMessage.embedDescriptionSafe())
            .setColor(getEmbedColor(event))
            .setFooter(WebhookEmbed.EmbedFooter("v${GitProperty.BOT_VERSION.value}", null))
            .setTimestamp(Instant.now())

        if (event.throwableProxy != null) {
            content.addField(WebhookEmbed.EmbedField(false, "Error Message", event.throwableProxy.message.embedFieldSafe()))
            content.addField(WebhookEmbed.EmbedField(false, "Stacktrace", "Stacktrace can be found in exceptions log file"))
        }

        this.defaultHook?.send(content.build())
    }

    private fun getEmbedColor(event: ILoggingEvent): Int {
        return if (event.level.equals(Level.ERROR) || event.throwableProxy != null) GlobalValues.errorColor.rgb
        else if (event.level.equals(Level.WARN)) GlobalValues.warnColor.rgb
        else GlobalValues.embedColor.rgb
    }
}