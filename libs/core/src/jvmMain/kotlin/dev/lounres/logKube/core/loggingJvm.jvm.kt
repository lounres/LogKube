package dev.lounres.logKube.core


public fun JvmLogger(name: String, logAcceptors: List<JvmLogAcceptor> = emptyList()): JvmLogger =
    object : JvmLogger {
        override val name: String = name
        override val logAcceptors: List<JvmLogAcceptor> = logAcceptors

        override fun log(source: String?, level: Level, throwable: Throwable?, items: () -> Map<String, String>, message: () -> String) {
            val metadata = JvmLogMetadata(
                loggerName = name,
                threadContext = Thread.currentThread().name,
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