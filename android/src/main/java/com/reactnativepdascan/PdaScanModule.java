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
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

@ReactModule(name = PdaScanModule.NAME)
public class PdaScanModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NAME = "PdaScan";
    public static final String barcodeName = "android.scanservice.action.UPLOAD_BARCODE_DATA";
    private ReactApplicationContext mContext;

    public PdaScanModule(ReactApplicationContext reactContext) {
      super(reactContext);
      mContext = reactContext;
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
    }

    private void registerBroadcastReceiver() {
      IntentFilter filter = new IntentFilter();
      filter.addAction(barcodeName);
      mContext.registerReceiver(mHeadsetPlugReceiver, filter);
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

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
      reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    }
}
