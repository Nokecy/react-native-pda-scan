package com.reactnativepdascan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.List;

@ReactModule(name = PdaScanModule.NAME)
public class PdaScanModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NAME = "PdaScan";
    public static final String barcodeName = "android.scanservice.action.UPLOAD_BARCODE_DATA";
    public static final String nlscanBarcodeName = "nlscan.action.SCANNER_RESULT";
    public static final String ldBarcodeName = "com.rfid.SCAN";
    public static final String ldStopBarcodeName = "com.rfid.STOP_SCAN";

    //扫描数量
    public int m_scanSize = 1;
    public int m_scanLen = 0;
    public List<String> scanData = new ArrayList<String>();
    private final ReactApplicationContext mContext;

    public PdaScanModule(ReactApplicationContext reactContext) {
      super(reactContext);
      mContext = reactContext;

      //新大陆广播模式 broadcast
      Intent intent = new Intent ("ACTION_BAR_SCANCFG");
      intent.putExtra("EXTRA_SCAN_MODE", 3);
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

    @ReactMethod
    public void setScanSize(int scanSize,int scanLen) {
      m_scanSize = scanSize;
      m_scanLen = scanLen;
    }

    @Override
    public void onHostDestroy() {
      mContext.unregisterReceiver(mHeadsetPlugReceiver);
      mContext.unregisterReceiver(nlscanReceiver);
      mContext.unregisterReceiver(ldcanReceiver);
    }

    private void registerBroadcastReceiver() {
      mContext.registerReceiver(mHeadsetPlugReceiver, new IntentFilter(barcodeName));

      mContext.registerReceiver(nlscanReceiver, new IntentFilter(nlscanBarcodeName));

      mContext.registerReceiver(ldcanReceiver, new IntentFilter(ldBarcodeName));

      mContext.registerReceiver(ldcanReceiver, new IntentFilter(ldStopBarcodeName));
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

     //兰盾扫描广播
    private final BroadcastReceiver ldcanReceiver = new BroadcastReceiver() {
      @RequiresApi(api = Build.VERSION_CODES.O)
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ldBarcodeName)) {
          final String scanResult_1 = intent.getStringExtra("scannerdata");
          String scanCode = scanResult_1;

          if (m_scanLen != 0 && m_scanLen != scanCode.length()){
            return;
          }

          if (!scanData.contains(scanCode)){
            scanData.add(scanCode);
          }
          if (scanData.size() == m_scanSize) {
            Intent broadIntent = new Intent();
            broadIntent.setAction(ldStopBarcodeName);
            context.sendBroadcast(broadIntent);
          }
        }

        if (intent.getAction().equals(ldStopBarcodeName)) {
          String data = String.join(" ", scanData);
          Toast.makeText(mContext, data, Toast.LENGTH_SHORT).show();

          WritableMap params = Arguments.createMap();
          params.putString("scanCode", data);
          sendEvent(mContext, "onScanReceive", params);

          //重置参数
          scanData.clear();
          m_scanSize = 1;
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
