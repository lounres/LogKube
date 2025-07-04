package dev.lounres.logKube.core

import kotlin.time.Instant


public expect class CurrentPlatformLogMetadata<Level>: LogMetadata<Level> {
    override val id: String
    override val timestamp: Instant
    override val loggerName: String
    override val source: String?
    override val logLevel: Level
}

public typealias CurrentPlatformLogEvent<Level> = LogEvent<Level, CurrentPlatformLogMetadata<Level>>

public typealias CurrentPlatformLogger<Level> = Logger<Level, CurrentPlatformLogMetadata<Level>>

public expect inline fun <Level> CurrentPlatformLogger<Level>.log(
    source: String? = null,
    logLevel: Level,
    throwable: Throwable? = null,
    items: () -> Map<String, String> = { emptyMap() },
    message: () -> String
)

public inline fun CurrentPlatformLogger<LogLevel>.debug(
    source: String? = null,
    throwable: Throwable? = null,
    items: () -> Map<String, String> = { emptyMap() },
    message: () -> String
) {
    log(
        source = source,
        logLevel = LogLevel.DEBUG,
        throwable = throwable,
        items = items,
        message = message,
    )
}

public inline fun CurrentPlatformLogger<LogLevel>.info(
    source: String? = null,
    throwable: Throwable? = null,
    items: () -> Map<String, String> = { emptyMap() },
    message: () -> String
) {
    log(
        source = source,
        logLevel = LogLevel.INFO,
        throwable = throwable,
        items = items,
        message = message,
    )
}

public inline fun CurrentPlatformLogger<LogLevel>.warn(
    source: String? = null,
    throwable: Throwable? = null,
    items: () -> Map<String, String> = { emptyMap() },
    message: () -> String
) {
    log(
        source = source,
        logLevel = LogLevel.WARN,
        throwable = throwable,
        items = items,
        message = message,
    )
}

public inline fun CurrentPlatformLogger<LogLevel>.error(
    source: String? = null,
    throwable: Throwable? = null,
    items: () -> Map<String, String> = { emptyMap() },
    message: () -> String
) {
    log(
        source = source,
        logLevel = LogLevel.ERROR,
        throwable = throwable,
        items = items,
        message = message,
    )
}

public typealias CurrentPlatformLogAcceptor<Level> = LogAcceptor<Level, CurrentPlatformLogMetadata<Level>>

public typealias CurrentPlatformLogWriter<Level> = LogWriter<Level, CurrentPlatformLogMetadata<Level>>

public expect object DefaultCurrentPlatformLogWriter : CurrentPlatformLogWriter<LogLevel> {
    override fun log(event: LogEvent<LogLevel, CurrentPlatformLogMetadata<LogLevel>>)
}