
#版本变更日志:

## V1.2.3 -> V1.2.4

#### 目标: 
    修复数据统计丢失问题 , 修复崩溃异常BUG ,  修复页面显示问题 , 实现断网加载缓存

#### 改动内容: (测试机型 : 1加 A0001, CM系统, Android SDK 6.0.1) 

     1) 修复主界面部分机型可能造成空指针问题, 涉及页面ui/activity/MainActivity.java , ui/fragment/gift/GiftFragment.java, 调整 Fragment.setRetainInstance() 的调用位置, 对 GiftFragment.run.mHandler执行前进行判空创建处理. (测试通过)

     2) 修复搜索结果页面的在不开启下载功能且搜索结果只有游戏的情况下显示空白页面的问题, 涉及页面 SearchActivity.java / ResultFragment.java, 修改结果if的判断. (测试通过)

     3) 调整初始化逻辑, 将上一版本置于 SplashActivity.java 和 MainActivity.java 的初始化操作重新调整到 AssistantApp.java 中, 去除多个初始化标识的控制操作(可能出现重复初始化问题, 但能保证一定有进行初始化), 涉及主要页面包括 AssistantApp.java , asynctask/AsyncTask_InitApplication.java , manager/AlarmClockManager.java , manager/StatisticsManager.java , ui/activity/MainActivity.java , ui/activity/SplashActivity.java . 
        (测试进行中, 昨晚使用云测初测结果 : (1) 测试数量100, TalkingData和友盟都有, 分别记录启动数92和93, 使用时长间隔等, TalkingData为30左右,友盟0; (2) 测试数量50, 友盟,    记录启动数43, 使用时长等0;  猜测是因为云测执行时间过短导致数据还没上报就被卸载)

     4) 添加了自定义网络数据缓存, 保证再断网离线情况下也能够展示首页基本数据, 涉及页面 ui/fragment/gift/GiftFragment.java , ui/fragment/game/GameSuperFragment.java, ui/fragment/game/GameTypeFragment.java, ui/fragment/game/GameNoticeFragment.java, ui/fragment/postbar/PostFragment.java, util/FileUtil.java, onLazyLoad的 retrofit 网络请求错误返回后执行 readCacheData() 方法逻辑 (测试通过)