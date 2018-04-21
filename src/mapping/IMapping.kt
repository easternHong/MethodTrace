package mapping

import utils.IConfig


interface IMapping {

    /**
     * 从mapping文件中解析出所有的类
     */
    fun collectMapping(config: IConfig): Map<String, Pair<String, String>>
}