package trace

import com.google.gson.Gson
import mapping.Mappings
import revert.RevertMethod
import revert.SignatureConverter
import utils.ConfigImpl
import utils.IConfig
import utils.TextUtils
import java.io.File
import javax.rmi.CORBA.ClassDesc

object Main {


    @JvmStatic
    fun main(args: Array<String>) {
        val time = System.currentTimeMillis()
        val mConfig = ConfigImpl()
        mConfig.setPackageFilter(object : IConfig.PackageFilter {
            override fun pluginAllow(pluginName: String): Boolean {
                return true
            }

            override fun pkgAllow(packageName: String): Boolean {
                if (packageName.contains("R$")) return false
                if (packageName.endsWith(".R")) return false
//                if (packageName.contains("sharesdk")) return false
//                if (packageName.contains("mob")) return false
//                if (packageName.contains("sina")) return false
//                if (packageName.contains("weibo")) return false
//                if (packageName.contains("proto")) return false
//                if (packageName.contains("jake")) return false
//                if (packageName.contains("life")) return false
//                if (packageName.contains("square")) return false
//                if (packageName.contains("webrtc")) return false
//                if (packageName.contains("android.support")) return false
//                if (packageName.contains("butter")) return false
//                if (packageName.contains("mediaframework")) return false
//                if (packageName.contains("videoplayer")) return false
//                if (packageName.contains("open")) return false
//                if (packageName.contains("udbauth")) return false
//                if (packageName.contains("medialib")) return false
//                if (packageName.contains("orangefilter")) return false
//                if (packageName.contains("hjbsdk")) return false
//                if (packageName.contains("lightstep")) return false
                return true
            }
        })
        val pkgMap = Mappings.mappings.collectMapping(mConfig)
        println("pkgList:${pkgMap.size}".plus(",cost:").plus((System.currentTimeMillis() - time)))
        val reader = DmTraceReader(Mappings.mappings.mConfig.traceFilePath, true)
        val revertMethodList = ArrayList<RevertMethod>()
        reader.setDumpFilter(object : DumpFilter {
            override fun allow(methodData: MethodData): Boolean {
                if (methodData.className.isEmpty()) return false
                if (methodData.className.contains("Xposed")) return false
                if (TextUtils.isEmpty(methodData.methodName)) return false
//
//                if (methodData.className.contains("android/os")) return false
//                if (methodData.className.contains("android/content")) return false
//                if (methodData.className.contains("java/lang")) return false
//                if (methodData.className.contains("java/util")) return false
//                if (methodData.className.contains("java/net")) return false
//                if (methodData.className.contains("reactivex")) return false
//                if (methodData.className.contains("ok")) return false
//                if (methodData.className.contains("crashreport")) return false
//                if (methodData.className.contains("android/net")) return false
//                if (methodData.className.contains("logger")) return false
//                if (methodData.className.contains("logger")) return false
//                if (methodData.className.contains("io")) return false
//                if (methodData.className.contains("Binder")) return false
//                if (methodData.className.contains("com/android/server")) return false
//                if (methodData.className.contains("libcore")) return false
//                if (methodData.className.contains("com/android/org")) return false
//                if (methodData.className.contains("meizu")) return false
//                if (methodData.className.contains("hiidostatis")) return false
//                if (methodData.className.contains("jakewharton")) return false
//                if (methodData.className.contains("javax")) return false
//                if (methodData.className.contains("database")) return false
//                if (methodData.className.contains("android/support")) return false
//                if (methodData.className.contains("sun")) return false
//                if (methodData.className.contains("security")) return false
//                if (methodData.className.contains("anet/")) return false
//                if (methodData.className.contains("dalvik/")) return false
//                if (methodData.className.contains("android/util/")) return false
//                if (methodData.className.contains("json/")) return false
//                if (methodData.className.contains("/text/")) return false
//                if (methodData.className.contains("android/app")) return false
//                if (methodData.className.contains("log")) return false
//                if (methodData.className.contains("Darts")) return false
//                if (methodData.className.contains("Executor")) return false

                formatMethod(methodData, revertMethodList)
                return true
            }
        })
        reader.setFormatListener(object : FormatListener {
            override fun finish() {
                for (method in revertMethodList) {
                    val classDesc = pkgMap[method.clazzName]
                    if (classDesc != null) {
                        method.rawClazzName = classDesc.name
                    }
                }
                println(Gson().toJson(revertMethodList))
                println("需要hook函数个数:".plus(revertMethodList.size))
            }
        })
        reader.generateTrees()
        println(reader.methods)
    }

    private fun formatMethod(methodData: MethodData, list: ArrayList<RevertMethod>) {
        val method = RevertMethod()
        method.clazzName = methodData.className.replace("/", ".")
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