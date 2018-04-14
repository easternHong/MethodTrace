

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