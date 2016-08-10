package com.xyd.aike.gank.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.PowerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * app相关辅助类
 */
public class AppUtil {
    private AppUtil() { 
        /* cannot be instantiated*/
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 获取应用程序名称
     *
     * @param context
     * @return
     */
    public static String getAppName(Context context) {

        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定包名的应用程序的icon
     *
     * @param context
     * @param packageName 包名
     * @return  应用程序的icon
     */
    public static Drawable getAppIcon(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Drawable appIcon = null;
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            appIcon = applicationInfo.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appIcon;
    }

    /**
     * 获取应用程序版本名称信息
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用程序的版本Code信息
     *
     * @param context
     * @return 版本code
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 安装指定路径下的Apk
     */
    public void installApk(Activity activity, String filePath) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
        activity.startActivityForResult(intent, 0);
    }

    /**
     * 卸载指定包名的App
     */
    public void uninstallApp(Activity activity, String packageName) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.DELETE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivityForResult(intent, 1);
    }

    /**
     * 打开指定包名的App
     */
    public void openOtherApp(Activity activity, String packageName) {
        PackageManager manager = activity.getPackageManager();
        Intent launchIntentForPackage = manager.getLaunchIntentForPackage(packageName);
        if (launchIntentForPackage != null) {
            activity.startActivity(launchIntentForPackage);
        }
    }

    /**
     * 打开指定包名的App应用信息界面
     */
    public void showAppInfo(Activity activity, String packageName) {
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivity(intent);
    }

    /**
     * 分享Apk信息
     */
    public void shareApkInfo(Activity activity, String info) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, info);
        activity.startActivity(intent);
    }

    /**
     * 判断当前App处于前台还是后台  --并且必须是系统应用该方法才有效
     * 需添加<uses-permission android:name="android.permission.GET_TASKS"/>
     */
    public static boolean isApplicationBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将当前应用运行到前台
     */
    public static void bring2Front(Context context, String packageName) {
        ActivityManager activtyManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activtyManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTaskInfos) {
            if (packageName.equals(runningTaskInfo.topActivity.getPackageName())) {
                activtyManager.moveTaskToFront(runningTaskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
                return;
            }
        }
    }

    /**
     * 回到系统桌面
     */
    private void back2Home(Activity activity) {
        Intent home = new Intent(Intent.ACTION_MAIN);

        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addCategory(Intent.CATEGORY_HOME);

        activity.startActivity(home);
    }

    /**
     * 系统是否在锁屏状态
     *
     * @return
     */
    public static boolean isScreenLocked(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    /**
     * 唤醒并解锁
     * 需添加权限<uses-permission android:name="android.permission.DISABLE_KEYGUARD"/>
     *
     * @param context
     */
    private void wakeAndUnlock(Context context) {
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        //获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是调试用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");

        //点亮屏幕
        wl.acquire(1000);

        //得到键盘锁管理器对象
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");

        //解锁
        kl.disableKeyguard();
    }

    /**
     * 获取App信息的一个封装类(包名、版本号、应用信息、图标、名称等)
     */
    public class AppEnging {
        public List<AppInfo> getAppInfos(Context context) {
            List<AppInfo> list = new ArrayList<AppInfo>();
            //获取应用程序信息
            //包的管理者
            PackageManager pm = context.getPackageManager();
            //获取系统中安装到所有软件信息
            List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
            for (PackageInfo packageInfo : installedPackages) {
                //获取包名
                String packageName = packageInfo.packageName;
                //获取版本号
                String versionName = packageInfo.versionName;
                //获取application
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                int uid = applicationInfo.uid;
                //获取应用程序的图标
                Drawable icon = applicationInfo.loadIcon(pm);
                //获取应用程序的名称
                String name = applicationInfo.loadLabel(pm).toString();
                //是否是用户程序
                //获取应用程序中相关信息,是否是系统程序和是否安装到SD卡
                boolean isUser;
                int flags = applicationInfo.flags;
                if ((applicationInfo.FLAG_SYSTEM & flags) == applicationInfo.FLAG_SYSTEM) {
                    //系统程序
                    isUser = false;
                } else {
                    //用户程序
                    isUser = true;
                }
                //是否安装到SD卡
                boolean isSD;
                if ((applicationInfo.FLAG_EXTERNAL_STORAGE & flags) == applicationInfo.FLAG_EXTERNAL_STORAGE) {
                    //安装到了SD卡
                    isSD = true;
                } else {
                    //安装到手机中
                    isSD = false;
                }
                //添加到bean中
                AppInfo appInfo = new AppInfo(name, icon, packageName, versionName, isSD, isUser);
                //将bean存放到list集合
                list.add(appInfo);
            }
            return list;
        }
    }

    // 封装软件信息的bean类
    static class AppInfo {
        //名称
        private String name;
        //图标
        private Drawable icon;
        //包名
        private String packagName;
        //版本号
        private String versionName;
        //是否安装到SD卡
        private boolean isSD;
        //是否是用户程序
        private boolean isUser;

        public AppInfo() {
            super();
        }

        public AppInfo(String name, Drawable icon, String packagName,
                       String versionName, boolean isSD, boolean isUser) {
            super();
            this.name = name;
            this.icon = icon;
            this.packagName = packagName;
            this.versionName = versionName;
            this.isSD = isSD;
            this.isUser = isUser;
        }
    }
}