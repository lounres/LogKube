package dev.lounres.logKube.core

import kotlinx.datetime.Clock
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.random.nextUInt


public fun timestampNow(clock: Clock = System): Instant = clock.now()

internal fun randomId(): String = Random.nextUInt().toString(16)

internal expect fun threadContext(): String?

internal expect val hostname: String