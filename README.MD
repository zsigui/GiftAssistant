
# 版本变更日志:

## V1.3.2 -> V1.3.3

#### 目标:

    1) 程序BUG修复

    2) 局部UI交互调整

#### 改动内容:

    1) 修复‘猜你喜欢’模块小红点不显示问题，为int型时间戳逻辑溢出导致

    2) 增加页面详情、系统消息等多处的TextView可划取复制功能

#### 另外说明:

    经过近两个版本出包测试，有两点变动导致的变化需要注意：

    1) 友盟统计，应用退出不执行 System.exit(0) 操作，而依然执行 MoblickAgent.onKillProcess() 后，会出现除了启动数
    据，其他包含统计时间间隔等多处数据出现严重异常，而 TalkingData 无类似操作，暂未发现受影响。友盟统计是否如不执
    行 System.exit(0) 则不执行 MoblickAgent.onKillProcess() 还有待验证。

    2) WebView 在部分机型（比如红米Note，VIVO Xplay6）有时会出现 loadUrl 不执行的问题，即是页面完全卡住白屏。通过
    调整 XML 中 <ProgressBar /> 和 <WebView /> 两个组件的位置，暂时打包未再出现上述问题，是否由此而解决，有待后续
    验证。

## V1.3.1 -> V1.3.2

#### 目标:

    1) 程序BUG修复

#### 改动内容:

    1) 修复手机登录中，读取短信权限被禁止后出现的崩溃问题，涉及 ui/fragment/login/PhoneLoginFragment.java、
    ui/fragment/login/PhoneLoginNewFragment.java

    2) 修改 Web 页面的请求连接协议为 https ，以避免服务商劫持问题，涉及 ui/fragment/base/BaseFragment_WebView.java、
    config/WebViewUrl.java

    3) 修改手机登录的 Toast 显示内容


## V1.3.0 -> V1.3.1

#### 目标:

    1) 程序BUG修复

#### 改动内容:

    1) ‘我的礼包’页面提示错误修改，涉及 ui/fragment/setting/MyGiftListFragment.java

    2) 原手机登录模式也添加自动登录逻辑，涉及 ui/fragment/login/PhoneLoginFragment.java

## V1.2.6 -> V1.3.0

#### 目标:

    1) 改版礼包模块，新增限时免费抢以及可预约首充券功能

    2) 调整手机登录逻辑，以进行转化对比

    3) 修复旧版本出现的程序崩溃BUG

#### 改动内容:

    1) 礼包相关模块的UI改版，包含礼包首页、侧边栏、礼包搜索栏、礼包详情、限量礼包列表、我的礼包等页面的修改，以及
    首页‘限时免费’TAB、我的首充券等页面的增加

        + 按钮颜色式样整体修改，主要涉及 ui/widget/button/GiftButton.java 及各 xml 样式

        + 礼包列表相关适配器样式根据需求进行显示内容调整，主要涉及 adapter/FreeAdapter.java、adapter/GiftAdapter.java、
        adapter/IndexGiftLimitAdapter.java、adapter/LimitGiftListNewAdapter.java、adapter/NestedGiftListAdapter.java、
        adapter/MyCouponListAdapter.java等多个适配

        + 增加数个引导页弹窗，包括首充券引导页、游戏专区关注引导页

    2) 手机登录模式修改，增加新的可切换登录界面，增加读取短信自动登录逻辑，涉及 ui/fragment/login/*.java

    3) 新增首充券类型及可预约状态，修改礼包各个状态判断逻辑，涉及 config/util/GiftTypeUtil.java

    4) 轮播图新增和任务处理方式一致的处理判断逻辑，以及默认无法处理调起更新逻辑，涉及 config/util/BannerTypeUtil.java

    5) 修改部分提示Toast

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

     1) 修复主界面部分机型可能造成空指针问题, 涉及页面ui/activity/MainActivity.java , ui/fragment/gift/GiftFragment.java,
     调整 Fragment.setRetainInstance() 的调用位置, 对 GiftFragment.run.mHandler执行前进行判空创建处理. (测试通过)

     2) 修复搜索结果页面的在不开启下载功能且搜索结果只有游戏的情况下显示空白页面的问题, 涉及页面 SearchActivity.java /
     ResultFragment.java, 修改结果if的判断. (测试通过)

     3) 调整初始化逻辑, 将上一版本置于 SplashActivity.java 和 MainActivity.java 的初始化操作重新调整到 AssistantApp.java 中,
     去除多个初始化标识的控制操作(可能出现重复初始化问题, 但能保证一定有进行初始化), 涉及主要页面包括 AssistantApp.java ,
     asynctask/AsyncTask_InitApplication.java , manager/AlarmClockManager.java , manager/StatisticsManager.java ,
     ui/activity/MainActivity.java , ui/activity/SplashActivity.java . (测试进行中, 昨晚使用云测初测结果 : (1) 测试数量100,
     TalkingData和友盟都有, 分别记录启动数92和93, 使用时长间隔等, TalkingData为30左右,友盟0; (2) 测试数量50, 友盟, 记录启
     动数43, 使用时长等0;  猜测是因为云测执行时间过短导致数据还没上报就被卸载)

     4) 添加了自定义网络数据缓存, 保证再断网离线情况下也能够展示首页基本数据, 涉及页面 ui/fragment/gift/GiftFragment.java ,
     ui/fragment/game/GameSuperFragment.java, ui/fragment/game/GameTypeFragment.java, ui/fragment/game/GameNoticeFragment.java,
     ui/fragment/postbar/PostFragment.java, util/FileUtil.java, onLazyLoad的 retrofit 网络请求错误返回后执行 readCacheData()
     方法逻辑 (测试通过)
