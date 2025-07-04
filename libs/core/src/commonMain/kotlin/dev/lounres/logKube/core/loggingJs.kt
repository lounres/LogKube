package dev.lounres.logKube.core

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid


@Serializable
public data class JsLogMetadata<Level>(
    override val id: String = Uuid.random().toString(),
    override val timestamp: Instant = Clock.System.now(),
    override val loggerName: String,
    override val source: String? = null,
    override val logLevel: Level,
): PlatformLogMetadata<Level>

public typealias JsLogEvent<Level> = LogEvent<Level, JsLogMetadata<Level>>

public typealias JsLogger<Level> = Logger<Level, JsLogMetadata<Level>>

public typealias JsLogAcceptor<Level> = LogAcceptor<Level, JsLogMetadata<Level>>

public typealias JsLogWriter<Level> = LogWriter<Level, JsLogMetadata<Level>>

public object DefaultJsLogWriter : JsLogWriter<LogLevel> {
    override fun log(event: LogEvent<LogLevel, JsLogMetadata<LogLevel>>) {
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