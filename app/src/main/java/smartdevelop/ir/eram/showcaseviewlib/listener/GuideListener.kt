package smartdevelop.ir.eram.showcaseviewlib.listener

import android.view.View

/**
 * Created by Mohammad Reza Eram (https://github.com/mreram)
 * Edited by @author[northshore.org]
 */
interface GuideListener {
    fun onNext(view: View)
    fun onBack(view: View)
    fun onExternalLink()
    fun onExit(view: View)
    fun onTourFished()
}
