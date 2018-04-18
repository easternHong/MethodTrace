import map.Mappings
import revert.RevertMethod
import revert.SignatureConverter

object Main {


    @JvmStatic
    fun main(args: Array<String>) {
        val filePath = "/home/g8489/xposed/main.trace"
        val mappingFile = "/home/g8489/yy/7.7.0_dior_feature/pluginmain-android_7.7.0_dior_feature/client/mapping.txt"
        val pkgMap = Mappings.mappings.parseMappings(mappingFile, Charsets.UTF_8.name())
        println("pkgList:${pkgMap.size}")
        val reader = DmTraceReader(filePath, true)
        val revertMethodList = ArrayList<RevertMethod>()
        reader.setDumpFilter(object : DumpFilter {
            override fun allow(methodData: MethodData): Boolean {
                if (methodData.name.contains("<init>")) return false
                if (methodData.name.contains("<clinit>")) return false
                val tmpClassName = methodData.className.replace("/", ".")
                if (pkgMap.containsKey(tmpClassName)) {
                    formatMethod(methodData, revertMethodList)
                    return true
                }
                return false
            }
        })
        reader.setFormatListener(object : FormatListener {
            override fun finish() {
                println(revertMethodList)
            }

        })
        reader.generateTrees()
    }

    private fun formatMethod(methodData: MethodData, list: ArrayList<RevertMethod>) {
        val method = RevertMethod()
        method.clazzName = methodData.className
        method.methodName = methodData.methodName
        val signatureList = SignatureConverter.convertMethodSignature("", methodData.signature)
                .replace("(", "").replace(")", "").replace(" ", "")
        if (signatureList.contains(",")) {
            method.signature = ArrayList(signatureList.split(",").toList())
        } else {
            val tmpList = ArrayList<String>(1)
            tmpList.add(signatureList)
            method.signature = ArrayList(tmpList)
        }
        list.add(method)
    }
}