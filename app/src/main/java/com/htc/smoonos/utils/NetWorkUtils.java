package com.htc.smoonos.utils;

/**
 * @author  作者：zgr
 * @version 创建时间：2017年7月3日 下午5:50:51
 * 类说明 判断网络是否可用
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.htc.smoonos.bean.StaticIpConfig;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;

public class NetWorkUtils {

	private String TAG = NetWorkUtils.class.getSimpleName();
	private WifiManager mWifiManager;
	private ConnectivityManager mConnectivityManager;
	//add code start

	private Context mContext;

	public NetWorkUtils(Context context, WifiManager wifiManager) {
		this.mContext = context;
		mWifiManager = wifiManager;
	}


	/**
	 * 判断是否有网络连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 判断WIFI网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 判断MOBILE网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null) {
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 获取当前网络连接的类型信息
	 * 
	 * @param context
	 * @return
	 */
	public static int getConnectedType(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				return mNetworkInfo.getType();
			}
		}
		return -1;
	}

	/**
	 * 获取当前的网络状态 ：没有网络0：WIFI网络1：3G网络2：2G网络3
	 * 
	 * @param context
	 * @return
	 */
	public static int getAPNType(Context context) {
		int netType = 0;
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = 1;// wifi
		} else if (nType == ConnectivityManager.TYPE_MOBILE) {
			int nSubType = networkInfo.getSubtype();
			TelephonyManager mTelephony = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
					&& !mTelephony.isNetworkRoaming()) {
				netType = 2;// 3G
			} else {
				netType = 3;// 2G
			}
		}
		return netType;
	}

	public String intToIp(int paramInt) {
		return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
	}

	/**
	 * @param staticIpConfig
	 * @return
	 */
	public boolean setWiFiWithStaticIP(StaticIpConfig staticIpConfig) {
		synchronized (this) {
            final long ident = Binder.clearCallingIdentity();
            boolean success = false;

            IpConfiguration ipConfig;
            WifiConfiguration wifiConfig = getWifiConfiguration(mContext, mWifiManager.getConnectionInfo().getSSID());

            if (wifiConfig != null) {
                ipConfig = wifiConfig.getIpConfiguration();

            } else {
                ipConfig = new IpConfiguration();
            }
            try {
                StaticIpConfiguration staticConfig = wifiConfig.getIpConfiguration().getStaticIpConfiguration();
                if (staticConfig == null) {
                    staticConfig = new StaticIpConfiguration();
                } else {
                    staticConfig.clear();
                }
				if (staticIpConfig.isDhcp()) {
					wifiConfig.getIpConfiguration().setIpAssignment(IpConfiguration.IpAssignment.DHCP);
                    wifiConfig.setIpConfiguration(new IpConfiguration(IpConfiguration.IpAssignment.DHCP, IpConfiguration.ProxySettings.NONE, staticConfig, null));
				} else {
					wifiConfig.getIpConfiguration().setIpAssignment(IpConfiguration.IpAssignment.STATIC);
					InetAddress inetAddress = NetworkUtils.numericToInetAddress(staticIpConfig.getIp());
                    staticConfig.ipAddress = new LinkAddress(inetAddress, 24);
                    staticConfig.gateway = (Inet4Address) NetworkUtils.numericToInetAddress(staticIpConfig.getGateWay());
                    if (!TextUtils.isEmpty(staticIpConfig.getDns1())) {
                        staticConfig.dnsServers.add(NetworkUtils.numericToInetAddress(staticIpConfig.getDns1()));
                    }
                    if (!TextUtils.isEmpty(staticIpConfig.getDns2())) {
                        staticConfig.dnsServers.add(NetworkUtils.numericToInetAddress(staticIpConfig.getDns2()));
                    }
					wifiConfig.setIpConfiguration(new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, staticConfig, null));
                    ipConfig.setStaticIpConfiguration(staticConfig);
				}
				saveConfiguration(wifiConfig);
                updateConfiguration(wifiConfig);
                disconnectWiFi();
                reconnectWiFi();
                success = true;

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
            return success;
		}
	}

	public void saveConfiguration(WifiConfiguration config) {
		mWifiManager.save(config, null);
	}

	public void updateConfiguration(WifiConfiguration config) {
		mWifiManager.updateNetwork(config);
	}

	public boolean disconnectWiFi() {
		return mWifiManager.disconnect();
	}

	public boolean reconnectWiFi() {
		return mWifiManager.reconnect();
	}

	public WifiConfiguration getWifiConfiguration(Context context, String ssid) {
		final long ident = Binder.clearCallingIdentity();
		try {
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			}
			List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();

			for (WifiConfiguration wifiConfig : list) {
				if (wifiConfig.SSID.equals(ssid)) {
					return wifiConfig;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			Binder.restoreCallingIdentity(ident);
		}
		return null;
	}

	public boolean isNetworkAvailable(ConnectivityManager connManager, Context context) {
		if (connManager == null) {
		} else {
			NetworkInfo[] info = connManager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
}