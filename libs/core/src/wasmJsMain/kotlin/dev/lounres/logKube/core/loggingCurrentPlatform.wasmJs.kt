package dev.lounres.logKube.core


public actual typealias CurrentPlatformLogMetadata = WasmJsLogMetadata

public actual fun CurrentPlatformLogger(name: String, logAcceptors: List<WasmJsLogAcceptor>): CurrentPlatformLogger = WasmJsLogger(name, logAcceptors)

public actual typealias DefaultCurrentPlatformLogWriter = DefaultWasmJsLogWriter