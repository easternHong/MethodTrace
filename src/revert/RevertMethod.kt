package revert


class RevertMethod {
    /**
     * 插件包名
     */
    var pluginPkgName = ""
    var methodName = ""
    var signature = ArrayList<String>()
    var clazzName = ""
    var rawClazzName = ""
    override fun toString(): String {
        return "RevertMethod(pluginPkgName='$pluginPkgName', methodName='$methodName', signature=$signature, clazzName='$clazzName', rawClazzName='$rawClazzName')"
    }

}