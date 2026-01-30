package com.example.codepal.Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

public class Network {
    public enum NetworkStatus {
        WIFI,
        MOBILE,
        NO_INTERNET
    }
    public static NetworkStatus getNetworkStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if ( cm == null ) {
            return NetworkStatus.NO_INTERNET;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = cm.getActiveNetwork();
            if ( network == null ) {
                return NetworkStatus.NO_INTERNET;
            }

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if ( capabilities == null ) {
                return NetworkStatus.NO_INTERNET;
            }

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return NetworkStatus.WIFI;
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return NetworkStatus.MOBILE;
            } else {
                return NetworkStatus.NO_INTERNET;
            }
        } else {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if ( networkInfo != null && networkInfo.isConnected() ) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return NetworkStatus.WIFI;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return NetworkStatus.MOBILE;
                }
            }
        }

        return NetworkStatus.NO_INTERNET;
    }
    public static boolean isConnected(Context context) {
        return getNetworkStatus(context) != NetworkStatus.NO_INTERNET;
    }
}
