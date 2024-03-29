package com.reactnativepdascan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

@ReactModule(name = PdaScanModule.NAME)
public class PdaScanModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NAME = "PdaScan";
    public static final String barcodeName = "android.scanservice.action.UPLOAD_BARCODE_DATA";
    public static final String nlscanBarcodeName = "nlscan.action.SCANNER_RESULT";

    private final ReactApplicationContext mContext;

    public PdaScanModule(ReactApplicationContext reactContext) {
      super(reactContext);
      mContext = reactContext;

      //设置直接填充模式
      Intent intent = new Intent ("ACTION_BAR_SCANCFG");
      intent.putExtra("EXTRA_SCAN_MODE", 1);
      intent.putExtra("EXTRA_SCAN_AUTOENT", 1);
      mContext.sendBroadcast(intent);

      registerBroadcastReceiver();
    }

    @Override
    @NonNull
    public String getName() {
      return NAME;
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
      mContext.unregisterReceiver(mHeadsetPlugReceiver);
      mContext.unregisterReceiver(nlscanReceiver);
    }

    private void registerBroadcastReceiver() {
      mContext.registerReceiver(mHeadsetPlugReceiver, new IntentFilter(barcodeName));

      mContext.registerReceiver(nlscanReceiver, new IntentFilter(nlscanBarcodeName));
    }

    private final BroadcastReceiver mHeadsetPlugReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(barcodeName)) {
          String scanCode = intent.getStringExtra("barcode");
          Toast.makeText(mContext, scanCode, Toast.LENGTH_SHORT).show();

          WritableMap params = Arguments.createMap();
          params.putString("scanCode", scanCode);
          sendEvent(mContext, "onScanReceive", params);
        }
      }
    };

    //新大陆扫描广播
    private final BroadcastReceiver nlscanReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(nlscanBarcodeName)) {

          final String scanResult_1 = intent.getStringExtra("SCAN_BARCODE1");
          final String scanResult_2 = intent.getStringExtra("SCAN_BARCODE2");
          final int barcodeType = intent.getIntExtra("SCAN_BARCODE_TYPE", -1); // -1:unknown
          final String scanStatus = intent.getStringExtra("SCAN_STATE");
          if ("ok".equals(scanStatus)) {
            String scanCode = scanResult_1;
            Toast.makeText(mContext, scanCode, Toast.LENGTH_SHORT).show();

            WritableMap params = Arguments.createMap();
            params.putString("scanCode", scanCode);
            sendEvent(mContext, "onScanReceive", params);
          }
        }
      }
    };

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
      reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    }
}
