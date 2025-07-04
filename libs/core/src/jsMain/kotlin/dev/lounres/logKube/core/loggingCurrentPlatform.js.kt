package dev.lounres.logKube.core


public actual typealias CurrentPlatformLogMetadata<Level> = JsLogMetadata<Level>

public actual inline fun <Level> JsLogger<Level>.log(
    source: String?,
    logLevel: Level,
    throwable: Throwable?,
    items: () -> Map<String, String>,
    message: () -> String
) {
    log(
        metadata = JsLogMetadata(
            loggerName = this.name,
            source = source,
            logLevel = logLevel,
        ),
        throwable = throwable,
        items = items,
        message = message,
    )
}

public actual typealias DefaultCurrentPlatformLogWriter = DefaultJsLogWriter