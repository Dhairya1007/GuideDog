package com.flyingcodes.guidedog.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.flyingcodes.guidedog.R;
import com.guidedog.sandewisdom.GuideDog;
import com.flyingcodes.liteble.LeScanner;

/**
 * project_name:GuideDogDemo
 * package_name:com.flyingcodes.guidedog.ui
 * info:
 * be use for:
 * create_by:haojie
 * version: 1.0
 * create_day：2018/11/14
 */
public class GuideDogSearchActivity extends AppCompatActivity {
    private GuideDogSearchActivity instance = this;
    private final String TAG = instance.getClass().getName();
    //    private TextView textview;
    private Button mSearchDevice;
    private LeScanner mLeScanner;

    private String device_id = "e371197804e5";//device_id

    private boolean hasMatch = false;//Match success;
    private final int TIME_OUT_MILLI = 10 * 1000;//Match time out

    private boolean doAfterSleep = false;//break the step after Thread sleep

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidedog_search);
//        textview = findViewById(R.id.content);
        mSearchDevice = findViewById(R.id.start_search_device);

        //set DeviceId
        GuideDog.getInstance().setDeviceId(device_id);


        //begin to BLE  discover
        mLeScanner = LeScanner.getInstance(instance);
        mLeScanner.setmLeScanCallback(new LeScanner.LeScannerCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                //match Device
                boolean isMatch = GuideDog.getInstance().verifyScanRecordFormat(scanRecord);
                if (isMatch) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //has match device
                            if (hasMatch)
                                return;
                            hasMatch = true;

                            //stop BLE Scanning
                            if (null != mLeScanner)
                                mLeScanner.stopScan();

                            //open GuideDogState
                            mSearchDevice.setText(getString(R.string.guidedog_connecting));
                            Intent intent = new Intent(instance, GuideDogStateActivity.class);
                            intent.putExtra("deviceId", device_id);
                            intent.putExtra("BluetoothDevice", device);
                            startActivity(intent);

                        }
                    });
                }
            }
        });
        mSearchDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchDevice.setEnabled(false);
                mSearchDevice.setText(getString(R.string.guidedog_searching));
                mLeScanner.startScan();
                //search time out
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(TIME_OUT_MILLI);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (hasMatch || doAfterSleep)
                            return;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "Search device timeout!");

                                Toast.makeText(instance, "SEARCH TIMEOUT!", Toast.LENGTH_SHORT).show();
                                if (null != mLeScanner)
                                    mLeScanner.stopScan();
                                mSearchDevice.setEnabled(true);
                                mSearchDevice.setText(getString(R.string.guidedog_search));
                            }
                        });
                    }
                }).start();
            }
        });

        //Open Bluetooth Device
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();  //Open Bluetooth Device
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        hasMatch = false;
        mSearchDevice.setEnabled(true);
        mSearchDevice.setText(getString(R.string.guidedog_search));
        doAfterSleep = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        doAfterSleep = false;
    }
}
