package dev.lounres.logKube.core

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable


@Serializable
public enum class Level: Comparable<Level> {
    DEBUG, INFO, WARN, ERROR
}

@Serializable
public sealed interface LogMetadata {
//    public val id: String
    public val timestamp: Instant
    public val loggerName: String
    public val source: String?
    public val level: Level
}

@Serializable
public data class LogEvent<out LM: LogMetadata>(
    val metadata: LM,
    val message: String,
    val stackTrace: String? = null,
    val items: Map<String, String> = mapOf(),
)

public interface Logger<LM: LogMetadata>{
    public val name: String
    public val logAcceptors: List<LogAcceptor<LM>>

    public fun log(source: String? = null, level: Level, throwable: Throwable? = null, items: () -> Map<String, String> = { emptyMap() }, message: () -> String)

    public fun debug(source: String? = null, throwable: Throwable? = null, items: () -> Map<String, String> = { mapOf() }, message: () -> String) {
        log(
            source = source,
            level = Level.DEBUG,
            throwable = throwable,
            items = items,
            message = message
        )
    }

    public fun info(source: String? = null, throwable: Throwable? = null, items: () -> Map<String, String> = { mapOf() }, message: () -> String) {
        log(
            source = source,
            level = Level.INFO,
            throwable = throwable,
            items = items,
            message = message
        )
    }

    public fun warn(source: String? = null, throwable: Throwable? = null, items: () -> Map<String, String> = { mapOf() }, message: () -> String) {
        log(
            source = source,
            level = Level.WARN,
            throwable = throwable,
            items = items,
            message = message
        )
    }

    public fun error(source: String? = null, throwable: Throwable? = null, items: () -> Map<String, String> = { mapOf() }, message: () -> String) {
        log(
            source = source,
            level = Level.ERROR,
            throwable = throwable,
            items = items,
            message = message
        )
    }
}

public interface LogAcceptor<in LM: LogMetadata> {
    public fun acceptLog(logMetadata: LM): Boolean
    public val logWriters: List<LogWriter<LM>>
}

public inline fun <LM: LogMetadata> LogAcceptor(logWriters: List<LogWriter<LM>>, crossinline acceptLogBlock: (LogMetadata) -> Boolean = { true }): LogAcceptor<LM> =
    object : LogAcceptor<LM> {
        override val logWriters: List<LogWriter<LM>> = logWriters
        override fun acceptLog(logMetadata: LM) = acceptLogBlock(logMetadata)
    }

public inline fun <LM: LogMetadata> LogAcceptor(vararg logWriters: LogWriter<LM>, crossinline acceptLogBlock: (LogMetadata) -> Boolean = { true }): LogAcceptor<LM> =
    object : LogAcceptor<LM> {
        override val logWriters: List<LogWriter<LM>> = logWriters.toList()
        override fun acceptLog(logMetadata: LM) = acceptLogBlock(logMetadata)
    }

public fun interface LogWriter<in LM: LogMetadata> {
    public fun log(event: LogEvent<LM>)
}

public object DefaultLogWriter : LogWriter<LogMetadata> {
    override fun log(event: LogEvent<LogMetadata>) {
        val localDateTime = event.metadata.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())

        val date = localDateTime.date

        val localTime = localDateTime.time
        val hour = localTime.hour.toString().padStart(2, '0')
        val minute = localTime.minute.toString().padStart(2, '0')
        val second = localTime.second.toString().padStart(2, '0')
        val nanosecond = localTime.nanosecond.toString().padStart(9, '0')

        val level = event.metadata.level.toString().padEnd(5, ' ')
        val loggerName = event.metadata.loggerName
        val sourceName = event.metadata.source
        val message = event.message

        println(
            buildString {
                appendLine("$date $hour:$minute:$second.$nanosecond [$level] $loggerName : ${sourceName ?: "<no source>"} : $message")
                
                event.items.forEach { (key, value) -> appendLine("    $key :\n${value.toString().lines().joinToString(separator = "\n") { "        $it" }}") }
                
                if (event.stackTrace != null) {
                    appendLine("<<<STACKTRACE>>>")
                    appendLine(event.stackTrace)
                }
            }
        )
    }
}