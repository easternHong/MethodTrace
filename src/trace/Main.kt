package trace

import com.google.gson.Gson
import mapping.Mappings
import revert.RevertMethod
import revert.SignatureConverter
import utils.ConfigFile
import utils.ConfigImpl
import utils.TextUtils
import java.io.FileReader
import java.util.*
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

        //1.oldVersionApk 信息收集
        val mappingsOld = Mappings()
        val mConfigOld = ConfigImpl()
        mConfigOld.aAptFile = mConfigFile.aapt
        mConfigOld.mApkDesc = mConfigFile.oldApk
        mappingsOld.mConfig = mConfigOld
        mappingsOld.collectVersions()

        //1.newVersionApk 信息收集
        val mConfigNew = ConfigImpl()
        val mappingsNew = Mappings()
        mConfigNew.aAptFile = mConfigFile.aapt
        mConfigNew.mApkDesc = mConfigFile.newApk
        mappingsNew.mConfig = mConfigNew
        mappingsNew.collectVersions()

        //diff BuildInPluginVersion
        //3.下载host和内置插件mapping文件前，删除相同版本的内置插件。
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

        //4.提取mapping文件集合
        mappingsOld.getMappingFileList()
        mappingsNew.getMappingFileList()
        val mOldClassDescMap = mappingsOld.collectMapping()
        val mNewClassDescMap = mappingsNew.collectMapping()
        println("pkgList:${mNewClassDescMap.size}".plus(",cost:").plus((System.currentTimeMillis() - time)))
        println("pkgList:${mOldClassDescMap.size}".plus(",cost:").plus((System.currentTimeMillis() - time)))
        println("开始比较增量")
        //5.删除两个版本没有改动的类信息(mapping文件描述相同)
        val keySet = TreeSet(mOldClassDescMap.keys)
        for (key in keySet) {
            val clazzOld = mOldClassDescMap[key]
            clazzOld?.fList?.sort()
            clazzOld?.mList?.sort()
            val clazzNew = mNewClassDescMap[key]
            clazzNew?.fList?.sort()
            clazzNew?.mList?.sort()
            var filterGod = (key.contains("\$EventBinder\$") || key.contains("DartsFactory")) && false
            if (clazzNew != null && clazzOld != null && clazzNew == clazzOld || filterGod) {
                (mOldClassDescMap as HashMap).remove(key)
                (mNewClassDescMap as HashMap).remove(key)
            } else {
                //删除剩余类集合中相同的field和methods
                for (i in clazzOld?.fList!!) {
                    clazzNew?.fList?.remove(i)
                }
                for (i in clazzOld.mList) {
                    clazzNew?.mList?.remove(i)
                }
                if (clazzNew?.fList != null && clazzNew.fList.isEmpty() && clazzNew.mList.isEmpty()) {
                    (mNewClassDescMap as HashMap).remove(key)
                }
            }
        }
        println("废弃&删除:${mOldClassDescMap.size}处,新增&更改:${mNewClassDescMap.size}处")

        //6.对于RxBus，同一个类，匿名内部类的名字可能每次都不一样。
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