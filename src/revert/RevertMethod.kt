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


class FieldDesc : Comparable<FieldDesc> {

    override fun compareTo(other: FieldDesc): Int {
        return name.compareTo(other.name)
    }

    var type = ""// field类型
    var name = "" //field名称
    var pName = "" //field混淆后名称

    override fun toString(): String {
        return "FieldDesc(type='$type', name='$name', pName='$pName')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FieldDesc

        if (type != other.type) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

}

class MethodDesc : Comparable<MethodDesc> {
    var name = ""// 方法名称
    var pName = ""// 混淆后名称
    //    var sList = ArrayList<String>() //signatures
    var rType = "" //返回类型

    override fun compareTo(other: MethodDesc): Int {
        return name.compareTo(other.name)
    }

    override fun toString(): String {
        return "MethodDesc(name='$name', pName='$pName', rType='$rType')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MethodDesc

        if (name != other.name) return false
        if (rType != other.rType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + rType.hashCode()
        return result
    }

}

class RawClass {
    @Transient
    var dartsInner = false
    var name = ""   //类名字
    var pName = ""  //混淆后的名字
    var fList = ArrayList<FieldDesc>() //
    var mList = ArrayList<MethodDesc>()//方法列表


    override fun toString(): String {
        return "RawClass(name='$name', pName='$pName', fList=$fList, mList=$mList)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawClass

        if (dartsInner != other.dartsInner) return false
        if (name != other.name) return false
        if (fList != other.fList) return false
        if (mList != other.mList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dartsInner.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + fList.hashCode()
        result = 31 * result + mList.hashCode()
        return result
    }

}