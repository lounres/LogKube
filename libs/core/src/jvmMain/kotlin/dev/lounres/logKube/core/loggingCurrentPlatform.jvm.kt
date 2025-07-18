package dev.lounres.logKube.core


public actual typealias CurrentPlatformLogMetadata<Level> = JvmLogMetadata<Level>

public actual inline fun <Level> CurrentPlatformLogger<Level>.log(
    source: String?,
    logLevel: Level,
    throwable: Throwable?,
    items: () -> Map<String, String>,
    message: () -> String
) {
    log(
        metadata = JvmLogMetadata(
            loggerName = this.name,
            threadContext = Thread.currentThread().name,
            source = source,
            logLevel = logLevel,
        ),
        throwable = throwable,
        items = items,
        message = message,
    )
}

public actual typealias DefaultCurrentPlatformLogWriter = DefaultJvmLogWriter