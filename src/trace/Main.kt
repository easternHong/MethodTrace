package trace

import com.google.gson.Gson
import mapping.Mappings
import revert.RevertMethod
import revert.SignatureConverter
import utils.ConfigFile
import utils.ConfigImpl
import utils.TextUtils
import java.io.FileReader
import java.util.TreeSet
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
        val mappingsOld = Mappings()
        val mConfigOld = ConfigImpl()
        mConfigOld.aAptFile = mConfigFile.aapt
        mConfigOld.mApkDesc = mConfigFile.oldApk
        mappingsOld.mConfig = mConfigOld
        mappingsOld.collectVersions()

        //second apk
        val mConfigNew = ConfigImpl()
        val mappingsNew = Mappings()
        mConfigNew.aAptFile = mConfigFile.aapt
        mConfigNew.mApkDesc = mConfigFile.newApk
        mappingsNew.mConfig = mConfigNew
        mappingsNew.collectVersions()

        //diff BuildInPluginVersion
        val mOldVersionMap = HashMap(mConfigOld.mBuiltInPluginVersionCode)

        val keys = mOldVersionMap.keys
        for (v in keys) {
            val mOld = mConfigOld.mBuiltInPluginVersionCode[v]
            val mNew = mConfigNew.mBuiltInPluginVersionCode[v]
            if (mOld == mNew) {
                mConfigOld.mBuiltInPluginVersionCode.remove(v)
                mConfigNew.mBuiltInPluginVersionCode.remove(v)
            }
        }

        //
        mappingsOld.getMappingFileList()
        mappingsNew.getMappingFileList()
        val mOldClassDescMap = mappingsOld.collectMapping()
        val mNewClassDescMap = mappingsNew.collectMapping()
        println("pkgList:${mNewClassDescMap.size}".plus(",cost:").plus((System.currentTimeMillis() - time)))
        println("pkgList:${mOldClassDescMap.size}".plus(",cost:").plus((System.currentTimeMillis() - time)))
        println("开始比较增量")
        val keySet = TreeSet(mOldClassDescMap.keys)
        for (key in keySet) {
            val clazzOld = mOldClassDescMap[key]
            val clazzNew = mNewClassDescMap[key]
            if (clazzNew != null && clazzOld != null && clazzNew == clazzOld) {
                (mOldClassDescMap as HashMap).remove(key)
                (mNewClassDescMap as HashMap).remove(key)
            }
        }
        println("比较结束:${mOldClassDescMap.size},${mNewClassDescMap.size}")

        println(mOldClassDescMap["com.yy.mobile.ui.startask.WeekTaskAccess\$\$EventBinder\$18"])
        println(mNewClassDescMap["com.yy.mobile.ui.startask.WeekTaskAccess\$\$EventBinder\$18"])
        val reader = DmTraceReader("xxxxx", true)
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
                println(Gson().toJson(revertMethodList))
                println("需要hook函数个数:".plus(revertMethodList.size))
            }
        })
        try {
            reader.generateTrees()
            println(reader.methods)
        } catch (e: Exception) {
            println(e)
        }
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