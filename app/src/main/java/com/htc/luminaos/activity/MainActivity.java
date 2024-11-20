package com.htc.luminaos.activity;

import static com.htc.luminaos.utils.BlurImageView.MAX_BITMAP_SIZE;
import static com.htc.luminaos.utils.BlurImageView.narrowBitmap;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.htc.luminaos.MyApplication;
import com.htc.luminaos.adapter.ShortcutsAdapterMuQi;
import com.htc.luminaos.databinding.ActivityMainMuqiBinding;
import com.htc.luminaos.receiver.AppCallBack;
import com.htc.luminaos.receiver.AppReceiver;
import com.htc.luminaos.receiver.BatteryReceiver;
import com.htc.luminaos.receiver.UsbDeviceCallBack;
import com.htc.luminaos.utils.BatteryCallBack;
import com.htc.luminaos.utils.BlurImageView;
import com.htc.luminaos.utils.CircularQueue;
import com.htc.luminaos.utils.FileUtils;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.htc.luminaos.R;
import com.google.gson.Gson;
import com.htc.luminaos.adapter.ShortcutsAdapter;
import com.htc.luminaos.adapter.ShortcutsAdapterCustom;
import com.htc.luminaos.databinding.ActivityMainBinding;
import com.htc.luminaos.databinding.ActivityMainCustomBinding;
import com.htc.luminaos.entry.AppInfoBean;
import com.htc.luminaos.entry.AppSimpleBean;
import com.htc.luminaos.entry.AppsData;
import com.htc.luminaos.entry.ChannelData;
import com.htc.luminaos.entry.ShortInfoBean;
import com.htc.luminaos.manager.RequestManager;
import com.htc.luminaos.receiver.BluetoothCallBcak;
import com.htc.luminaos.receiver.BluetoothReceiver;
import com.htc.luminaos.receiver.MyTimeCallBack;
import com.htc.luminaos.receiver.MyTimeReceiver;
import com.htc.luminaos.receiver.MyWifiCallBack;
import com.htc.luminaos.receiver.MyWifiReceiver;
import com.htc.luminaos.receiver.NetWorkCallBack;
import com.htc.luminaos.receiver.NetworkReceiver;
import com.htc.luminaos.receiver.UsbDeviceReceiver;
import com.htc.luminaos.utils.AppUtils;
import com.htc.luminaos.utils.BluetoothUtils;
import com.htc.luminaos.utils.Constants;
import com.htc.luminaos.utils.Contants;
import com.htc.luminaos.utils.DBUtils;
import com.htc.luminaos.utils.LanguageUtil;
import com.htc.luminaos.utils.LogUtils;
import com.htc.luminaos.utils.NetWorkUtils;
import com.htc.luminaos.utils.ShareUtil;
import com.htc.luminaos.utils.SystemPropertiesUtil;
import com.htc.luminaos.utils.TimeUtils;
import com.htc.luminaos.utils.ToastUtil;
import com.htc.luminaos.utils.Uri;
import com.htc.luminaos.utils.Utils;
import com.htc.luminaos.utils.VerifyUtil;
import com.htc.luminaos.widget.ManualQrDialog;
import com.htc.luminaos.widget.SpacesItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends BaseMainActivity implements BluetoothCallBcak, MyWifiCallBack, MyTimeCallBack, NetWorkCallBack, UsbDeviceCallBack, AppCallBack, BatteryCallBack {

    private ActivityMainBinding mainBinding;

    public ActivityMainMuqiBinding customBinding;
    private ArrayList<ShortInfoBean> short_list = new ArrayList<>();

    boolean get_default_url = false;
    private boolean isFrist = true;
    private ChannelData channelData;
    private List<AppsData> appsDataList;
    /**
     * receiver
     */

    private NetworkReceiver networkReceiver = null;
    // 时间
    private IntentFilter timeFilter = new IntentFilter();
    private MyTimeReceiver timeReceiver = null;
    // wifi
    private IntentFilter wifiFilter = new IntentFilter();
    private MyWifiReceiver wifiReceiver = null;
    // 蓝牙
    private IntentFilter blueFilter = new IntentFilter();
    //usbDevice
    private IntentFilter usbDeviceFilter = new IntentFilter();
    private BluetoothReceiver blueReceiver = null;
    //Usb 设备
    private UsbDeviceReceiver usbDeviceReceiver = null;

    //电池
    private BatteryReceiver batteryReceiver = null;

    private static String TAG = "MainActivity";

    private String appName = "";

    private boolean requestFlag = false;

    private final int DATA_ERROR = 102;
    private final int DATA_FINISH = 103;

    private Hashtable<String, String> hashtable = new Hashtable<>();

    private AppReceiver appReceiver = null;
    private WifiManager wifiManager = null;

    private StorageManager storageManager = null;

    ExecutorService threadExecutor = Executors.newFixedThreadPool(5);

    private List<StorageVolume> localDevicesList;

    private AudioManager audioManager = null;

    private List<AppInfoBean> appInfoBeans = null;

    private CircularQueue circularQueue = null;

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
//                case 202:
//                    ShortcutsAdapter shortcutsAdapter = new ShortcutsAdapter(MainActivity.this, short_list);
//                    shortcutsAdapter.setItemCallBack(itemCallBack);
//                    mainBinding.shortcutsRv.setAdapter(shortcutsAdapter);
//                    break;
                case 204:
                    ShortcutsAdapterCustom shortcutsAdapterCustom = new ShortcutsAdapterCustom(MainActivity.this, short_list);
                    shortcutsAdapterCustom.setItemCallBack(itemCallBackCustom);
                    customBinding.shortcutsRv.setAdapter(shortcutsAdapterCustom);
                    break;
                case 205:
                    ShortcutsAdapterMuQi shortcutsAdapterMuQi = new ShortcutsAdapterMuQi(MainActivity.this, short_list);
                    shortcutsAdapterMuQi.setItemCallBack(itemCallBackMuQi);
                    customBinding.shortcutsRv.setAdapter(shortcutsAdapterMuQi);
                    break;
                case DATA_ERROR:
                    requestFlag = false;
                    ToastUtil.showShortToast(MainActivity.this, getString(R.string.data_err));
                    break;
                case DATA_FINISH:
                    requestFlag = false;
                    if (channelData != null && channelData.getData().size() > 0) {
                        startAppFormChannel();
                    }
                    break;
            }

            return false;
        }
    });

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        //定制逻辑 xuhao add 20240717
//        try {
//            customBinding = ActivityMainCustomBinding.inflate(LayoutInflater.from(this));
//            setContentView(customBinding.getRoot());
//            initViewCustom();
//            initDataCustom();
//            initReceiver();
//            wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);
//            storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
//            localDevicesList = new ArrayList<StorageVolume>();
//            devicesPathAdd();
////            countUsbDevices(getApplicationContext());
//            Log.d(TAG, " onCreate快捷图标 short_list " + short_list.size());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //定制逻辑 xuhao add 20240717
        try {
            customBinding = ActivityMainMuqiBinding.inflate(LayoutInflater.from(this));
            setContentView(customBinding.getRoot());
            initViewCustom();
            initDataCustom();
            initReceiver();
            wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);
            storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            localDevicesList = new ArrayList<StorageVolume>();
            devicesPathAdd();
//            countUsbDevices(getApplicationContext());
            Log.d(TAG, " onCreate快捷图标 short_list " + short_list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateTime();
            updateBle();
            if ((boolean) ShareUtil.get(this, Contants.MODIFY, false)) {
                short_list = loadHomeAppData();
//            handler.sendEmptyMessage(202);
                handler.sendEmptyMessage(204);
                ShareUtil.put(this, Contants.MODIFY, false);
            }
            Log.d(TAG, " onResume快捷图标 short_list " + short_list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private void initViewCustom() {
    //总共12个
    //1 我的应用 新的UI放到recyclerView里面去了
    //2 应用商店
//        customBinding.rlGoogle.setOnClickListener(this);
//        customBinding.rlGoogle.setOnHoverListener(this);
//        customBinding.rlGoogle.setOnFocusChangeListener(this);
//        //3 设置
//        customBinding.rlSettings.setOnClickListener(this);
//        customBinding.rlSettings.setOnHoverListener(this);
//        customBinding.rlSettings.setOnFocusChangeListener(this);
//        //4 文件管理
//        customBinding.rlUsb.setOnClickListener(this);
//        customBinding.rlUsb.setOnHoverListener(this);
//        customBinding.rlUsb.setOnFocusChangeListener(this);
//        //5 HDMI 1
//        customBinding.rlHdmi1.setOnClickListener(this);
//        customBinding.rlHdmi1.setOnHoverListener(this);
//        customBinding.rlHdmi1.setOnFocusChangeListener(this);
//        //6 rl_av
//        //7 rl_hdmi2
//        //8 rl_vga
//        //9 rl_manual 说明书，新的UI没这个功能
//        //10 wifi
//        customBinding.rlWifi.setOnClickListener(this);
//        customBinding.rlWifi.setOnHoverListener(this);
//        customBinding.rlWifi.setOnFocusChangeListener(this);
//        //11 蓝牙
//        customBinding.rlBluetooth.setOnClickListener(this);
//        customBinding.rlBluetooth.setOnHoverListener(this);
//        customBinding.rlBluetooth.setOnFocusChangeListener(this);
//        //12 切换背景
//        customBinding.rlWallpapers.setOnClickListener(this);
//        customBinding.rlWallpapers.setOnHoverListener(this);
//        customBinding.rlWallpapers.setOnFocusChangeListener(this);
//        //13 Eshare
//        customBinding.homeEshare.setOnClickListener(this);
//        customBinding.homeEshare.setOnHoverListener(this);
//        customBinding.homeEshare.setOnFocusChangeListener(this);
//        //14 Netflix
//        customBinding.homeNetflix.setOnClickListener(this);
//        customBinding.homeNetflix.setOnHoverListener(this);
//        customBinding.homeNetflix.setOnFocusChangeListener(this);
//        //15 Youtube
//        customBinding.homeYoutube.setOnClickListener(this);
//        customBinding.homeYoutube.setOnHoverListener(this);
//        customBinding.homeYoutube.setOnFocusChangeListener(this);
//        //16 迪士尼
//        customBinding.homeDisney.setOnClickListener(this);
//        customBinding.homeDisney.setOnHoverListener(this);
//        customBinding.homeDisney.setOnFocusChangeListener(this);
    //17 首页Usb插入、拔出图标
//        customBinding.usbConnect
//        //18 电池状态
//        initBattery();
//        //19 U盘插入
//        customBinding.rlUsbConnect.setOnClickListener(this);
//        customBinding.rlUsbConnect.setOnHoverListener(this);
//        customBinding.rlUsbConnect.setOnFocusChangeListener(this);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this) {
//            @Override
//            public boolean canScrollHorizontally() {
//                // 禁用水平滚动
//                return false;
//            }
//        };
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
////        customBinding.shortcutsRv.addItemDecoration(new SpacesItemDecoration(0,
////                (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.03), 0, 0));
//        //定义Item之间的间距
//        customBinding.shortcutsRv.addItemDecoration(new SpacesItemDecoration(0,
//                (int) getResources().getDimension(R.dimen.x_43), 0, 0));
//        customBinding.shortcutsRv.setLayoutManager(layoutManager);
//    }

    private void initViewCustom() {
        //U盘
        customBinding.rlUsbConnect.setOnClickListener(this);
        customBinding.rlUsbConnect.setOnHoverListener(this);
        customBinding.rlUsbConnect.setOnFocusChangeListener(this);
        //信源
        customBinding.rlSignalSource.setOnClickListener(this);
        customBinding.rlSignalSource.setOnHoverListener(this);
        customBinding.rlSignalSource.setOnFocusChangeListener(this);
        //背景切换
        customBinding.rlWallpapers.setOnClickListener(this);
        customBinding.rlWallpapers.setOnHoverListener(this);
        customBinding.rlWallpapers.setOnFocusChangeListener(this);
        //蓝牙
        customBinding.rlMuqiBt.setOnClickListener(this);
        customBinding.rlMuqiBt.setOnHoverListener(this);
        customBinding.rlMuqiBt.setOnFocusChangeListener(this);
        //文件管理器
        customBinding.rlMuqiUsb.setOnClickListener(this);
        customBinding.rlMuqiUsb.setOnHoverListener(this);
        customBinding.rlMuqiUsb.setOnFocusChangeListener(this);
        //设置
        customBinding.rlMuqiSettings.setOnClickListener(this);
        customBinding.rlMuqiSettings.setOnHoverListener(this);
        customBinding.rlMuqiSettings.setOnFocusChangeListener(this);
        //wifi
        customBinding.rlMuqiWifi.setOnClickListener(this);
        customBinding.rlMuqiWifi.setOnHoverListener(this);
        customBinding.rlMuqiWifi.setOnFocusChangeListener(this);
        //icon1
        customBinding.rlMuqiIcon1.setOnClickListener(this);
        //icon2
        customBinding.rlMuqiIcon2.setOnClickListener(this);
        //icon3
        customBinding.rlMuqiIcon3.setOnClickListener(this);
        //icon4
        customBinding.rlMuqiIcon4.setOnClickListener(this);
        customBinding.rlMuqiIcon4.setOnHoverListener(this);
        customBinding.rlMuqiIcon4.setOnFocusChangeListener(this);
        customBinding.rlMuqiIcon4.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // 只在按下时处理
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            if (circularQueue != null) {
                                circularQueue.moveLeft();
                                update7Icon(circularQueue.front, circularQueue.rear);
                            }
                            if (getButtonSound()) {
                                audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
                            }
//                            ToastUtil.showShortToast(getApplicationContext(),"向左移动");
                            return true;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            if (circularQueue != null) {
                                circularQueue.moveRight();
                                update7Icon(circularQueue.front, circularQueue.rear);
                            }
                            if (getButtonSound()) {
                                audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
                            }
//                            ToastUtil.showShortToast(getApplicationContext(),"向右移动");
                            return true;

                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN:

                            return false;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        //icon5
        customBinding.rlMuqiIcon5.setOnClickListener(this);
        //icon6
        customBinding.rlMuqiIcon6.setOnClickListener(this);
        //icon7
        customBinding.rlMuqiIcon7.setOnClickListener(this);
        //left
        customBinding.muqiLeft.setOnClickListener(this);
        //right
        customBinding.muqiRight.setOnClickListener(this);
        // 电池状态
        initBattery();

        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this) {
            @Override
            public boolean canScrollHorizontally() {
                // 禁用水平滚动
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //定义Item之间的间距
        customBinding.shortcutsRv.addItemDecoration(new SpacesItemDecoration(0,
                (int) getResources().getDimension(R.dimen.x_20), 0, 0));
        customBinding.shortcutsRv.setLayoutManager(layoutManager);
    }

    public void initBattery() {
        Log.d(TAG, "电池状态 初始化");

        if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryEnable).equals("1")) {//是否有电池
            Log.d(TAG, "电池状态 初始化 有电池");
            customBinding.rlBattery.setVisibility(View.VISIBLE);
            if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("1")) {
                Log.d(TAG, "电池状态 初始化 正在充电");
                switch (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryLevel)) {
                    case "0":
                        customBinding.battery.setImageResource(R.drawable.battery_charging_1);
                        break;
                    case "1":
                        customBinding.battery.setImageResource(R.drawable.battery_charging_2);
                        break;
                    case "2":
                        customBinding.battery.setImageResource(R.drawable.battery_charging_3);
                        break;
                    case "3":
                        customBinding.battery.setImageResource(R.drawable.battery_charging_4);
                        break;
                    case "4":
                        customBinding.battery.setImageResource(R.drawable.battery_charging_5);
                        break;
                }
            } else if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("0")) {
                Log.d(TAG, "电池状态 初始化 没充电");
                switch (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryLevel)) {
                    case "0":
                        customBinding.battery.setImageResource(R.drawable.battery_1);
                        break;
                    case "1":
                        customBinding.battery.setImageResource(R.drawable.battery_2);
                        break;
                    case "2":
                        customBinding.battery.setImageResource(R.drawable.battery_3);
                        break;
                    case "3":
                        customBinding.battery.setImageResource(R.drawable.battery_4);
                        break;
                    case "4":
                        customBinding.battery.setImageResource(R.drawable.battery_5);
                        break;
                }
            }
        } else {
            Log.d(TAG, "电池状态 初始化 没有电池");
        }

    }

    @Override
    public void setBatteryLevel(String level) {
        Log.d(TAG, "电池状态 setBatteryLevel");
        switch (level) {
            case "0":
                if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("1")) {
                    customBinding.battery.setImageResource(R.drawable.battery_charging_1);
                } else if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("0")) {
                    customBinding.battery.setImageResource(R.drawable.battery_1);
                }
                break;
            case "1":
                if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("1")) {
                    customBinding.battery.setImageResource(R.drawable.battery_charging_2);
                } else if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("0")) {
                    customBinding.battery.setImageResource(R.drawable.battery_2);
                }
                break;
            case "2":
                if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("1")) {
                    customBinding.battery.setImageResource(R.drawable.battery_charging_3);
                } else if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("0")) {
                    customBinding.battery.setImageResource(R.drawable.battery_3);
                }
                break;
            case "3":
                if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("1")) {
                    customBinding.battery.setImageResource(R.drawable.battery_charging_4);
                } else if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("0")) {
                    customBinding.battery.setImageResource(R.drawable.battery_4);
                }
                break;
            case "4":
                if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("1")) {
                    customBinding.battery.setImageResource(R.drawable.battery_charging_5);
                } else if (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryDc).equals("0")) {
                    customBinding.battery.setImageResource(R.drawable.battery_5);
                }
                break;
        }

    }

    @Override
    public void Plug_in_charger() {
        Log.d(TAG, "电池状态 Plug_in_charger");
        switch (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryLevel)) {
            case "0":
                customBinding.battery.setImageResource(R.drawable.battery_charging_1);
                break;
            case "1":
                customBinding.battery.setImageResource(R.drawable.battery_charging_2);
                break;
            case "2":
                customBinding.battery.setImageResource(R.drawable.battery_charging_3);
                break;
            case "3":
                customBinding.battery.setImageResource(R.drawable.battery_charging_4);
                break;
            case "4":
                customBinding.battery.setImageResource(R.drawable.battery_charging_5);
                break;
        }
    }

    @Override
    public void Unplug_the_charger() {
        Log.d(TAG, "电池状态 Unplug_the_charger");
        switch (SystemPropertiesUtil.getSystemProperty(SystemPropertiesUtil.batteryLevel)) {
            case "0":
                customBinding.battery.setImageResource(R.drawable.battery_1);
                break;
            case "1":
                customBinding.battery.setImageResource(R.drawable.battery_2);
                break;
            case "2":
                customBinding.battery.setImageResource(R.drawable.battery_3);
                break;
            case "3":
                customBinding.battery.setImageResource(R.drawable.battery_4);
                break;
            case "4":
                customBinding.battery.setImageResource(R.drawable.battery_5);
                break;
        }
    }

    private void initDataCustom() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //读取首页的配置文件，优先读取网络服务器配置，其次读本地配置。只读取一次，清除应用缓存可触发再次读取。
                initDataApp();
                short_list = loadHomeAppData();
                Log.d(TAG, " initDataCustom快捷图标 short_list " + short_list.size());
//                handler.sendEmptyMessage(204);
                appInfoBeans = AppUtils.getApplicationMsg(MainActivity.this);
                if (appInfoBeans != null) {
                    circularQueue = new CircularQueue<>(appInfoBeans);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 设置首页的配置图标
                            try {
                                update7Icon(circularQueue.front, circularQueue.rear);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
//                handler.sendEmptyMessage(204);
                }
                handler.sendEmptyMessage(205);
            }
        }).start();
    }

    private void initReceiver() {
        IntentFilter networkFilter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        networkReceiver = new NetworkReceiver();
        networkReceiver.setNetWorkCallBack(this);
        registerReceiver(networkReceiver, networkFilter);

        // 时间变化 分为单位
        timeReceiver = new MyTimeReceiver(this);
        timeFilter.addAction(Intent.ACTION_TIME_CHANGED);
        timeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        timeFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        timeFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        timeFilter.addAction(Intent.ACTION_USER_SWITCHED);
        timeFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(timeReceiver, timeFilter);

        // wifi
        wifiFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        wifiReceiver = new MyWifiReceiver(this);
        registerReceiver(wifiReceiver, wifiFilter);

        // 蓝牙
        // blueFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        // blueFilter
        // .addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        // blueFilter.addAction("android.bluetooth.device.action.FOUND");
        // blueFilter
        // .addAction("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED");
        blueFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        blueFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        blueReceiver = new BluetoothReceiver(this);
        registerReceiver(blueReceiver, blueFilter);

        //Usb设备插入、拔出
        usbDeviceReceiver = new UsbDeviceReceiver(this);
//        usbDeviceFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        usbDeviceFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDeviceFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbDeviceFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbDeviceFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        usbDeviceFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbDeviceFilter.addDataScheme("file");
        registerReceiver(usbDeviceReceiver, usbDeviceFilter);

        //APP安装、改变、卸载
        appReceiver = new AppReceiver(this);
        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        appFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appFilter.addDataScheme("package");
        registerReceiver(appReceiver, appFilter);

        //电量变化
        batteryReceiver = new BatteryReceiver(this, this);
        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction("action.projector.dcin");
        batteryFilter.addAction("action.projector.batterylevel");
        registerReceiver(batteryReceiver, batteryFilter);


    }

//    ShortcutsAdapter.ItemCallBack itemCallBack = new ShortcutsAdapter.ItemCallBack() {
//        @Override
//        public void onItemClick(int i) {
//            if (i < short_list.size()) {
//                if (short_list.get(i).getAppname() != null) {
//                    AppUtils.startNewApp(MainActivity.this, short_list.get(i).getPackageName());
//                } else if (appsDataList != null) {
//                    AppsData appsData = findAppsData(short_list.get(i).getPackageName());
//                    if (appsData != null) {
//                        Intent intent = new Intent();
//                        intent.setComponent(new ComponentName("com.htc.storeos", "com.htc.storeos.AppDetailActivity"));
//                        intent.putExtra("appData", new Gson().toJson(appsData));
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    } else {
//                        ToastUtil.showShortToast(getBaseContext(), getString(R.string.data_none));
//                    }
//                } else {
//                    ToastUtil.showShortToast(getBaseContext(), getString(R.string.data_none));
//                }
//            } else {
//                AppUtils.startNewActivity(MainActivity.this, AppFavoritesActivity.class);
//            }
//        }
//    };

    ShortcutsAdapterCustom.ItemCallBack itemCallBackCustom = new ShortcutsAdapterCustom.ItemCallBack() {
        @Override
        public void onItemClick(int i, String name) {
            if (i < short_list.size()) {

                Log.d(TAG, " xuhao执行点击前 " + i);
                if (i == 0) {
                    Log.d(TAG, " 打开APP详情页");
                    startNewActivity(AppsActivity.class);
                    return;
                }
                Log.d(TAG, " short_list.get(i).getPackageName() " + short_list.get(i).getPackageName());
                if (!AppUtils.startNewApp(MainActivity.this, short_list.get(i).getPackageName())) {
                    appName = name;
                    requestChannelData();
                }

            } else {
                AppUtils.startNewActivity(MainActivity.this, AppFavoritesActivity.class);
            }
        }
    };

    ShortcutsAdapterMuQi.ItemCallBack itemCallBackMuQi = new ShortcutsAdapterMuQi.ItemCallBack() {
        @Override
        public void onItemClick(int i, String name) {
            if (i < short_list.size()) {

                Log.d(TAG, " xuhao执行点击前 " + i);
                if (i == 0) {
                    Log.d(TAG, " 打开APP详情页");
                    startNewActivity(AppsActivity.class);
                    return;
                }
                Log.d(TAG, " short_list.get(i).getPackageName() " + short_list.get(i).getPackageName());
                if (!AppUtils.startNewApp(MainActivity.this, short_list.get(i).getPackageName())) {
                    appName = name;
                    requestChannelData();
                }

            } else {
                AppUtils.startNewActivity(MainActivity.this, AppFavoritesActivity.class);
            }
        }
    };


//    public AppsData findAppsData(String pkg) {
//        for (AppsData appsData : appsDataList) {
//            if (appsData.getApp_id().equals(pkg))
//                return appsData;
//        }
//        return null;
//    }


    private void requestAppData() {
        String channel = SystemProperties.get("persist.sys.Channel", "project");
        String url = Uri.BASE_URL + channel + "/channel_apps_" + Locale.getDefault().getLanguage() + ".xml";
        RequestManager.getInstance().getData(url, channelCallback);
    }

    Callback channelCallback = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
            LogUtils.d("onFailure()");
            handler.sendEmptyMessage(DATA_ERROR);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            try {
                String content = response.body().string();
                LogUtils.d("content " + content);
                if (RequestManager.isOne(Uri.complexType, 3)) {
                    byte[] bytes = Base64.decode(content, Base64.NO_WRAP);
                    content = new String(VerifyUtil.gzipDecompress(bytes), StandardCharsets.UTF_8);
                    LogUtils.d("content " + content);
                }
                channelData = new Gson().fromJson(content, ChannelData.class);
                if (channelData.getCode() != 0) {
                    handler.sendEmptyMessage(DATA_ERROR);
                } else {
                    handler.sendEmptyMessage(DATA_FINISH);
                }

            } catch (Exception e) {
                handler.sendEmptyMessage(DATA_ERROR);
            }
        }
    };

    private void responseErrorRedirect() {
        if (!get_default_url) {
            get_default_url = true;
            String channel = SystemProperties.get("persist.sys.Channel", "project");
            String url = Uri.BASE_URL + channel + "/channel_apps_global.xml";
            RequestManager.getInstance().getData(url, channelCallback);
        }
    }

    @Override
    public void onClick(View v) {
        String appname = null;
        String action = null;
        switch (v.getId()) {
            case R.id.rl_usb_connect:
                AppUtils.startNewApp(MainActivity.this, "com.hisilicon.explorer");
                break;
            case R.id.signal_source:
                try {
                    String listaction = DBUtils.getInstance(this).getActionFromListModules("list3");
                    if (listaction != null && !listaction.equals("")) { //读取配置
                        goAction(listaction);
                    } else {// 默认跳转
                        startSource("HDMI1");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.rl_wallpapers:
                startNewActivity(WallPaperActivity.class);
                break;
            case R.id.rl_bluetooth:
                startNewActivity(BluetoothActivity.class);
                break;
            case R.id.rl_muqi_usb:
                AppUtils.startNewApp(MainActivity.this, "com.hisilicon.explorer");
                break;
            case R.id.rl_muqi_settings:
                startNewActivity(MainSettingActivity.class);
                break;
            case R.id.rl_muqi_icon4:
                if (appInfoBeans != null && circularQueue != null) {
                    int icon4position = -1;
                    if (circularQueue.front + 3 < appInfoBeans.size()) {
                        icon4position = circularQueue.front + 3;
                    } else if (circularQueue.front + 3 == appInfoBeans.size()) {
                        icon4position = 0;
                    } else if (circularQueue.front + 3 > appInfoBeans.size()) {
                        icon4position = circularQueue.front + 3 - appInfoBeans.size();
                    }
                    AppUtils.startNewApp(MainActivity.this, appInfoBeans.get(icon4position).getApppackagename());
                }
                break;
            case R.id.muqi_left:
                if (circularQueue != null) {
                    circularQueue.moveLeft();
                    update7Icon(circularQueue.front, circularQueue.rear);
                }
                if (getButtonSound()) {
                    audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
                }
                break;
            case R.id.muqi_right:
                if (circularQueue != null) {
                    circularQueue.moveRight();
                    update7Icon(circularQueue.front, circularQueue.rear);
                }
                if (getButtonSound()) {
                    audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
                }
                break;
        }
    }

    private void goAction(String listaction) {
        Log.d(TAG, " goAction list配置跳转 " + listaction);
        if (listaction.contains("/")) {
            String[] parts = listaction.split("/", 2);
            String packageName = parts[0];
            String activityName = parts[1];
            Log.d(TAG, " goAction 包名活动名 " + packageName + " " + activityName);
            startNewActivity(packageName, activityName);
        } else if (listaction.equals("HDMI1") || listaction.equals("HDMI2") || listaction.equals("VGA") || listaction.equals("CVBS1")) {
            startSource(listaction);
        } else {
            AppUtils.startNewApp(MainActivity.this, listaction);
        }
    }

    private void startSource(String sourceName) {
        Intent intent_hdmi = new Intent();
        intent_hdmi.setComponent(new ComponentName("com.softwinner.awlivetv", "com.softwinner.awlivetv.MainActivity"));
        intent_hdmi.putExtra("input_source", sourceName);
        intent_hdmi.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent_hdmi.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent_hdmi);
    }

    /**
     * 第一次初始化默认快捷栏app数据
     */
    private boolean initDataApp() {
        boolean isLoad = true;
        SharedPreferences sharedPreferences = ShareUtil.getInstans(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int code = sharedPreferences.getInt("code", 0);
        Log.d(TAG, " initDataApp读code值 " + code);
        if (code == 0) {  //保证配置文件只在最初读一次

            //1、优先连接服务器读取配置

            //2、服务器没有，就读本地
            Log.d(TAG, " MainActivity开始读取配置文件 ");

            // 读取文件,优先读取oem分区
            File file = new File("/oem/shortcuts.config");

            if (!file.exists()) {
                file = new File("/system/shortcuts.config");
            }

            if (!file.exists()) {
                Log.d(TAG, " 配置文件不存在 ");
                DBUtils.getInstance(this).deleteTable();

                editor.putInt("code", 1);
                editor.apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 设置首页的配置图标
                        try {
                            setDefaultBackground();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                return false;
            }

            try {
                FileInputStream is = new FileInputStream(file);
                byte[] b = new byte[is.available()];
                is.read(b);
                String result = new String(b);

                Log.d(TAG, " MainActivity读取到的配置文件 " + result); //这里把配置文件原封不动的读取出来，不做一整行处理

                List<String> residentList = new ArrayList<>();
                JSONObject obj = new JSONObject(result);

                //读取默认背景配置 这块提前放到MyApplication中
//                readDefaultBackground(obj);

                //读取首页四大APP图标
                readMain(obj);

                //读取APP快捷图标
                readShortcuts(obj, residentList, sharedPreferences);

                //读取filterApps屏蔽显示的APP
                readFilterApps(obj);

                //读取右边list第一个、第三个的配置
                readListModules(obj);
                Log.d(TAG, " 当前的语言环境是： " + LanguageUtil.getCurrentLanguage());

                //读取品牌图标
                readBrand(obj);

                //是否显示时间
                //readTime();

                editor.putString("resident", residentList.toString());

                editor.putInt("code", 1);
                editor.apply();
                is.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                isLoad = false;
            }
        }

        //设置首页的配置图标
        // 在主线程中更新 UI
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // 设置首页的配置图标
//                try {
//                    setIconOrText();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        return isLoad;
    }

    private void readDefaultBackground(JSONObject obj) {
        try {
            if (obj.has("defaultbackground")) {
                String DefaultBackground = obj.getString("defaultbackground").trim();
                Log.d(TAG, " readDefaultBackground " + DefaultBackground);
                // 将字符串存入数据库；
                SharedPreferences sharedPreferences = ShareUtil.getInstans(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Contants.DefaultBg, DefaultBackground);
                editor.apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readMain(JSONObject obj) {
        try {
            if (obj.has("mainApp")) {
                JSONArray jsonarrray = obj.getJSONArray("mainApp");

                for (int i = 0; i < jsonarrray.length(); i++) {
                    JSONObject jsonobject = jsonarrray.getJSONObject(i);
                    String tag = jsonobject.getString("tag");
                    String appName = jsonobject.getString("appName");
                    String iconPath = jsonobject.getString("iconPath");
                    String action = jsonobject.getString("action");

                    Log.d(TAG, " 读取到的mainApp " + tag + appName + iconPath + action);

                    //从iconPath中把png读出来赋值给drawable
                    Drawable drawable = FileUtils.loadImageAsDrawable(this, iconPath);

                    //把读到的数据放入db数据库
                    DBUtils.getInstance(this).insertMainAppData(tag, appName, drawable, action);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void readShortcuts(JSONObject obj, List<String> residentList, SharedPreferences
            sharedPreferences) {
        try {
            if (obj.has("apps")) {
                JSONArray jsonarrray = obj.getJSONArray("apps");
                //xuhao
                //用户每次更新配置，必须把原来数据库中保存的上一次失效的数据清除掉
                ArrayList<AppSimpleBean> mylist = DBUtils.getInstance(this).getFavorites();
                for (int i = 0; i < jsonarrray.length(); i++) {
                    JSONObject jsonobject = jsonarrray.getJSONObject(i);
                    String packageName = jsonobject.getString("packageName");

                    for (int d = 0; d < mylist.size(); d++) {
                        Log.d(TAG, " 对比 " + mylist.get(d).getPackagename() + " " + packageName);
                        if (mylist.get(d).getPackagename().equals(packageName)) { //去除掉两个队列中相同的部分
                            Log.d(TAG, " 移除两个队列中的相同部分 " + packageName + mylist.size());
                            mylist.remove(d);
                            Log.d(TAG, " mylist.size " + mylist.size());
                            break;
                        }
                    }
                }
                for (int d = 0; d < mylist.size(); d++) { //剩余的不同的就是无效的，把无效的delet，保证每次修改配置之后都正确生效
                    if (sharedPreferences.getString("resident", "").contains(mylist.get(d).getPackagename())) {
                        Log.d(TAG, " 移除APP快捷图标栏废弃的配置 ");
                        DBUtils.getInstance(this).deleteFavorites(mylist.get(d).getPackagename());
                    }
                }
                //xuhao
                for (int i = 0; i < jsonarrray.length(); i++) {
                    JSONObject jsonobject = jsonarrray.getJSONObject(i);
                    String appName = jsonobject.getString("appName");
                    String packageName = jsonobject.getString("packageName");
                    String iconPath = jsonobject.getString("iconPath");
                    boolean resident = jsonobject.getBoolean("resident"); //用于标志移除上一轮配置文件和这一轮配置文件不需要的App
                    if (resident) {
                        residentList.add(packageName);
                    }
                    Drawable drawable = FileUtils.loadImageAsDrawable(this, iconPath);
                    if (!DBUtils.getInstance(this).isExistData(
                            packageName)) {
                        long addCode = DBUtils.getInstance(this)
                                .addFavorites(appName, packageName, drawable);
                        Log.d(TAG, " Shortcuts 添加快捷数据库成功 " + appName + " " + packageName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFilterApps(JSONObject obj) {
        try {
            if (obj.has("filterApps")) {
                String filterApps = obj.getString("filterApps");
                Log.d(TAG, " readFilterApps " + filterApps);
                // 将字符串按分号拆分成数组
                String[] packageNames = filterApps.split(";");
                DBUtils.getInstance(this).insertFilterApps(packageNames);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void readListModules(JSONObject obj) {
        try {
            if (obj.has("listModules")) {
                JSONArray jsonarrray = obj.getJSONArray("listModules");
                for (int i = 0; i < jsonarrray.length(); i++) {
                    JSONObject jsonobject = jsonarrray.getJSONObject(i);
                    String tag = jsonobject.getString("tag");
                    String iconPath = jsonobject.getString("iconPath");
//                    JSONObject textObject = jsonobject.getJSONObject("text");
//                    String zhCN = textObject.getString("zh-CN");
//                    String zhTW = textObject.getString("zh-TW");
//                    String zhHK = textObject.getString("zh-HK");
//                    String ko = textObject.getString("ko-KR");
//                    String ja = textObject.getString("ja-JP");
//                    String en = textObject.getString("en-US");
//                    String ru = textObject.getString("ru-RU");
//                    String ar = textObject.getString("ar-EG");
                    String action = jsonobject.getString("action");
//                    hashtable.put("zh-CN", zhCN);
//                    hashtable.put("zh-TW", zhTW);
//                    hashtable.put("zh-HK", zhHK);
//                    hashtable.put("ko-KR", ko);
//                    hashtable.put("ja-JP", ja);
//                    hashtable.put("en-US", en);
//                    hashtable.put("ru-RU", ru);
//                    hashtable.put("ar-EG", ar);
                    JSONObject textObject = jsonobject.getJSONObject("text");
                    JSONArray keys = textObject.names();
                    Log.d(TAG, " 读取到的listModules keys " + keys);
                    if (keys != null) {
                        for (int b = 0; b < keys.length(); b++) {
                            String key = keys.getString(b);
                            String value = textObject.getString(key);
                            Log.d(TAG, " 读取到的listModules " + tag + iconPath + key + value);
                            hashtable.put(key, value);
                        }
                    }
                    //从iconPath中把png读出来赋值给drawable
                    Drawable drawable = FileUtils.loadImageAsDrawable(this, iconPath);
                    //将读取到的数据写入数据库
                    DBUtils.getInstance(this).insertListModulesData(tag, drawable, hashtable, action);
                    hashtable.clear();
//                DBUtils.getInstance(this).getHashtableFromDatabase("list1");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readBrand(JSONObject obj) {
        try {
            if (obj.has("brandLogo")) {
                JSONObject jsonobject = obj.getJSONObject("brandLogo");
                String iconPath = jsonobject.getString("iconPath");
                Drawable drawable = FileUtils.loadImageAsDrawable(this, iconPath);
                DBUtils.getInstance(this).insertBrandLogoData(drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ArrayList<ShortInfoBean> loadHomeAppData() {

        ArrayList<AppSimpleBean> appSimpleBeans = DBUtils.getInstance(this).getFavorites(); //获取配置文件中设置的首页显示App

        ArrayList<ShortInfoBean> shortInfoBeans = new ArrayList<>();

        ArrayList<AppInfoBean> appList = AppUtils.getApplicationMsg(this);//获取所有的应用(排除了配置文件中拉黑的App)

        //xuhao add 默认添加我的应用按钮
        ShortInfoBean mshortInfoBean = new ShortInfoBean();
        mshortInfoBean.setAppicon(ContextCompat.getDrawable(this, R.drawable.muqi_apps));
        shortInfoBeans.add(mshortInfoBean);
        //xuhao

        Log.d(TAG, " loadHomeAppData快捷图标 appList " + appList.size());
        Log.d(TAG, " loadHomeAppData快捷图标 appSimpleBeans " + appSimpleBeans.size());
        for (int i = 0; i < appSimpleBeans.size(); i++) {
            ShortInfoBean shortInfoBean = new ShortInfoBean();
            shortInfoBean.setPackageName(appSimpleBeans.get(i).getPackagename());

            for (int j = 0; j < appList.size(); j++) {
                if (appSimpleBeans.get(i).getPackagename()
                        .equals(appList.get(j).getApppackagename())) {
                    shortInfoBean.setAppicon(appList.get(j).getAppicon());
                    shortInfoBean.setAppname(appList.get(j).getAppname());
                    break;
                }
            }
            shortInfoBeans.add(shortInfoBean);
        }

        return shortInfoBeans;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            return true;
        return super.dispatchKeyEvent(event);
    }

    private void updateBle() {
        boolean isConnected = BluetoothUtils.getInstance(this)
                .isBluetoothConnected();
        if (isConnected) {
////            mainBinding.homeBluetooth.setBackgroundResource(R.drawable.bluetooth_con);
//            customBinding.homeBluetooth.setImageResource(R.drawable.bt_custom_green);
//        } else {
////            mainBinding.homeBluetooth.setBackgroundResource(R.drawable.bluetooth_not);
//            customBinding.homeBluetooth.setImageResource(R.drawable.bt_custom2);
        }
    }

    private void updateTime() {
//        String builder = TimeUtils.getCurrentDate() +
//                " | " +
//                TimeUtils
//                        .getCurrentTime(this);
//        mainBinding.timeTv.setText(builder);

//        customBinding.timeTv.setText(TimeUtils.getCurrentTime(this));
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(networkReceiver);
        unregisterReceiver(timeReceiver);
        unregisterReceiver(blueReceiver);
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(usbDeviceReceiver);
        unregisterReceiver(appReceiver);
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }

    @Override
    public void bluetoothChange() {
        updateBle();
    }

    @Override
    public void UsbDeviceChange() {

        Log.d("UsbDeviceChange ", String.valueOf(Utils.hasUsbDevice));

        if (Utils.hasUsbDevice) {
            Log.d("UsbDeviceChange ", "usbConnect设为VISIBLE");
            customBinding.rlUsbConnect.setVisibility(View.VISIBLE);
        } else {
            customBinding.rlUsbConnect.clearFocus();
            customBinding.rlUsbConnect.clearAnimation();
            customBinding.rlUsbConnect.setVisibility(View.GONE);
            Log.d("UsbDeviceChange ", "usbConnect设为GONE");
        }
    }

    @Override
    public void changeTime() {
        updateTime();
    }

    @Override
    public void getWifiState(int state) {
        if (state == 1) {
//            mainBinding.homeWifi.setBackgroundResource(R.drawable.wifi_not);
//            customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_4);
        }
    }

//    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
//        @Override
//        public void onFocusChange(View v, boolean hasFocus) {
//            AnimationSet animationSet = new AnimationSet(true);
//            v.bringToFront();
//            if (hasFocus) {
//                ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.50f,
//                        1.0f, 1.50f, Animation.RELATIVE_TO_SELF, 0.5f,
//                        Animation.RELATIVE_TO_SELF, 0.5f);
//                scaleAnimation.setDuration(150);
//                animationSet.addAnimation(scaleAnimation);
//                animationSet.setFillAfter(true);
//                v.startAnimation(animationSet);
//            } else {
//                ScaleAnimation scaleAnimation = new ScaleAnimation(1.50f, 1.0f,
//                        1.50f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
//                        Animation.RELATIVE_TO_SELF, 0.5f);
//                animationSet.addAnimation(scaleAnimation);
//                scaleAnimation.setDuration(150);
//                animationSet.setFillAfter(true);
//                v.startAnimation(animationSet);
//            }
//        }
//    };

//    @Override
//    public void getWifiNumber(int count) {
//
//        List<ScanResult> wifiList = wifiManager.getScanResults();
//        Log.d(TAG,"getWifiNumber "+count);
//        switch (count) {
//            case -1:
////                mainBinding.homeWifi.setBackgroundResource(R.drawable.wifi_not);
//                customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_4);
//                break;
//            case 0:
////                mainBinding.homeWifi.setBackgroundResource(R.drawable.bar_wifi_1_focus);
//                customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_1);
//                break;
//            case 1:
////                mainBinding.homeWifi.setBackgroundResource(R.drawable.bar_wifi_2_focus);
//                customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_2);
//                break;
//            case 2:
////                mainBinding.homeWifi.setBackgroundResource(R.drawable.bar_wifi_2_focus);
//                customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_3);
//                break;
//            case 3:
////                mainBinding.homeWifi.setBackgroundResource(R.drawable.bar_wifi_2_focus);
//                customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_4);
//                break;
//            default:
////                mainBinding.homeWifi.setBackgroundResource(R.drawable.bar_wifi_full_focus);
//                customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_4);
//                break;
//
//        }
//    }

    @Override
    public void getWifiNumber(int count) {

//        List<ScanResult> wifiList = wifiManager.getScanResults();
//        Log.d(TAG, "getWifiNumber " + count);
//
//        if (count == 1) {
//            customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_4);
//            return;
//        } else if (count == 3) {
//            customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_4);
//            return;
//        }
//
//        Log.d(TAG, " level数据" + count);
//        if (count < -85) {
//            customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_1);
//        } else if (count < -70) {
//            customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_2);
//        } else if (count < -60) {
//            customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_3);
//        } else if (count < -50) {
//            customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_4);
//        } else {
//            customBinding.homeWifi.setImageResource(R.drawable.wifi_custom_green_4);
//        }
    }

    @Override
    public void connect() {
//        if (isFrist) {
//            isFrist = false;
//            requestAppData();
//        }
    }

    @Override
    public void disConnect() {

    }

    private void requestChannelData() {
        if (requestFlag)
            return;

        if (!NetWorkUtils.isNetworkConnected(this)) {
            ToastUtil.showShortToast(this, getString(R.string.network_disconnect_tip));
            return;
        }
        requestFlag = true;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        OkHttpClient okHttpClient = builder.build();
        String time = String.valueOf(System.currentTimeMillis());
        String chan = Constants.getChannel();
        LogUtils.d("chanId " + chan);
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.addHeader("chanId", chan);
        requestBuilder.addHeader("timestamp", time);
        HashMap<String, Object> requestData = new HashMap<>();
        requestData.put("chanId", chan);
        String deviceId = Constants.getWan0Mac();
        if (Constants.isOne(Uri.complexType, 1)) {
            String aesKey = VerifyUtil.initKey();
            LogUtils.d("aesKey " + aesKey);
            deviceId = VerifyUtil.encrypt(deviceId, aesKey, aesKey, VerifyUtil.AES_CBC);
            LogUtils.d("deviceId " + deviceId);
        }
        requestData.put("deviceId", deviceId);
        requestData.put("model", SystemProperties.get("persist.sys.modelName", "project"));
        requestData.put("sysVersion", Constants.getHtcDisplay());
        try {
            requestData.put("verCode", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            requestData.put("verCode", 10);
            throw new RuntimeException(e);
        }

        requestData.put("complexType", Uri.complexType);//
        Gson gson = new Gson();
        String json = gson.toJson(requestData);
        requestBuilder.url(Uri.SIGN_APP_LIST_URL)
                .post(RequestBody.create(json, MediaType.parse("application/json;charset=UTF-8")));
        String sign = RequestManager.getInstance().getSign(json, chan, time);
        LogUtils.d("sign " + sign);
        requestBuilder.addHeader("sign", sign);
        Request request = requestBuilder.build();
        okHttpClient.newCall(request).enqueue(channelCallback);
    }

    private void startAppFormChannel() {
        for (AppsData appsData : channelData.getData()) {
            if (appName.equals(appsData.getName())) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.htc.storeos", "com.htc.storeos.AppDetailActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("appData", new Gson().toJson(appsData));
                startActivity(intent);
                return;
            }
        }
        ToastUtil.showShortToast(this, getString(R.string.data_none));
    }

    private void setIconOrText() {

        //1、MainApp
        setMainApp();

        //2、ListModules
        setListModules();

        //3、brandLogo
        setbrandLogo();

        //4、DefaultBackground
        setDefaultBackground();

    }

    private void setMainApp() {
        Drawable drawable = DBUtils.getInstance(this).getIconDataByTag("icon1");
        if (drawable != null) {
//            customBinding.icon1.setImageDrawable(drawable);
        }

        drawable = DBUtils.getInstance(this).getIconDataByTag("icon2");
        if (drawable != null) {
//            customBinding.icon2.setImageDrawable(drawable);
        }

        drawable = DBUtils.getInstance(this).getIconDataByTag("icon3");
        if (drawable != null) {
//            customBinding.icon3.setImageDrawable(drawable);
        }

        drawable = DBUtils.getInstance(this).getIconDataByTag("icon4");
        if (drawable != null) {
//            customBinding.icon4.setImageDrawable(drawable);
        }
    }

    private void setListModules() {
        Drawable drawable = DBUtils.getInstance(this).getDrawableFromListModules("list1");
        if (drawable != null) {
//            customBinding.eshareIcon.setImageDrawable(drawable);
            drawable = null;
        }

        drawable = DBUtils.getInstance(this).getDrawableFromListModules("list3");
        if (drawable != null) {
//            customBinding.hdmiIcon.setImageDrawable(drawable);
            drawable = null;
        }

        Hashtable<String, String> mHashtable1 = DBUtils.getInstance(this).getHashtableFromListModules("list1");
        Hashtable<String, String> mHashtable2 = DBUtils.getInstance(this).getHashtableFromListModules("list3");

        Log.d(TAG, "xu当前语言" + LanguageUtil.getCurrentLanguage());

        if (mHashtable1 != null) {
            String text = mHashtable1.get(LanguageUtil.getCurrentLanguage());
            Log.d(TAG, "xu当前语言 text eshareText" + text);
            if (text != null && !text.isEmpty()) {
//                customBinding.eshareText.setText(text);
            }

//            switch (LanguageUtil.getCurrentLanguage()) {
//                case "zh-CN":
//                    Log.d(TAG, "中文设置eshareText和hdmiText");
//                    text = mHashtable1.get("zh-CN");
//                    if (text != null && !text.equals("")) {
//                        customBinding.eshareText.setText(mHashtable1.get("zh-CN"));
//                    }
//                    break;
//                case "zh-TW":
//                    text = mHashtable1.get("zh-TW");
//                    if (text != null && !text.equals("")) {
//                        customBinding.eshareText.setText(mHashtable1.get("zh-TW"));
//                    }
//                    break;
//                case "zh-HK":
//                    text = mHashtable1.get("zh-HK");
//                    if (text != null && !text.equals("")) {
//                        customBinding.eshareText.setText(mHashtable1.get("zh-HK"));
//                    }
//                    break;
//                case "ko-KR":
//                    text = mHashtable1.get("ko-KR");
//                    if (text != null && !text.equals("")) {
//                        customBinding.eshareText.setText(mHashtable1.get("ko-KR"));
//                    }
//                    break;
//                case "ja-JP":
//                    text = mHashtable1.get("ja-JP");
//                    if (text != null && !text.equals("")) {
//                        customBinding.eshareText.setText(mHashtable1.get("ja-JP"));
//                    }
//                    break;
//                case "en-US":
//                    text = mHashtable1.get("en-US");
//                    if (text != null && !text.equals("")) {
//                        customBinding.eshareText.setText(mHashtable1.get("en-US"));
//                    }
//                    break;
//                case "ru-RU":
//                    text = mHashtable1.get("ru-RU");
//                    if (text != null && !text.equals("")) {
//                        customBinding.eshareText.setText(mHashtable1.get("ru-RU"));
//                    }
//                    break;
//                case "ar-EG":
//                    text = mHashtable1.get("ar-EG");
//                    if (text != null && !text.equals("")) {
//                        customBinding.eshareText.setText(mHashtable1.get("ar-EG"));
//                    }
//                    break;
//            }
        }

        if (mHashtable2 != null) {
            String text = mHashtable2.get(LanguageUtil.getCurrentLanguage());
            Log.d(TAG, "xu当前语言 text hdmiText" + text);
            if (text != null && !text.isEmpty()) {
//                customBinding.hdmiText.setText(text);
            }
//            switch (LanguageUtil.getCurrentLanguage()) {
//                case "zh-CN":
//                    Log.d(TAG, "中文设置eshareText和hdmiText");
//                    text = mHashtable2.get("zh-CN");
//                    if (text != null && !text.equals("")) {
//                        customBinding.hdmiText.setText(mHashtable2.get("zh-CN"));
//                    }
//                    break;
//                case "zh-TW":
//                    text = mHashtable2.get("zh-TW");
//                    if (text != null && !text.equals("")) {
//                        customBinding.hdmiText.setText(mHashtable2.get("zh-TW"));
//                    }
//                    break;
//                case "zh-HK":
//                    text = mHashtable2.get("zh-HK");
//                    if (text != null && !text.equals("")) {
//                        customBinding.hdmiText.setText(mHashtable2.get("zh-HK"));
//                    }
//                    break;
//                case "ko-KR":
//                    text = mHashtable2.get("ko-KR");
//                    if (text != null && !text.equals("")) {
//                        customBinding.hdmiText.setText(mHashtable2.get("ko-KR"));
//                    }
//                    break;
//                case "ja-JP":
//                    text = mHashtable2.get("ja-JP");
//                    if (text != null && !text.equals("")) {
//                        customBinding.hdmiText.setText(mHashtable2.get("ja-JP"));
//                    }
//                    break;
//                case "en-US":
//                    text = mHashtable2.get("en-US");
//                    if (text != null && !text.equals("")) {
//                        customBinding.hdmiText.setText(mHashtable2.get("en-US"));
//                    }
//                    break;
//                case "ru-RU":
//                    text = mHashtable2.get("ru-RU");
//                    if (text != null && !text.equals("")) {
//                        customBinding.hdmiText.setText(mHashtable2.get("ru-RU"));
//                    }
//                    break;
//                case "ar-EG":
//                    text = mHashtable2.get("ar-EG");
//                    if (text != null && !text.equals("")) {
//                        customBinding.hdmiText.setText(mHashtable2.get("ar-EG"));
//                    }
//                    break;
//            }
        }

//        if (mHashtable2 != null) {
//            String text = null;
//            switch (LanguageUtil.getCurrentLanguage()) {
//                case "zh-CN":
//                    Log.d(TAG, "中文设置eshareText和hdmiText");
//                    customBinding.hdmiText.setText(mHashtable2.get("zh-CN"));
//                    break;
//                case "zh-TW":
//                    customBinding.hdmiText.setText(mHashtable2.get("zh-TW"));
//                    break;
//                case "zh-HK":
//                    customBinding.hdmiText.setText(mHashtable2.get("zh-HK"));
//                    break;
//                case "ko-KR":
//                    customBinding.hdmiText.setText(mHashtable2.get("ko-KR"));
//                    break;
//                case "ja-JP":
//                    customBinding.hdmiText.setText(mHashtable2.get("ja-JP"));
//                    break;
//                case "en-US":
//                    customBinding.hdmiText.setText(mHashtable2.get("en-US"));
//                    break;
//                case "ru-RU":
//                    customBinding.hdmiText.setText(mHashtable2.get("ru-RU"));
//                    break;
//                case "ar-EG":
//                    customBinding.hdmiText.setText(mHashtable2.get("ar-EG"));
//                    break;
//            }
//        }

    }

    private void setbrandLogo() {
        Drawable drawable = DBUtils.getInstance(this).getDrawableFromBrandLogo(1);
        if (drawable != null) {
            customBinding.brand.setImageDrawable(drawable);
        }
    }

    private void setDefaultBackground() {
        //如果用户自主修改了背景，那么重启之后不再设置默认背景start
        SharedPreferences sharedPreferences = ShareUtil.getInstans(getApplicationContext());
        int selectBg = sharedPreferences.getInt(Contants.SelectWallpaperLocal, -1);
        if (selectBg != -1) {
            Log.d(TAG, " setDefaultBackground 用户已经自主修改了背景");
            return;
        }
        //背景控制end
        String defaultbg = sharedPreferences.getString(Contants.DefaultBg, "1");
        Log.d(TAG, " setDefaultBackground defaultbg " + defaultbg);
        int number = Integer.parseInt(defaultbg);
        Log.d(TAG, " setDefaultBackground number " + number);
        if (number > Utils.drawables.size()) {
            Log.d(TAG, " setDefaultBackground 用户设置的默认背景，超出了范围");
            return;
        }
        MyApplication.mainDrawable = (BitmapDrawable) Utils.drawables.get(number - 1);
        setWallPaper(Utils.drawables.get(number - 1));
        setDefaultBg(Utils.drawables.get(number - 1));
    }

    private void setDefaultBg(int resId) {
        threadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                CopyResIdToSd(resId);
                CopyResIdToSd(BlurImageView.BoxBlurFilter(MainActivity.this, resId));
                if (new File(Contants.WALLPAPER_MAIN).exists()) {
                    MyApplication.mainDrawable = new BitmapDrawable(BitmapFactory.decodeFile(Contants.WALLPAPER_MAIN));
                }
                if (new File(Contants.WALLPAPER_OTHER).exists())
                    MyApplication.otherDrawable = new BitmapDrawable(BitmapFactory.decodeFile(Contants.WALLPAPER_OTHER));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 设置首页的配置图标
                        try {
                            setWallPaper();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }

    private void setDefaultBg(Drawable drawable) {
        threadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                CopyDrawableToSd(drawable);
                if (new File(Contants.WALLPAPER_MAIN).exists()) {
                    MyApplication.mainDrawable = new BitmapDrawable(BitmapFactory.decodeFile(Contants.WALLPAPER_MAIN));
                }

            }
        });
    }


    @Override
    public void appChange(String packageName) {
        Log.d(TAG, "MainActivity 收到Change广播");
    }

    @Override
    public void appUnInstall(String packageName) {
        Log.d(TAG, "MainActivity 收到卸载广播 " + packageName);
        SharedPreferences sp = ShareUtil.getInstans(this);
        SharedPreferences.Editor ed = sp.edit();
        String resident = sp.getString("resident", "");
        if (resident.contains(packageName)) {
            Log.d(TAG, " 配置文件中apps：\"resident\":true 常驻首页前台，应用删除了，也不能从首页APP快捷栏移除");
            return;
        }
        DBUtils.getInstance(this).deleteFavorites(packageName);
        short_list = loadHomeAppData();
        handler.sendEmptyMessage(204);
    }

    @Override
    public void appInstall(String packageName) {
        Log.d(TAG, "MainActivity 收到安装广播");
    }

    private void CopyResIdToSd(int resId) {
        File file = new File(Contants.WALLPAPER_DIR);
        if (!file.exists())
            file.mkdir();

        InputStream inputStream = getResources().openRawResource(resId);
        try {
            File file1 = new File(Contants.WALLPAPER_MAIN);
            if (file1.exists())
                file1.delete();

            FileOutputStream fileOutputStream = new FileOutputStream(file1);

            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, bytesRead);
            }
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void CopyResIdToSd(Bitmap bitmap) {
        File file1 = new File(Contants.WALLPAPER_DIR);
        if (!file1.exists())
            file1.mkdir();

        File file = new File(Contants.WALLPAPER_OTHER);//将要保存图片的路径
        if (file.exists())
            file.delete();
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int countUsbDevices(Context context) {
        File storageDir = new File("/storage/");
        int usbCount = 0;

        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] directories = storageDir.listFiles();
            Log.d(TAG, "检测到  directories" + directories);
            if (directories != null) {
                for (File dir : directories) {
                    Log.d(TAG, "检测到  directories");
                    // 检查子目录是否是一个挂载点，并且是否是外部可移动存储
                    if (dir.isDirectory() && dir.canRead() && isUsbDevice(dir)) {
                        usbCount++;
                    }
                }
            }
        }

        Log.d(TAG, "检测到 " + usbCount + " 个U盘");
        return usbCount;
    }

    // 辅助函数，用于判断给定目录是否为 USB 设备
    private boolean isUsbDevice(File dir) {
        try {
            // 获取目录的挂载信息
            String mountInfo = getMountInfo(dir);
            // 检查是否为支持的 USB 设备文件系统格式
            return mountInfo.contains("vfat") ||
                    mountInfo.contains("exfat") ||
                    mountInfo.contains("ntfs") ||
                    mountInfo.contains("fat32") ||
                    mountInfo.contains("fuse");
        } catch (Exception e) {
            Log.e(TAG, "检查目录是否为 USB 设备时出错", e);
            return false;
        }
    }


    // 获取目录的挂载信息
    private String getMountInfo(File dir) {
        try {
            // 使用 "mount" 命令获取挂载信息
            Process process = Runtime.getRuntime().exec("mount");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains(dir.getAbsolutePath())) {
                    output.append(line);
                    break;
                }
            }

            reader.close();
            Log.e(TAG, "检测到 output.toString() " + output.toString());
            return output.toString();
        } catch (IOException e) {
            Log.e(TAG, "获取挂载信息时出错", e);
            return "";
        }
    }


    private void CopyDrawableToSd(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        //判断图片大小，如果超过限制就做缩小处理
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width * height * 4 >= MAX_BITMAP_SIZE) {
            bitmap = narrowBitmap(bitmap);
        }
        //缩小完毕

        File dir = new File(Contants.WALLPAPER_DIR);
        if (!dir.exists()) dir.mkdirs();

        File file1 = new File(Contants.WALLPAPER_MAIN);
//        if (file1.exists()) file1.delete();

        try (FileOutputStream fileOutputStream = new FileOutputStream(file1)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream); // 可根据需要更改格式
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void devicesPathAdd() {
        if (storageManager == null) {
            Log.e(TAG, "devicesPathAdd manager is null return error!");
            return;
        }
        localDevicesList = storageManager.getStorageVolumes();
        Log.d(TAG, " 检测到devicesPathAdd " + localDevicesList.size());
        StorageVolume storageVolume;
        for (int i = 0; i < localDevicesList.size(); i++) {
            storageVolume = localDevicesList.get(i);
//            Log.d(TAG," 检测到storageVolume.getPath() "+storageVolume.getPath()+" "+Environment.getExternalStorageDirectory().getPath());
            if (!storageVolume.getPath().equals(Environment.getExternalStorageDirectory().getPath())) {
                if (storageVolume.getId().startsWith("public:179")) {
                    /* 获取SD卡设备路径列表 */
                    Log.d(TAG, " 检测到SD卡 " + storageVolume.getPath());
                } else if (storageVolume.getId().startsWith("public:8")) {
                    /* 获取USB设备路径列表 */
                    Utils.hasUsbDevice = true;
                    Utils.usbDevicesNumber += 2;
                    if (customBinding.rlUsbConnect.getVisibility() == View.GONE) {
                        customBinding.rlUsbConnect.setVisibility(View.VISIBLE);
                    }
                    Log.d(TAG, " 检测到USB设备 " + storageVolume.getPath() + " Utils.hasUsbDevice " + Utils.hasUsbDevice
                            + " Utils.usbDevicesNumber " + Utils.usbDevicesNumber);
                } else if (storageVolume.getPath().contains("sata")) {
                    /* 获取sata设备路径列表 */
                    Log.d(TAG, " 检测到sata设备 " + storageVolume.getPath());
                }
            }
        }
    }

    private void update7Icon(int front, int rear) {
        if (appInfoBeans != null && appInfoBeans.size() >= 7) {
            // 创建图标和文本视图的数组
            ImageView[] iconViews = {
                    customBinding.muqiIcon1, customBinding.muqiIcon2, customBinding.muqiIcon3,
                    customBinding.muqiIcon4, customBinding.muqiIcon5, customBinding.muqiIcon6,
                    customBinding.muqiIcon7
            };

            TextView[] textViews = {
                    customBinding.muqiText1, customBinding.muqiText2, customBinding.muqiText3,
                    customBinding.muqiText4, customBinding.muqiText5, customBinding.muqiText6,
                    customBinding.muqiText7
            };

            int size = appInfoBeans.size();
            int i = 0;

            if (front < rear) {
                // 正常情况，front 小于 rear
                for (int index = front; index <= rear; index++, i++) {
                    iconViews[i].setImageDrawable(appInfoBeans.get(index).getAppicon());
                    textViews[i].setText(appInfoBeans.get(index).getAppname());
                }
            } else {
                // 循环队列情况，front 大于 rear
                // 先处理 front 到队列末尾
                for (int index = front; index < size; index++, i++) {
                    iconViews[i].setImageDrawable(appInfoBeans.get(index).getAppicon());
                    textViews[i].setText(appInfoBeans.get(index).getAppname());
                }
                // 再处理从队列起始到 rear
                for (int index = 0; index <= rear; index++, i++) {
                    iconViews[i].setImageDrawable(appInfoBeans.get(index).getAppicon());
                    textViews[i].setText(appInfoBeans.get(index).getAppname());
                }
            }
        } else {
            // 处理条件不满足的情况
            Log.d(TAG, " App数量太少都没满足7个");
        }
    }

    private boolean getButtonSound(){
        return Settings.System.getInt(getContentResolver(),
                Settings.System.SOUND_EFFECTS_ENABLED, 0)==1;
    }

}