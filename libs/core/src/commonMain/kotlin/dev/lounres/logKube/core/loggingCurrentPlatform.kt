package dev.lounres.logKube.core

import kotlinx.datetime.Instant


public expect class CurrentPlatformLogMetadata: LogMetadata {
    override val timestamp: Instant
    override val loggerName: String
    override val source: String?
    override val level: Level
}

public typealias CurrentPlatformLogEvent = LogEvent<CurrentPlatformLogMetadata>

public typealias CurrentPlatformLogger = Logger<CurrentPlatformLogMetadata>

public expect fun CurrentPlatformLogger(name: String, logAcceptors: List<CurrentPlatformLogAcceptor> = emptyList()): CurrentPlatformLogger

public typealias CurrentPlatformLogAcceptor = LogAcceptor<CurrentPlatformLogMetadata>

public typealias CurrentPlatformLogWriter = LogWriter<CurrentPlatformLogMetadata>

public expect object DefaultCurrentPlatformLogWriter : CurrentPlatformLogWriter {
    override fun log(event: LogEvent<CurrentPlatformLogMetadata>)
}