package dev.lounres.logKube.core

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


public enum class Level: Comparable<Level> {
    DEBUG, INFO, WARN, ERROR
}

public data class LogMetadata(
    val id: String = randomId(),
    val timestamp: Instant = timestampNow(),
    val host: String = hostname,
    val loggerName: String,
    val context: String? = threadContext(),
    val level: Level,
)

public data class LogEvent(
    val metadata: LogMetadata,
    val message: String,
    val stackTrace: String? = null,
    val items: Map<String, Any?> = mapOf(),
)

public class Logger(
    public val name: String,
    public val logAcceptors: MutableList<LogAcceptor> = mutableListOf(),
) {
    public inline fun log(level: Level, throwable: Throwable? = null, crossinline items: () -> Map<String, Any?> = { mapOf() }, crossinline message: () -> String) {
        val metadata = LogMetadata(
            loggerName = name,
            level = level
        )
        val event by lazy {
            LogEvent(
                metadata = metadata,
                message = message(),
                stackTrace = throwable?.stackTraceToString(),
                items = items()
            )
        }
        for (logAcceptor in logAcceptors)
            if (logAcceptor.acceptLog(metadata))
                for (writer in logAcceptor.logWriters)
                    writer.log(event)
    }

    public inline fun debug(throwable: Throwable? = null, crossinline items: () -> Map<String, Any?> = { mapOf() }, crossinline message: () -> String) {
        log(
            level = Level.DEBUG,
            throwable = throwable,
            items = items,
            message = message
        )
    }

    public inline fun info(throwable: Throwable? = null, crossinline items: () -> Map<String, Any?> = { mapOf() }, crossinline message: () -> String) {
        log(
            level = Level.INFO,
            throwable = throwable,
            items = items,
            message = message
        )
    }

    public inline fun warn(throwable: Throwable? = null, crossinline items: () -> Map<String, Any?> = { mapOf() }, crossinline message: () -> String) {
        log(
            level = Level.WARN,
            throwable = throwable,
            items = items,
            message = message
        )
    }

    public inline fun error(throwable: Throwable? = null, crossinline items: () -> Map<String, Any?> = { mapOf() }, crossinline message: () -> String) {
        log(
            level = Level.ERROR,
            throwable = throwable,
            items = items,
            message = message
        )
    }

    public companion object
}

public interface LogAcceptor {
    public fun acceptLog(logMetadata: LogMetadata): Boolean
    public val logWriters: MutableList<LogWriter>
}

public inline fun LogAcceptor(logWriters: MutableList<LogWriter>, crossinline acceptLogBlock: (LogMetadata) -> Boolean = { true }): LogAcceptor =
    object : LogAcceptor {
        override val logWriters: MutableList<LogWriter> = logWriters
        override fun acceptLog(logMetadata: LogMetadata) = acceptLogBlock(logMetadata)
    }

public inline fun LogAcceptor(vararg logWriters: LogWriter, crossinline acceptLogBlock: (LogMetadata) -> Boolean = { true }): LogAcceptor =
    object : LogAcceptor {
        override val logWriters: MutableList<LogWriter> = mutableListOf(*logWriters)
        override fun acceptLog(logMetadata: LogMetadata) = acceptLogBlock(logMetadata)
    }

public fun interface LogWriter {
    public fun log(event: LogEvent)
}

public object CommonLogWriter : LogWriter {
    override fun log(event: LogEvent) {
        val localDateTime = event.metadata.timestamp.toLocalDateTime(TimeZone.UTC)

        val date = localDateTime.date

        val localTime = localDateTime.time
        val hour = localTime.hour.toString().padStart(2, '0')
        val minute = localTime.minute.toString().padStart(2, '0')
        val second = localTime.second.toString().padStart(2, '0')
        val nanosecond = localTime.nanosecond.toString().padStart(9, '0')

        val level = event.metadata.level
        val context = event.metadata.context
        val loggerName = event.metadata.loggerName
        val message = event.message

        println("$date $hour:$minute:$second.$nanosecond $level [$context] $loggerName : $message")
    }
}