package com.example.wittyapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wittyapp.domain.GraphPoint
import com.example.wittyapp.domain.parseKp1m
import com.example.wittyapp.domain.parseMag1m
import com.example.wittyapp.domain.parseWind1m
import com.example.wittyapp.domain.predictAurora3h
import com.example.wittyapp.net.SpaceWeatherApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

data class SpaceWeatherUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val updatedAt: Instant? = null,

    val kpNow: Double? = null,
    val speedNow: Double? = null,
    val densityNow: Double? = null,
    val bxNow: Double? = null,
    val bzNow: Double? = null,

    val auroraScore: Int = 0,
    val auroraTitle: String = "",
    val auroraDetails: String = "",

    val kpSeries24h: List<GraphPoint> = emptyList(),
    val speedSeries24h: List<GraphPoint> = emptyList(),
    val bzSeries24h: List<GraphPoint> = emptyList(),
)

class SpaceWeatherViewModel(
    private val api: SpaceWeatherApi
) : ViewModel() {

    var state: SpaceWeatherUiState by mutableStateOf(SpaceWeatherUiState())
        private set

    private var autoJob: Job? = null

    fun startAutoRefresh(periodMs: Long) {
        autoJob?.cancel()
        autoJob = viewModelScope.launch {
            while (true) {
                delay(periodMs)
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                state = state.copy(loading = true, error = null)

                val kpBody = api.kp1mJson()
                val windBody = api.wind1mJson()
                val magBody = api.mag1mJson()

                val kp = parseKp1m(kpBody)
                val wind = parseWind1m(windBody)
                val mag = parseMag1m(magBody)

                val now = Instant.now()

                val kpNow = kp.lastOrNull()?.kp
                val windNow = wind.lastOrNull()
                val magNow = mag.lastOrNull()

                val since3h = now.minus(3, ChronoUnit.HOURS)
                val kp3h = kp.filter { it.t.isAfter(since3h) }.map { it.kp }.averageOrNull()
                val sp3h = wind.filter { it.t.isAfter(since3h) }.map { it.speed }.averageOrNull()
                val bz3h = mag.filter { it.t.isAfter(since3h) }.mapNotNull { it.bz }.averageOrNull()

                val pred = predictAurora3h(kp3h, sp3h, bz3h)

                val since24h = now.minus(24, ChronoUnit.HOURS)
                val kp24 = kp.filter { it.t.isAfter(since24h) }.map { GraphPoint(it.t, it.kp) }
                val sp24 = wind.filter { it.t.isAfter(since24h) }.map { GraphPoint(it.t, it.speed) }
                val bz24 = mag.filter { it.t.isAfter(since24h) }
                    .mapNotNull { s -> s.bz?.let { GraphPoint(s.t, it) } }

                state = state.copy(
                    loading = false,
                    updatedAt = now,
                    kpNow = kpNow,
                    speedNow = windNow?.speed,
                    densityNow = windNow?.density,
                    bxNow = magNow?.bx,
                    bzNow = magNow?.bz,

                    auroraScore = pred.score,
                    auroraTitle = pred.title,
                    auroraDetails = pred.details,

                    kpSeries24h = kp24,
                    speedSeries24h = sp24,
                    bzSeries24h = bz24
                )
            } catch (e: Exception) {
                state = state.copy(loading = false, error = e.message ?: e.toString())
            }
        }
    }
}

private fun List<Double>.averageOrNull(): Double? = if (isEmpty()) null else average()