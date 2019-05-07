package com.flyingcodes.liteble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;

/**
 * project_name:FlyingcodesBLEManager
 * package_name:com.flyingcodes.ble.scan
 * info:
 * be use for:
 * create_by:haojie
 * version: 1.0
 * create_day：2018/11/13
 */
public class LeScanner {
    private final String TAG = this.getClass().getName();
    private Context mContext;

    protected BluetoothAdapter mBluetoothAdapter;

    // android 4.3、4.4 的android BLE方案
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    // android 5.0以上android BLE方案
    private BluetoothLeScanner mBluetoothLeScanner;
    private android.bluetooth.le.ScanCallback mScanCallback;
    private boolean isScanning = false;

    private boolean useAndroidLScanner = false;// 禁止使用android L的LeScan方案进行ble发现

    private static LeScanner instance = null;

    public static LeScanner getInstance(Context mContext){
        if (null == instance)
        {
            synchronized (LeScanner.class)
            {
                if (null == instance)
                {
                    instance = new LeScanner(mContext, true);
                }
            }
        }
        return instance;
    }


    private LeScanner(Context context, boolean sAndroidLScanningDisabled) {
        //参数context不能为空
        if (null == context)
            throw new NullPointerException("The context field is required.");
        this.mContext = context;

        // Android版本低于18[android 4.3]
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            throw new UnsupportedClassVersionError("Android version less than 18.");
        }
        //获取蓝牙适配，判断是否支持蓝牙
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == mBluetoothAdapter) {
            throw new NullPointerException("Android device does not support Bluetooth.");
        }

        /* 初始化BLE scanner 方案 */
        this.useAndroidLScanner = false;

        //若版本大于21 [android 5.0]
        if (Build.VERSION.SDK_INT >= 21) {
            if (!sAndroidLScanningDisabled) {
                //使用L的Android BLE扫描方案
                useAndroidLScanner = true;
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
        }
        //初始化
        initCallback();

    }

    public void checkBluetoothState(){
        //獲取藍牙適配器，打開藍牙
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();  //打开蓝牙
            }
        }
    }

    private void initCallback() {

        //Android L BLE的callback
        if (useAndroidLScanner && Build.VERSION.SDK_INT >= 21) {
            mScanCallback = new android.bluetooth.le.ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    if (null != callback && isScanning)
                        callback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }
            };
        } else {

            //Android BLE的callback
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device == null)
                        return;
                    if (scanRecord == null)
                        return;
                    if (null != callback && isScanning)
                        callback.onLeScan(device, rssi, scanRecord);

                }
            };
        }
    }

    public void startScan() {
        if (isScanning)
            return;

        if (useAndroidLScanner && Build.VERSION.SDK_INT >= 21) {
            mBluetoothLeScanner.startScan(mScanCallback);
        } else {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

        isScanning = true;
    }

    public void stopScan() {
        if (!isScanning)
            return;
        if (useAndroidLScanner && Build.VERSION.SDK_INT >= 21) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        isScanning = false;
    }

    private LeScannerCallback callback;

    public void setmLeScanCallback(LeScannerCallback callback) {
        this.callback = callback;
    }

    public interface LeScannerCallback {

        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);
    }
}
