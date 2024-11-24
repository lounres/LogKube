package dev.lounres.logKube.core


public fun WasmJsLogger(name: String, logAcceptors: List<WasmJsLogAcceptor> = emptyList()): WasmJsLogger =
    object : WasmJsLogger {
        override val name: String = name
        override val logAcceptors: List<WasmJsLogAcceptor> = logAcceptors

        override fun log(source: String?, level: Level, throwable: Throwable?, items: () -> Map<String, String>, message: () -> String) {
            val metadata = WasmJsLogMetadata(
                loggerName = name,
                source = source,
                level = level,
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
    }