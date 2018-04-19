package trace

import mapping.Mappings
import revert.RevertMethod
import revert.SignatureConverter
import utils.ConfigImpl
import utils.IConfig
import java.io.File

object Main {


    @JvmStatic
    fun main(args: Array<String>) {
        val filePath = "/home/g8489/xposed/main.trace"
        var time = System.currentTimeMillis()
        val mConfig = ConfigImpl()
        val mapList = ArrayList<String>()
        val rootFile = File("/home/g8489/mappings")
                .list { p0, p1 ->
                    mapList.add(p0.absolutePath.plus(File.separator).plus(p1))
                    true
                }
        mConfig.setPackageFilter(object : IConfig.PackageFilter {
            override fun pluginAllow(pluginName: String): Boolean {
//                if (pluginName.contains("ycloud")) return false
                return true
            }

            override fun pkgAllow(packageName: String): Boolean {
                if (packageName.contains("R$")) return false
                if (packageName.endsWith(".R")) return false
                if (packageName.contains("sharesdk")) return false
                if (packageName.contains("mob")) return false
                if (packageName.contains("sina")) return false
                if (packageName.contains("weibo")) return false
                if (packageName.contains("proto")) return false
                if (packageName.contains("jake")) return false
                if (packageName.contains("life")) return false
                if (packageName.contains("square")) return false
                if (packageName.contains("webrtc")) return false
                if (packageName.contains("android.support")) return false
                if (packageName.contains("butter")) return false
                if (packageName.contains("mediaframework")) return false
                if (packageName.contains("videoplayer")) return false
                if (packageName.contains("open")) return false
                if (packageName.contains("udbauth")) return false
                if (packageName.contains("medialib")) return false
                if (packageName.contains("orangefilter")) return false
                if (packageName.contains("hjbsdk")) return false
                if (packageName.contains("lightstep")) return false
                println(packageName)
                return true
            }
        })
        mConfig.setMappings(mapList)
        val pkgMap = Mappings.mappings.collectMapping(mConfig)
        println("pkgList:${pkgMap.size}".plus(",cost:").plus((System.currentTimeMillis() - time)))
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
                println("需要hook函数个数:".plus(revertMethodList.size))
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