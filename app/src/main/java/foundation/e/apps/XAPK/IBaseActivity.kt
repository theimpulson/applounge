package foundation.e.apps.XAPK


abstract class IBaseActivity : BaseActivity(){

    override fun init() {
        super.init()
        setContentView(getLayout())
//        EventManager.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
//        EventManager.unregister(this)
    }


    protected abstract fun getLayout(): Int




}
