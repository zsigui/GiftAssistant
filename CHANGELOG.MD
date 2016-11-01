# 版本变更日志:

## V1.5.1 -> V1.5.2

### 改动内容:

    [modify] 修改预览图片加载中的显示内容，改为圆形加载栏，并增加单击退出事件。


## V1.4.5 -> V1.5.0

### 改动内容:

    [modify] 礼包首页UI调整，增加限时免费模块，横向列表全部改为竖向列表

    [modify] 礼包列表项'价值'属性限时调整

    [modify] 游戏列表项中段UI字体调整

    [modify] 调整搜索结果页'求礼包'的位置

    [remove] 首页移除'限时免费'Tab，以及侧边栏

    [add] 推送添加游戏ID作为topic的方式

    [add] 首页增加'我的'Tab，内容为原来侧边栏的个人设置项

    [add] 礼包详情页增加跳转游戏礼包页入口

    [fix] 修复搜索结果出错时刷新出错的BUG

## V1.4.4 -> V1.4.5

### 改动内容:

    [modify] SDK调整唤醒广播逻辑

    [modify] 闪屏界面由Activity改为Fragment形式，优化启动速度

    [modify] 分享活动添加分享特定ID标志

## V1.4.3 -> V1.4.4

### 改动内容:

    [modify] 汇付宝SDK从1.0升级为3.0

    [modify] 添加新增的SDK微信支付完毕后关闭页面语句

## V1.4.2 -> V1.4.3

### 改动内容:

    [modify] 金币任务显示简化

    [modify] 首页缓存加载顺序调整

    [modify] 升级AndFix为0.5

    [modify] 调整SDK登录唤醒逻辑，减少加载白屏及切换失败情况

    [modify] 首充券抢号及使用说明UI调整

## V1.4.1 -> V1.4.2

### 改动内容:

    [fix] 修改手机登录统计内容

    [fix] 修改SDK版本号不对应BUG，解决偶玩后台统计出错问题

    [modify] 跳转设置账号密码页面判断

    [modify] 修复首充券活动详情页不显示横幅的BUG

    [fix] 首页部分页面加载逻辑调整，减少出现白屏情况

    [fix] 调整SDK唤醒判断，尽量减少切换调起失败的情况

## V1.4.0 -> V1.4.1

### 改动内容:

    [fix] 修复礼包详情页、游戏详情页，特定游戏下载状态关闭显示错误BUG

    [modify] 统一应用全局Activity界面跳转动画

## V1.3.8 -> V1.4.0

### 改动内容:

    [new] 活动首页改版，并增加‘开服开测表’界面

    [new] 添加闪屏广告跳转逻辑

    [new] 更新SDK为400，添加打通SDK账号的判断逻辑，主要包括‘切换账号’、‘其他应用已登录账号选择’

    [new] 增加活动礼包及相对应的消息提示和系统信息领取逻辑

    [modify] 统一各处礼包列表UI的判断逻辑，减少显示出错率

    [modify] 侧边栏、用户信息界面显示微调

    [modify] 修改我的首充券、我的礼包界面显示

    [modify] 增加控制下载开启关闭的属性

    [fix] 修复横竖屏切换应用被回收后的恢复失败BUG、上传图片超时BUG，及其他小错误 输入一个 tag 说明

### 说明:

    添加了新的功能界面，所以调整版本号为1.4.0。另外修改‘改动内容’的说明标签

## V1.3.7 -> V1.3.8

### 改动内容:

    1) 增加小米推送逻辑，并能够通过初始化接口进行相应推送SDK配置

    2) 修改了任务页面显示任务数目判断逻辑，涉及 ui/fragment/setting/TaskFragment.java

    3) 修复了充值偶玩豆没有刷新的BUG，修改了下　OuwanSDK　对于支付回调的判断，并添加了相应的支付回调接口

    4) 添加分享特定礼包的任务逻辑

    5) 添加了 URI 的 Schemas 过滤规则，以支持后续SDK调起礼包酷的跳转

    6) SocketIO修改传输协议transport的类型为只有"Polling"

    7) 活动页面添加分享功能，需要由前段进行传值调用，添加活动直接分享任务，url添加页面来源属性from

## V1.3.6 -> V1.3.7

### 说明:

    由于上个版本360以及乐视渠道出现检测安全权限问题（涉及静默下载和卸载的命令），鉴于这‘可能’存在问题，
    采取对方的建议重新上传更高版本的包进行解决。

### 改动内容:

    1) 解决礼包详情页http失效问题，修改PostDetailActivity.java类对于Uri的处理逻辑

## V1.3.5 -> V1.3.6

### 改动内容:

    1) 修复更新弹窗不显示BUG，涉及 manager/DialogManager.java

    2) 修复静默下载模块，增加判断网络状态，默认WIFI下才进行，涉及 download/silent 模块

    3) 修复游戏任务检测的闹钟失效BUG，修改设置闹钟的类型间隔为RTC，涉及 manager/AlarmClockManager.java

    4) 修复礼包两日后0点显示错误BUG，增加临界值判断，涉及 util/DateUtil.java

    5) 活动评论页面修改逻辑，右上角增加‘阅读原帖’按钮

    6) 欲增加 AndFix 热更新判断逻辑，涉及 manager/HotFixManager.java

    7) 调整 async/AsyncTask_InitApplication.java 的初始化逻辑，将非用户状态判断的网络请求初始化提取延后至
    ui/actvity/SplashActivity.java 里执行，已减少可能的推送唤醒时的同时大量网络请求情况

    8) 修改礼包页面项免费抢的显示内容

    9) 添加全局默认异常捕获器(Logic)

    10) 修改下载模块默认为False，不显示

## V1.3.4 -> V1.3.5

#### 目标:

    1) 统一 LOG 调用规范，添加文件配置 LOG 输出

    2) 统一 Toast 提示内容

    3) 统一 轮播图 / Web / 任务 / 推送 等多处的跳转页面和特殊代码块调用逻辑

    4) 尽量解决出现的BUG

#### 改动内容:

    1) 添加 util/log 模块，修改 config/AppDebugConfig.java ，涉及全局 KLog 与 Debug_Sdk 调用处；此外，
    在 asynctask/AsyncTask_InitApplication.java 中添加了文件配置 LOG 输出逻辑判断

    2) 提取所有的客户端出现的 Toast 提示内容到 config/ConstString.java 中，修改 ToastUtil.java 的 blur 方法
    逻辑，涉及全体 ToastUtil.blurXXX() 调用处

    3) config/util/BannerTypeUtil.java、listener/WebViewInterface.java、ui/fragment/TaskFragment.java、
    manager/PushMessageManager.java 各处添加调用统一结构逻辑方法

    4) SocketIOManager.java 逻辑重连判断缺失，进行了 log 输出调整和 mIsConnecting 判断

    5) 提取全局静态配置变量到 BuildConfig 中，采用 gradle.properties + app.gradle 协调模式配置，增加了
    dDebug (测试环境测试参数) 、rDebug (正式环境测试参数)、rRelease (正式环境正式参数，发布模式) 等三个
    执行配置模式，减少人为修改可能的出错率

#### 特别说明:

    由于出包时仓促未经测试的错误判断语句，导致了两个BUG： DialogManager#showUpdateDialog() 调起应用更新弹窗失败的BUG，和
    AlarmClockManager#startAlarm() 后台轮询失效而无法进行有效的游戏任务检测的BUG。关系1.3.4和1.3.5(未发包版本)

## V1.3.3 -> V1.3.4

#### 特别说明:

    无实际大调整，开启统计重新出包


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
