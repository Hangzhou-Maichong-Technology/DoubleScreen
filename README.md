# DoubleScreen
杭州迈冲科技双屏异显/同显实例

## 一、使用说明
双屏异显/同显是迈冲 Android 智能设备连接 HDMI 显示屏时的增强功能。

### 获取屏幕最顶部显示权限
前往设置页面配置权限
```java
if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:" + getPackageName()));
    startActivityForResult(intent, REQUEST_CODE_OVERLAY);
} else {
    init();
}
```

### 使用 DisplayManager 和 Presentation 显示
```java
public void updateContents(int state, String path) {
    mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
    Display[] displays = mDisplayManager.getDisplays();
    Log.i(TAG, "hdmi path ==>" + displays.length);
    showPresentation(displays[state], path);
}
```

## 二、下载体验
[双屏异显/同显 apk 下载](https://github.com/Hangzhou-Maichong-Technology/DoubleScreen/raw/master/apk/DoubleScreen.apk)