package utils


abstract class IConfig {

    var aAptFile: String = ""
    protected var mappingsFilePaths = ArrayList<String>()
    /**
     * 读取mapping文件列表
     */
    abstract fun getMappings(): List<String>

    fun setMappings(mappingsFilePaths: ArrayList<String>) {
        this.mappingsFilePaths = mappingsFilePaths
    }

    var traceFilePath = ""
    var hostVersion: String = ""
    var buildBranch: String? = null
    var workingDir = ""

    private var packageFilter: PackageFilter? = null
    /**
     * 内置插件列表
     */
    private val mBuiltInPlugins = HashMap<String, String>()
    /**
     * 插件仓库路径
     */
    var mBuiltInPluginRepo = HashMap<String, String>()

    /**
     * 是否不需要下载mapping
     */
    var skipDownloadMapping = HashMap<String, Boolean>()

    var mBuiltInPluginVersionCode = HashMap<String, String>()

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

        mBuiltInPluginRepo["com.yy.mobile.share"] = "http://repo.yypm.com/dwbuild/mobile/android/pluginshare/pluginshare-%s/"
        mBuiltInPluginRepo["com.yy.mobile.plugin.main"] = "http://repo.yypm.com/dwbuild/mobile/android/pluginmain/pluginmain-%s/"
        mBuiltInPluginRepo["com.yy.mobile.qupaishenqu"] = "http://repo.yypm.com/dwbuild/mobile/android/pluginentshenqu/pluginentshenqu-%s/"
        mBuiltInPluginRepo["com.yy.mobile.plugin.ycloud"] = "http://repo.yypm.com/dwbuild/mobile/android/pluginycloud/pluginycloud-%s/"
        mBuiltInPluginRepo["com.yy.mobile.plugin.reactnative"] = "http://repo.yypm.com/dwbuild/mobile/android/rnplugin/rnplugin-%s/"
        mBuiltInPluginRepo["com.yy.mobile.plugin.playtogether"] = "http://repo.yypm.com/dwbuild/mobile/android/playtogether/playtogether-%s/"
        mBuiltInPluginRepo["com.yy.mobile.plugin.onepiece"] = "http://repo.yypm.com/dwbuild/mobile/android/pluginonepiece/pluginonepiece-%s/"
        mBuiltInPluginRepo["com.yy.mobile.plugin.moment"] = "http://repo.yypm.com/dwbuild/mobile/android/pluginmoment/pluginmoment-%s/"
        mBuiltInPluginRepo["com.yy.mobile.plugin.im"] = "http://repo.yypm.com/dwbuild/mobile/android/pluginim/pluginim-%s/"
        mBuiltInPluginRepo["com.yy.mobile.plugin.dolls"] = "http://repo.yypm.com/dwbuild/mobile/android/plugindolls/plugindolls-%s/"
        mBuiltInPluginRepo["com.duowan.mobile.entlive"] = "http://repo.yypm.com/dwbuild/mobile/android/pluginentlive/pluginentlive-%s/"
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