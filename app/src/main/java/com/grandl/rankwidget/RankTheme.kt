package com.grandl.rankwidget

import android.graphics.Color

object RankTheme {
    fun colors(tier: String): IntArray = when (tier.lowercase()) {
        "iron" -> intArrayOf(Color.rgb(80,88,92), Color.rgb(35,42,46))
        "bronze" -> intArrayOf(Color.rgb(166,100,61), Color.rgb(69,42,31))
        "silver" -> intArrayOf(Color.rgb(173,192,201), Color.rgb(65,83,94))
        "gold" -> intArrayOf(Color.rgb(232,183,73), Color.rgb(94,60,19))
        "platinum" -> intArrayOf(Color.rgb(80,220,204), Color.rgb(29,93,101))
        "emerald" -> intArrayOf(Color.rgb(38,208,126), Color.rgb(16,91,67))
        "diamond" -> intArrayOf(Color.rgb(96,176,255), Color.rgb(62,73,166))
        "master" -> intArrayOf(Color.rgb(183,101,255), Color.rgb(91,45,142))
        "grandmaster" -> intArrayOf(Color.rgb(239,75,96), Color.rgb(111,28,47))
        "challenger" -> intArrayOf(Color.rgb(245,205,93), Color.rgb(39,132,170))
        else -> intArrayOf(Color.rgb(80,220,204), Color.rgb(29,93,101))
    }
}
