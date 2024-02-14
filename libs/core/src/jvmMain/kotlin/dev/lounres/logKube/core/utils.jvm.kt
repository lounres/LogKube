package dev.lounres.logKube.core

import java.net.InetAddress


internal actual fun threadContext(): String? = Thread.currentThread().name

internal actual val hostname: String = InetAddress.getLocalHost().hostName