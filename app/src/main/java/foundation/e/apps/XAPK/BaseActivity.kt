package foundation.e.apps.XAPK

import android.os.Bundle
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity

abstract class BaseActivity : RxAppCompatActivity() {
    protected val logTag: String by lazy { javaClass.simpleName }
    protected val mContext by lazy { this }
    protected val mActivity: BaseActivity by lazy { this }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nextStep()
        finish()
    }

    protected open fun nextStep() {}
}
