package com.example.wittyapp.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class AuroraPrediction(
    val score: Int,
    val title: String,
    val details: String
)

/**
 * Простая эвристика на 3 часа: чем больше скорость и чем более отрицательный Bz,
 * тем выше шанс активности. Kp добавляет уверенности.
 */
fun predictAurora3h(
    kpAvg: Double?,
    speedAvg: Double?,
    bzAvg: Double?
): AuroraPrediction {
    if (kpAvg == null || speedAvg == null || bzAvg == null) {
        return AuroraPrediction(0, "нет данных", "Открой экран ещё раз или нажми обновить.")
    }

    val bzDown = max(0.0, -bzAvg) // интересует только "вниз"
    val s = speedAvg

    // нормировки (широкие, с запасом)
    val bzN = (bzDown / 10.0).coerceIn(0.0, 1.2)
    val spN = ((s - 300.0) / 600.0).coerceIn(0.0, 1.2)
    val kpN = (kpAvg / 9.0).coerceIn(0.0, 1.0)

    var score = 100.0 * (0.45 * bzN + 0.35 * spN + 0.20 * kpN)
    // штраф, если Bz вверх (плохо для активности)
    if (bzAvg > 2.0) score *= 0.65

    val sInt = score.coerceIn(0.0, 100.0).toInt()
    val title = when {
        sInt >= 75 -> "высокий шанс"
        sInt >= 45 -> "средний шанс"
        sInt >= 20 -> "низкий шанс"
        else -> "скорее тихо"
    }

    val details = buildString {
        append("Средние за 3ч: Kp≈${"%.1f".format(kpAvg)}, ")
        append("Speed≈${"%.0f".format(speedAvg)} км/с, ")
        append("Bz≈${"%.1f".format(bzAvg)} нТл. ")
        append("Чем ниже (отрицательнее) Bz и выше скорость — тем лучше.")
    }

    return AuroraPrediction(sInt, title, details)
}
