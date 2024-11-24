package dev.lounres.logKube.core


public actual typealias CurrentPlatformLogMetadata = JvmLogMetadata

public actual fun CurrentPlatformLogger(name: String, logAcceptors: List<JvmLogAcceptor>): CurrentPlatformLogger = JvmLogger(name, logAcceptors)

public actual typealias DefaultCurrentPlatformLogWriter = DefaultJvmLogWriter