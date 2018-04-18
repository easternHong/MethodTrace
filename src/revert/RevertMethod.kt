package revert


class RevertMethod {

    var methodName = ""
    var signature = ArrayList<String>()
    var clazzName = ""
    override fun toString(): String {
        return "RevertMethod(methodName='$methodName', signature=$signature, clazzName='$clazzName')"
    }

}