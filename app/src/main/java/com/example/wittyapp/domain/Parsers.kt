package com.example.wittyapp.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant

private val json = Json { ignoreUnknownKeys = true }

/**
 * NOAA SWPC endpoints (JSON arrays of objects):
 * - planetary_k_index_1m.json
 * - rtsw_wind_1m.json
 * - rtsw_mag_1m.json
 *
 * We parse defensively (keys can differ slightly).
 */
fun parseKp1m(body: String): List<KpSample> =
    parseArray(body).mapNotNull { obj ->
        val t = obj.timeInstant() ?: return@mapNotNull null
        val kp = obj.doubleAny("kp_index", "kp", "kpIndex") ?: return@mapNotNull null
        KpSample(t = t, kp = kp)
    }.sortedBy { it.t }

fun parseWind1m(body: String): List<WindSample> =
    parseArray(body).mapNotNull { obj ->
        val t = obj.timeInstant() ?: return@mapNotNull null
        val speed = obj.doubleAny("proton_speed", "speed", "speed_km_s", "speed_km_per_s") ?: return@mapNotNull null
        val density = obj.doubleAny("density", "proton_density", "density_p_cm3")
        WindSample(t = t, speed = speed, density = density)
    }.sortedBy { it.t }

fun parseMag1m(body: String): List<MagSample> =
    parseArray(body).mapNotNull { obj ->
        val t = obj.timeInstant() ?: return@mapNotNull null
        val bx = obj.doubleAny("bx_gsm", "bx", "bx_gse")
        val by = obj.doubleAny("by_gsm", "by", "by_gse")
        val bz = obj.doubleAny("bz_gsm", "bz", "bz_gse")
        MagSample(t = t, bx = bx, by = by, bz = bz)
    }.sortedBy { it.t }

/** Legacy helpers kept for compatibility with earlier screens (if any). */
fun parseKpNow(jsonBody: String): Double? = parseKp1m(jsonBody).lastOrNull()?.kp

fun parsePlasmaNow(jsonBody: String): Triple<Double?, Double?, Double?> {
    val last = parseWind1m(jsonBody).lastOrNull()
    return Triple(last?.speed, last?.density, null)
}

fun parseMagBzNow(jsonBody: String): Double? = parseMag1m(jsonBody).lastOrNull()?.bz

// ---- internal helpers ----

private fun parseArray(body: String): List<JsonObject> = try {
    val el = json.parseToJsonElement(body)
    when (el) {
        is JsonArray -> el.mapNotNull { it as? JsonObject }
        else -> el.jsonArray.mapNotNull { it as? JsonObject }
    }
} catch (_: Exception) {
    emptyList()
}

private fun JsonObject.timeInstant(): Instant? {
    val s = stringAny("time_tag", "time", "timestamp", "timeTag") ?: return null
    return try { Instant.parse(s) } catch (_: Exception) { null }
}

private fun JsonObject.stringAny(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { k -> this[k]?.jsonPrimitive?.contentOrNull }

private fun JsonObject.doubleAny(vararg keys: String): Double? =
    keys.firstNotNullOfOrNull { k -> this[k]?.jsonPrimitive?.doubleOrNull }

private val kotlinx.serialization.json.JsonPrimitive.contentOrNull: String?
    get() = try { this.content } catch (_: Exception) { null }
