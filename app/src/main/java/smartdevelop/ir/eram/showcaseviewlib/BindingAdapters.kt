package smartdevelop.ir.eram.showcaseviewlib

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("bindVisibleOrGone")
fun setVisibility(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}
