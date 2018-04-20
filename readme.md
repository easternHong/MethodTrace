

#Android TraceView工具源码提取


本工程目的：分析函数执行时间分析脚本化。

[此工程基于源码-Android-7.0.0_r35](https://android.googlesource.com/platform/tools/swt/+/android-7.0.0_r35)

源码具有分析xxx.trace文件的功能。

监控App启动速度的一个较好的选择。

前提:
    a.手机:Root，安装Xposed，基于目标App的Xposed插件。
    b.关键模块的执行时间监控。



猜想：
    Debug.startMethodTracing("xxx"),跟踪了所有方法的执行时间，其中包括系统函数。
    能否修改xposed源码，过滤非必要的函数，保留关心的函数，尽可能降低TraceView文件记录过程对App的执行时间的破坏。
    
    
 0.Xposed对App启动速度影响程度(待验证)
        00.TraceView对函数执行时间的影响(待验证)
        01.
        02.查询方式
        2.插件启动速度


        脚本：1.Root手机，装有Xposed框架；2.使用内置的apk插件框架。3.装有YYAndroid客户端。
        注意事项：安装debug版本的YY(或者提供mappings文件)

        脚本流程：
            1.读入配置文件
                {
                    "hookedApk","xxx/path/xx.apk",
                    "yy_xposed_plugin","xxx/path/xx.apk",
                    "module_method_name",[[xxx],[xxx],[xxx]],
                    ...
                    "filter_log",[[com.yy.xxx],[com.android.yy.]]
                }
            2.安装插件，reboot device.
            3.run yy App .
            4.hook and create xx.trace file ,extracting them out to PC
            5.run `java -jar xxx_trace_tool.jar xxx.trace`
            6.
               