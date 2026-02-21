package com.example.wittyapp.domain

import java.time.Instant

data class KpSample(val t: Instant, val kp: Double)
data class WindSample(val t: Instant, val speed: Double, val density: Double?)
data class MagSample(val t: Instant, val bx: Double?, val by: Double?, val bz: Double?)
data class GraphPoint(val t: Instant, val v: Double)
