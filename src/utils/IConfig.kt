package utils


abstract class IConfig {

    protected var mappingsFilePaths = ArrayList<String>()
    /**
     * 读取mapping文件列表
     */
    abstract fun getMappings(): List<String>

    fun setMappings(mappingsFilePaths: ArrayList<String>) {
        this.mappingsFilePaths = mappingsFilePaths
    }

    private var packageFilter: PackageFilter? = null
    /**
     * 内置插件列表
     */
    protected val mBuiltInPlugins = HashMap<String, String>()
    /**
     * 插件仓库路径
     */
    protected val mBuiltInPluginRepo = HashMap<String, String>()

    protected val mBuiltInPluginVersionCode = HashMap<String, String>()

    init {
        mBuiltInPlugins["com.yy.mobile.share"] = "com.yy.mobile.share"
        mBuiltInPlugins["com.yy.mobile.qupaishenqu"] = "com.yy.mobile.qupaishenqu"
        mBuiltInPlugins["com.yy.mobile.plugin.ycloud"] = "com.yy.mobile.plugin.ycloud"
        mBuiltInPlugins["com.yy.mobile.plugin.reactnative"] = "com.yy.mobile.plugin.reactnative"
        mBuiltInPlugins["com.yy.mobile.plugin.playtogether"] = "com.yy.mobile.plugin.playtogether"
        mBuiltInPlugins["com.yy.mobile.plugin.onepiece"] = "com.yy.mobile.plugin.onepiece"
        mBuiltInPlugins["com.yy.mobile.plugin.moment"] = "com.yy.mobile.plugin.moment"
        mBuiltInPlugins["com.yy.mobile.plugin.main"] = "com.yy.mobile.plugin.main"
        mBuiltInPlugins["com.yy.mobile.plugin.im"] = "com.yy.mobile.plugin.im"
        mBuiltInPlugins["com.yy.mobile.plugin.dolls"] = "com.yy.mobile.plugin.dolls"
        mBuiltInPlugins["com.duowan.mobile.entlive"] = "com.duowan.mobile.entlive"

        mBuiltInPlugins["com.yy.mobile.plugin.main"] = "http://repo.yypm.com/dwbuild/mobile/android/pluginmain/pluginmain-%s/"
    }

    fun getBuiltInPlugins(): Map<String, String> {
        return mBuiltInPlugins
    }

    fun getBuiltInPluginVersionCode(): Map<String, String> {
        return mBuiltInPluginVersionCode
    }

    fun setPackageFilter(packageFilter: PackageFilter) {
        this.packageFilter = packageFilter
    }

    fun getPackageFilter(): PackageFilter? {
        return packageFilter
    }

    interface PackageFilter {
        fun pkgAllow(packageName: String): Boolean
        fun pluginAllow(pluginName: String): Boolean
    }
}