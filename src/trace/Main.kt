package trace

import com.google.gson.Gson
import mapping.Mappings
import revert.RevertMethod
import revert.SignatureConverter
import utils.ConfigFile
import utils.ConfigImpl
import utils.TextUtils
import java.io.FileReader

object Main {

    private fun getConfigJsonFile(): ConfigFile {
        val reader = FileReader("/home/g8489/Downloads/MethodTrace/config.json")
        return Gson().fromJson(reader, ConfigFile::class.java)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val time = System.currentTimeMillis()
        val mConfigFile = getConfigJsonFile()

        //first apk
        var mConfig = ConfigImpl()
        mConfig.aAptFile = mConfigFile.aapt
        mConfig.mApkDesc = mConfigFile.newApk
        val mOldClassDescMap = Mappings.mappings.collectMapping(mConfig)
        println("pkgList:${mOldClassDescMap.size}".plus(",cost:").plus((System.currentTimeMillis() - time)))
        //second apk
        mConfig = ConfigImpl()
        mConfig.aAptFile = mConfigFile.aapt
        mConfig.mApkDesc = mConfigFile.newApk
        val mNewClassDescMap = Mappings.mappings.collectMapping(mConfig)
        println("pkgList:${mNewClassDescMap.size}".plus(",cost:").plus((System.currentTimeMillis() - time)))


        val reader = DmTraceReader(Mappings.mappings.mConfig.traceFilePath, true)
        val revertMethodList = ArrayList<RevertMethod>()
        reader.setDumpFilter(object : DumpFilter {
            override fun allow(methodData: MethodData): Boolean {
                if (methodData.className.isEmpty()) return false
                if (methodData.className.contains("Xposed")) return false
                if (TextUtils.isEmpty(methodData.methodName)) return false
                formatMethod(methodData, revertMethodList)
                return true
            }
        })
        reader.setFormatListener(object : FormatListener {
            override fun finish() {
//                for (method in revertMethodList) {
//                    val classDesc = pkgMap[method.clazzName]
//                    if (classDesc != null) {
//                        method.rawClazzName = classDesc.name
//                    }
//                }
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