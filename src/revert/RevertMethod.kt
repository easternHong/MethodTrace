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


class FieldDesc {
    var type = ""// field类型
    var name = "" //field名称
    var pName = "" //field混淆后名称
}

class MethodDesc {
    var name = ""// 方法名称
    var pName = ""// 混淆后名称
    //    var sList = ArrayList<String>() //signatures
    var rType = "" //返回类型
}

class RawClass {
    var name = ""   //类名字
    var pName = ""  //混淆后的名字
    var fList = ArrayList<FieldDesc>() //
    var mList = ArrayList<MethodDesc>()//方法列表
}