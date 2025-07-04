package dev.lounres.logKube.core


public actual typealias CurrentPlatformLogMetadata<Level> = WasmJsLogMetadata<Level>

public actual inline fun <Level> WasmJsLogger<Level>.log(
    source: String?,
    logLevel: Level,
    throwable: Throwable?,
    items: () -> Map<String, String>,
    message: () -> String
) {
    log(
        metadata = WasmJsLogMetadata(
            loggerName = this.name,
            source = source,
            logLevel = logLevel,
        ),
        throwable = throwable,
        items = items,
        message = message,
    )
}

public actual typealias DefaultCurrentPlatformLogWriter = DefaultWasmJsLogWriter