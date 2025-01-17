package map

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toRect
import conf.ConfRepo
import db.Element
import icons.iconResId
import org.btcmap.R

class MapMarkersRepo(
    private val context: Context,
    private val conf: ConfRepo,
) {

    private val cache = mutableMapOf<Int?, BitmapDrawable>()

    fun getMarker(element: Element): BitmapDrawable {
        val iconResId = element.iconResId()
        var markerDrawable = cache[iconResId]

        if (markerDrawable == null) {
            markerDrawable = createMarkerIcon(iconResId).toDrawable(context.resources)
            cache[iconResId] = markerDrawable
        }

        return markerDrawable
    }

    fun invalidateCache() {
        cache.clear()
    }

    private fun createMarkerIcon(iconResId: Int?): Bitmap {
        val pinSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics
        ).toInt()

        val emptyPinDrawable = ContextCompat.getDrawable(context, R.drawable.ic_marker)!!
        DrawableCompat.setTint(emptyPinDrawable, context.getPrimaryContainerColor(conf.conf.value))
        val emptyPinBitmap = emptyPinDrawable.toBitmap(width = pinSizePx, height = pinSizePx)

        val markerIcon = createBitmap(emptyPinBitmap.width, emptyPinBitmap.height).applyCanvas {
            drawBitmap(emptyPinBitmap, 0f, 0f, Paint())
        }

        if (iconResId != null) {
            val iconFrame = RectF(
                markerIcon.width.toFloat() * 0.27f,
                markerIcon.width.toFloat() * 0.17f,
                markerIcon.width.toFloat() * 0.73f,
                markerIcon.height.toFloat() * 0.63f
            ).toRect()

            val iconDrawable = ContextCompat.getDrawable(context, iconResId)!!

            DrawableCompat.setTint(
                iconDrawable, context.getOnPrimaryContainerColor(conf.conf.value)
            )

            val iconBitmap = iconDrawable.toBitmap(
                width = iconFrame.right - iconFrame.left,
                height = iconFrame.bottom - iconFrame.top,
            )

            markerIcon.applyCanvas {
                drawBitmap(
                    iconBitmap,
                    null,
                    iconFrame,
                    Paint().apply { isAntiAlias = true },
                )
            }
        }

        return markerIcon
    }
}