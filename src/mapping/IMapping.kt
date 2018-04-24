package mapping

import revert.RawClass


interface IMapping {

    /**
     * 从mapping文件中解析出所有的类
     */
    fun collectMapping(): Map<String, RawClass>

    fun getMappingFileList(): ArrayList<String>

    fun collectVersions()

    fun getNewClass(): Map<String, RawClass>
}