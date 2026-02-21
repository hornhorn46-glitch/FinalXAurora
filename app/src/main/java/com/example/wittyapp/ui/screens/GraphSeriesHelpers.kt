package com.example.wittyapp.ui.screens

import com.example.wittyapp.ui.SpaceWeatherViewModel
import com.example.wittyapp.ui.strings.AppStrings
import kotlin.math.max
import kotlin.math.min

fun build24hSeries(vm: SpaceWeatherViewModel, strings: AppStrings): List<GraphSeries> {
    val kp = vm.state.kpSeries24h
    val sp = vm.state.speedSeries24h
    val bz = vm.state.bzSeries24h

    fun bounds(values: List<Double>, pad: Double): Pair<Double, Double> {
        if (values.isEmpty()) return 0.0 to 1.0
        val mn = values.minOrNull() ?: 0.0
        val mx = values.maxOrNull() ?: 1.0
        val p = max(0.001, (mx - mn) * pad)
        return (mn - p) to (mx + p)
    }

    val (kpMin, kpMax) = 0.0 to 9.0
    val (spMin, spMax) = bounds(sp.map { it.v }, 0.15)
    val (bzMin, bzMax) = bounds(bz.map { it.v }, 0.20)

    return listOf(
        GraphSeries(
            title = "Kp",
            unit = "",
            points = kp,
            minY = kpMin,
            maxY = kpMax,
            gridStepY = 1.0,
            dangerAbove = 7.0
        ),
        GraphSeries(
            title = "Speed",
            unit = "км/с",
            points = sp,
            minY = min(250.0, spMin),
            maxY = max(1200.0, spMax),
            gridStepY = 100.0,
            dangerAbove = 750.0
        ),
        GraphSeries(
            title = "Bz",
            unit = "нТл",
            points = bz,
            minY = min(-20.0, bzMin),
            maxY = max(20.0, bzMax),
            gridStepY = 2.0,
            dangerBelow = -6.0
        )
    )
}