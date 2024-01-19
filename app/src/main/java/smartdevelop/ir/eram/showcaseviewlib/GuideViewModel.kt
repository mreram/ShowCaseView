package smartdevelop.ir.eram.showcaseviewlib

import android.text.Spannable
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity

data class GuideViewModel(
    var title: Spannable,
    var content: Spannable,
    var leftButton: String? = null,
    var rightButton: String? = null,
    var isFinalScreen: Boolean = false,
    var isVideoScreen: Boolean = false,
    val gravity: Gravity? = Gravity.CENTER
) {
    fun title(): String = title.toString()
    fun content(): String = content.toString()
}
