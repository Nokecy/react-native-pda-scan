package com.reactnativepdascan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
import com.keyence.autoid.sdk.SdkStatus;
import com.keyence.autoid.sdk.scan.DecodeResult;
import com.keyence.autoid.sdk.scan.ScanManager;
import com.keyence.autoid.sdk.scan.scanparams.ScanParams;
import com.keyence.autoid.sdk.scan.scanparams.scanParams.Collection;

import java.util.ArrayList;
import java.util.List;

@ReactModule(name = PdaScanModule.NAME)
public class PdaScanModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NAME = "PdaScan";
    public static final String barcodeName = "android.scanservice.action.UPLOAD_BARCODE_DATA";
    public static final String nlscanBarcodeName = "nlscan.action.SCANNER_RESULT";
    public static final String ldBarcodeName = "com.rfid.SCAN";
    public static final String ldStopBarcodeName = "com.rfid.STOP_SCAN";
    private ScanManager mScanManager;

    protected @Nullable
    ScanManager.DataListener mScanDataListener;

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

      //创建 ScanManager 类的实例
      mScanManager = ScanManager.createScanManager(this.getReactApplicationContext());

      mScanDataListener = new ScanManager.DataListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onDataReceived(DecodeResult decodeResult) {
          //取得读码结果
          DecodeResult.Result result = decodeResult.getResult();
          //取得已读取的代码类别
          String codeType = decodeResult.getCodeType();
          //取得已读取的数据列表
          List<String> datas = decodeResult.getDataList();
          //取得已读取的数据
          String data = decodeResult.getData();

          if (result == DecodeResult.Result.SUCCESS){
            String dataStr = String.join(" ", datas);
            Toast.makeText(mContext, dataStr, Toast.LENGTH_SHORT).show();
            WritableMap params = Arguments.createMap();
            params.putString("scanCode", dataStr);
            sendEvent(mContext, "onScanReceive", params);

            //重置参数
            setScanSize(1,0);
          }
        }
      };

      new Thread(new Runnable() {
        @Override
        public void run() {
          Looper.prepare();
          //创建读码事件接收监听者
          mScanManager.addDataListener(mScanDataListener);
          Looper.loop();
        }
      }).start();//启动线程
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

      //定义存储代码类别的变量
      ScanParams scanParams = new ScanParams();
      //取得当前的设置值
      SdkStatus status = mScanManager.getConfig(scanParams);
      if (status == SdkStatus.SUCCESS){
        scanParams.collection.method = Collection.Method.ACCUMULATE;
        scanParams.collection.codeCountAccumulate = scanSize;
        //反映设置值。
        status = mScanManager.setConfig(scanParams);
      }
    }

    @Override
    public void onHostDestroy() {
      mContext.unregisterReceiver(mHeadsetPlugReceiver);
      mContext.unregisterReceiver(nlscanReceiver);
      mContext.unregisterReceiver(ldcanReceiver);

      //舍弃 ScanManager 类的实例
      mScanManager.removeDataListener(mScanDataListener);
      //舍弃 ScanManager 类的实例，释放资源
      mScanManager.releaseScanManager();
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
