package dev.lounres.logKube.core

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable


@Serializable
public data class JvmLogMetadata(
//    val id: String = randomId(),
    override val timestamp: Instant = Clock.System.now(),
    override val loggerName: String,
    val threadContext: String,
    override val source: String? = null,
    override val level: Level,
): LogMetadata

public typealias JvmLogEvent = LogEvent<JvmLogMetadata>

public typealias JvmLogger = Logger<JvmLogMetadata>

public typealias JvmLogAcceptor = LogAcceptor<JvmLogMetadata>

public typealias JvmLogWriter = LogWriter<JvmLogMetadata>

public object DefaultJvmLogWriter : JvmLogWriter {
    override fun log(event: LogEvent<JvmLogMetadata>) {
        val localDateTime = event.metadata.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())

        val date = localDateTime.date

        val localTime = localDateTime.time
        val hour = localTime.hour.toString().padStart(2, '0')
        val minute = localTime.minute.toString().padStart(2, '0')
        val second = localTime.second.toString().padStart(2, '0')
        val nanosecond = localTime.nanosecond.toString().padStart(9, '0')

        val level = event.metadata.level.toString().padEnd(5, ' ')
        val threadContext = event.metadata.threadContext
        val loggerName = event.metadata.loggerName
        val sourceName = event.metadata.source
        val message = event.message
        
        println(
            buildString {
                appendLine("$date $hour:$minute:$second.$nanosecond [$level] [$threadContext] $loggerName : ${sourceName ?: "<no source>"} : $message")
                
                event.items.forEach { (key, value) -> appendLine("    $key :\n${value.toString().lines().joinToString(separator = "\n") { "        $it" }}") }
                
                if (event.stackTrace != null) {
                    appendLine("<<<STACKTRACE>>>")
                    appendLine(event.stackTrace)
                }
            }
        )
    }
}