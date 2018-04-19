package revert


class RevertMethod {
    /**
     * 插件包名
     */
    var pluginPkgName = ""
    var methodName = ""
    var signature = ArrayList<String>()
    var clazzName = ""
    override fun toString(): String {
        return "RevertMethod(methodName='$methodName', signature=$signature, clazzName='$clazzName')"
    }

}