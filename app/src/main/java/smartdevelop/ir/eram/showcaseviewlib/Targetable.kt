package smartdevelop.ir.eram.showcaseviewlib

import android.graphics.Path
import android.graphics.RectF

/**
 * Created by Mohammad Reza Eram (https://github.com/mreram)
 * Edited by @author[northshore.org]
 */
interface Targetable {
    /**
     * This path will be used when drawing the guide.
     * @return The path that will be drawn.
     */
    fun guidePath(): Path?

    /**
     * This rect is used when displaying the guide message.
     * If the guidePath is a circle then the bounding box should
     * be a square that contains the circle inside of it.
     * @return The rect that will used for positioning guide message.
     */
    fun boundingRect(): RectF?
}
