package map

import android.content.Context
import android.graphics.Color
import db.Conf
import org.btcmap.R

fun Context.getOnSurfaceColor(): Int {
    val attrs = theme.obtainStyledAttributes(intArrayOf(R.attr.colorOnSurface))
    return attrs.getColor(0, 0)
}

fun Context.getPrimaryContainerColor(conf: Conf): Int {
    return if (conf.themedPins) {
        val attrs = theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimaryContainer))
        attrs.getColor(0, 0)
    } else {
        Color.parseColor("#0a7b72")
    }
}

fun Context.getOnPrimaryContainerColor(conf: Conf): Int {
    return if (conf.themedPins) {
        val attrs = theme.obtainStyledAttributes(intArrayOf(R.attr.colorOnPrimaryContainer))
        attrs.getColor(0, 0)
    } else {
        Color.WHITE
    }
}

fun Context.getErrorColor(): Int {
    val attrs = theme.obtainStyledAttributes(intArrayOf(R.attr.colorError))
    return attrs.getColor(0, 0)
}