package dev.lounres.logKube.core

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Instant


@Serializable
public enum class LogLevel: Comparable<LogLevel> {
    DEBUG, INFO, WARN, ERROR
}

@Serializable
public sealed interface LogMetadata<out Level> {
    public val id: String
    public val timestamp: Instant
    public val loggerName: String
    public val source: String?
    public val logLevel: Level
}

@Serializable
public sealed interface PlatformLogMetadata<out Level>: LogMetadata<Level>

@Serializable
public data class LogEvent<out Level, out Metadata: LogMetadata<Level>>(
    val metadata: Metadata,
    val message: String,
    val stackTrace: String? = null,
    val items: Map<String, String> = mapOf(),
)

public data class Logger<Level, Metadata: LogMetadata<Level>> (
    public val name: String,
    public var logAcceptors: List<LogAcceptor<Level, Metadata>> = emptyList(),
)

public fun <Level, Metadata: LogMetadata<Level>> Logger(
    name: String,
    vararg logAcceptors: LogAcceptor<Level, Metadata>
): Logger<Level, Metadata> = Logger(name, logAcceptors.toList())

public inline fun <Level, Metadata: LogMetadata<Level>> Logger<Level, Metadata>.log(
    metadata: Metadata,
    throwable: Throwable? = null,
    items: () -> Map<String, String> = { emptyMap() },
    message: () -> String
) {
    val iterator = logAcceptors.iterator()
    if (!iterator.hasNext()) return
    var lastLogAcceptor = iterator.next()
    while (!lastLogAcceptor.acceptLog(metadata)) {
        if (!iterator.hasNext()) return
        lastLogAcceptor = iterator.next()
    }
    val logEvent = LogEvent(
        metadata = metadata,
        message = message(),
        stackTrace = throwable?.stackTraceToString(),
        items = items()
    )
    lastLogAcceptor.logWriters.forEach { it.log(logEvent) }
    for (logAcceptor in iterator)
        if (logAcceptor.acceptLog(metadata))
            logAcceptor.logWriters.forEach { it.log(logEvent) }
}

public interface LogAcceptor<Level, in Metadata: LogMetadata<Level>> {
    public fun acceptLog(logMetadata: Metadata): Boolean
    public val logWriters: List<LogWriter<Level, Metadata>>
}

public inline fun <Level, Metadata: LogMetadata<Level>> LogAcceptor(
    logWriters: List<LogWriter<Level, Metadata>>,
    crossinline acceptLogBlock: (Metadata) -> Boolean = { true }
): LogAcceptor<Level, Metadata> =
    object : LogAcceptor<Level, Metadata> {
        override val logWriters: List<LogWriter<Level, Metadata>> = logWriters
        override fun acceptLog(logMetadata: Metadata) = acceptLogBlock(logMetadata)
    }

public inline fun <Level, Metadata: LogMetadata<Level>> LogAcceptor(vararg logWriters: LogWriter<Level, Metadata>, crossinline acceptLogBlock: (Metadata) -> Boolean = { true }): LogAcceptor<Level, Metadata> =
    object : LogAcceptor<Level, Metadata> {
        override val logWriters: List<LogWriter<Level, Metadata>> = logWriters.toList()
        override fun acceptLog(logMetadata: Metadata) = acceptLogBlock(logMetadata)
    }

public fun interface LogWriter<Level, in Metadata: LogMetadata<Level>> {
    public fun log(event: LogEvent<Level, Metadata>)
}

public object DefaultLogWriter : LogWriter<LogLevel, LogMetadata<LogLevel>> {
    override fun log(event: LogEvent<LogLevel, LogMetadata<LogLevel>>) {
        val localDateTime = event.metadata.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())

        val date = localDateTime.date

        val localTime = localDateTime.time
        val hour = localTime.hour.toString().padStart(2, '0')
        val minute = localTime.minute.toString().padStart(2, '0')
        val second = localTime.second.toString().padStart(2, '0')
        val nanosecond = localTime.nanosecond.toString().padStart(9, '0')

        val level = event.metadata.logLevel.toString().padEnd(5, ' ')
        val loggerName = event.metadata.loggerName
        val sourceName = event.metadata.source
        val message = event.message

        println(
            buildString {
                appendLine("$date $hour:$minute:$second.$nanosecond [$level] $loggerName : ${sourceName ?: "<no source>"} : $message")
                
                event.items.forEach { (key, value) -> appendLine("    $key :\n${value.prependIndent("        ")}") }
                
                if (event.stackTrace != null) {
                    appendLine("    <<<STACKTRACE>>>")
                    appendLine(event.stackTrace.prependIndent("        "))
                }
            }
        )
    }
}