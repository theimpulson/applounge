package foundation.e.apps.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.CheckBoxPreference
import foundation.e.apps.R

class RadioButtonPreference : CheckBoxPreference {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        setView()
    }

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        setView()
    }

    private fun setView() {
        widgetLayoutResource = R.layout.preference_widget_radiobutton
    }

    override fun onClick() {
        if (this.isChecked)
            return

        super.onClick()
    }
}
