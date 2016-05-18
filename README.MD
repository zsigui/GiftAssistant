
#版本变更日志:

## V1.2.5 -> V1.2.6

#### 目标:

    1) 修复未捕捉的导致程序崩溃BUG

    2) 更新友盟统计SDK版本

#### 改动内容:

    1) 修复 lint 检查后发现 third/youmilib 文件存在格式错误问题(换行符号不合适'\r\n'), 涉及改动整个第三方库
    全部格式化页面

    2) 修复 lint 检查后发现 third/youmilib 和 third/ouwansdk 存在 getSystemService 取值不符合规范问题, 以及未在
    调用相关 location 定位服务前调用 checkPermission 方法进行权限验证的问题.

    3) 修复数个空指针错误, 涉及 AssistantApp.java 的 getRetrofit(),
    cn.finalteam.galleryfinal.adapter.ViewHolderRecyclingPagerAdapter 的 getCount()

    4) 调整友盟统计升级为V6.0.0后的初始化设置逻辑变更, 涉及 manage/StatiscsManager.java


## V1.2.4 -> V1.2.5

#### 目标:

    1) 数据统计丢失解决后正式版出包

    2) 修复发现的交互BUG

#### 改动内容:

    1) 修改上一版本更改初始化逻辑而未调整测试模式下的初始化的遗漏问题,涉及SplashActivity.java

    2) 修复切换到其他程序回来出现主界面页面重叠(可体现为下端tab栏选项与显示内容不符合)的问题,
    涉及 MainActivity.java, 通过在 hideAllFragment() 中再搜索执行一次 Fragment.findByTag()实现

    3) 修复不允许下载的时候, 抢礼包后出现的'打开游戏'按钮无响应问题,涉及GetCodeDialog.java, 点击事件进行判断处理

    4) 修复游戏榜单列表部分图标长时间不加载问题, 取消原来滑动禁止加载的限制(旧问题,
    删除ScrollListener的UIL resume()和pause()判断即可), 涉及GameNoticeFragment.java, 非完善处理


## V1.2.3 -> V1.2.4

#### 目标: 
    修复数据统计丢失问题 , 修复崩溃异常BUG ,  修复页面显示问题 , 实现断网加载缓存

#### 改动内容: (测试机型 : 1加 A0001, CM系统, Android SDK 6.0.1) 

     1) 修复主界面部分机型可能造成空指针问题, 涉及页面ui/activity/MainActivity.java , ui/fragment/gift/GiftFragment.java, 调整 Fragment.setRetainInstance() 的调用位置, 对 GiftFragment.run.mHandler执行前进行判空创建处理. (测试通过)

     2) 修复搜索结果页面的在不开启下载功能且搜索结果只有游戏的情况下显示空白页面的问题, 涉及页面 SearchActivity.java / ResultFragment.java, 修改结果if的判断. (测试通过)

     3) 调整初始化逻辑, 将上一版本置于 SplashActivity.java 和 MainActivity.java 的初始化操作重新调整到 AssistantApp.java 中, 去除多个初始化标识的控制操作(可能出现重复初始化问题, 但能保证一定有进行初始化), 涉及主要页面包括 AssistantApp.java , asynctask/AsyncTask_InitApplication.java , manager/AlarmClockManager.java , manager/StatisticsManager.java , ui/activity/MainActivity.java , ui/activity/SplashActivity.java . 
        (测试进行中, 昨晚使用云测初测结果 : (1) 测试数量100, TalkingData和友盟都有, 分别记录启动数92和93, 使用时长间隔等, TalkingData为30左右,友盟0; (2) 测试数量50, 友盟,    记录启动数43, 使用时长等0;  猜测是因为云测执行时间过短导致数据还没上报就被卸载)

     4) 添加了自定义网络数据缓存, 保证再断网离线情况下也能够展示首页基本数据, 涉及页面 ui/fragment/gift/GiftFragment.java , ui/fragment/game/GameSuperFragment.java, ui/fragment/game/GameTypeFragment.java, ui/fragment/game/GameNoticeFragment.java, ui/fragment/postbar/PostFragment.java, util/FileUtil.java, onLazyLoad的 retrofit 网络请求错误返回后执行 readCacheData() 方法逻辑 (测试通过)
