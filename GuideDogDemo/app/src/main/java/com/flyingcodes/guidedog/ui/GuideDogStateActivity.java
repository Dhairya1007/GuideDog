package com.flyingcodes.guidedog.ui;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingcodes.guidedog.R;
import com.guidedog.sandewisdom.GuideDog;


import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * project_name:GuideDogDemo
 * package_name:com.flyingcodes.guidedog.ui
 * info:
 * be use for:
 * create_by:haojie
 * version: 1.0
 * create_day：2018/11/14
 */
public class GuideDogStateActivity extends AppCompatActivity implements View.OnClickListener {
    private GuideDogStateActivity instance = this;
    private static final String TAG = "GuideDogStateActivity";
    private CircleImageView statussignal;
    private CardView statusshower;

    private static final UUID BCS_UUID_SERVICE = UUID.fromString("8E400001-F315-4F60-9FB8-838830DAEA50");
    private final String password_UUID = "8E400002-F315-4F60-9FB8-838830DAEA50";
    private static final UUID NOTIFI_CHAR_UUID = UUID.fromString("8E400003-F315-4F60-9FB8-838830DAEA50");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    
    private boolean doAfterSleep = false;//break the step after Thread sleep

    private Button mConnect;
    private TextView mDeviceId, mProximity, mAngle, mCharging, mBigBtnState, mSmallBtnState, mSlideBtnState,statuscolor;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    //guidedog licence
    private byte[] licencesCode = null;


    private static final int HANDLER_GUIDEDOG_CALLBACK_BIG_BTN_CLICK = 0x02;
    private static final int HANDLER_GUIDEDOG_CALLBACK_BIG_BTN_LONG_PRESS = 0x03;
    private static final int HANDLER_GUIDEDOG_CALLBACK_SMALL_BTN_CLICK = 0x04;
    private static final int HANDLER_GUIDEDOG_CALLBACK_SMALL_BTN_LONG_PRESS = 0x05;
    private static final int HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_NONE = 0x06;
    private static final int HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_CLEAR = 0x07;
    private static final int HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_CAUTION = 0x08;
    private static final int HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_DANGER = 0x09;
    private static final int HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_NONE = 0x0a;
    private static final int HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_ON = 0x0b;
    private static final int HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_OFF = 0x0c;
    private static final int HANDLER_GUIDEDOG_CALLBACK_ANGLE = 0x0d;
    private static final int HANDLER_GUIDEDOG_CALLBACK_CHARGEING = 0x0e;
    private static final int HANDLER_GUIDEDOG_CALLBACK_CHARGE_STOP = 0x0f;
    private static final int HANDLER_GUIDEDOG_CALLBACK_CHARGE_NONE = 0x10;


    private static final String GUIDEDOG_CALLBACK_ANGLE = "GUIDEDOG_ANGLE";

    private Handler handler = new Handler() {

        final int stateShowMilliTime = 200;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_GUIDEDOG_CALLBACK_ANGLE:
                    int angle = msg.getData().getInt(GUIDEDOG_CALLBACK_ANGLE, 0);
                    //显示角度
                    mAngle.setText(""+angle);
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_CHARGE_NONE:
                    mCharging.setText("");
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_CHARGE_STOP:
                    mCharging.setText("Charnge stop");
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_CHARGEING:
                    mCharging.setText("Charnging");
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_BIG_BTN_CLICK:
                    mBigBtnState.setText(getString(R.string.click));
                    //clear mBigBtnState Text
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(stateShowMilliTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!doAfterSleep)
                                return;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mBigBtnState.setText("");
                                }
                            });

                        }
                    }).start();
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_BIG_BTN_LONG_PRESS:
                    mBigBtnState.setText(getString(R.string.long_click));
                    //clear mBigBtnState Text
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(stateShowMilliTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (!doAfterSleep)
                                return;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mBigBtnState.setText("");
                                }
                            });

                        }
                    }).start();
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_SMALL_BTN_CLICK:
                    mSmallBtnState.setText(getString(R.string.click));
                    //clear mSmallBtnState Text
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(stateShowMilliTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (!doAfterSleep)
                                return;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSmallBtnState.setText("");
                                }
                            });

                        }
                    }).start();
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_SMALL_BTN_LONG_PRESS:
                    mSmallBtnState.setText(getString(R.string.long_click));
                    //clear mSmallBtnState Text
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Thread.sleep(stateShowMilliTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (!doAfterSleep)
                                return;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSmallBtnState.setText("");
                                }
                            });

                        }
                    }).start();
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_NONE:
                    mProximity.setText("");
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_CLEAR:
                    mProximity.setText(getString(R.string.proximity_clear));
                    mProximity.setTextColor(getResources().getColor(R.color.clear_color));
                    statuscolor.setTextColor(getResources().getColor(R.color.white));
                    statuscolor.setText("Safe");
                    statusshower.setCardBackgroundColor(getResources().getColor(R.color.clear_color));
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_CAUTION:
                    mProximity.setText(getString(R.string.proximity_caution));
                    mProximity.setTextColor(getResources().getColor(R.color.warnning_color));
                    statuscolor.setTextColor(getResources().getColor(R.color.black));
                    statuscolor.setText("Caution");
                    statusshower.setCardBackgroundColor(getResources().getColor(R.color.cautioncolor));
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_DANGER:
                    mProximity.setText(getString(R.string.proximity_danger));
                    mProximity.setTextColor(getResources().getColor(R.color.error_color));
                    statuscolor.setTextColor(getResources().getColor(R.color.white));
                    statuscolor.setText("Danger");
                    statusshower.setCardBackgroundColor(getResources().getColor(R.color.error_color));
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_ON:
                    mSlideBtnState.setText(getString(R.string.slide_on));
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_OFF:
                    mSlideBtnState.setText(getString(R.string.slide_off));
                    break;
                case HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_NONE:
                    mSlideBtnState.setText("");
                    break;
            }
        }
    };

    private BluetoothDevice mBluetoothDevice = null;
    private String deviceId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId = getIntent().getExtras().getString("deviceId", "");
        mBluetoothDevice = getIntent().getExtras().getParcelable("BluetoothDevice");
        doAfterSleep = true;

        //get licencesCode
        licencesCode = GuideDog.getInstance().getLicencesCode();


        setContentView(R.layout.activity_guidedog_state);
        statuscolor=findViewById(R.id.statuscolor);
        statusshower=findViewById(R.id.statusshower);
        mConnect = findViewById(R.id.guide_dog_connect);
        mDeviceId = findViewById(R.id.guide_dog_device_id);
        mProximity = findViewById(R.id.guide_dog_proximity_state);
        mAngle = findViewById(R.id.guide_dog_angle_state);
        mCharging = findViewById(R.id.guide_dog_charge_state);
        mBigBtnState = findViewById(R.id.guide_dog_big_btn_state);
        mSmallBtnState = findViewById(R.id.guide_dog_small_btn_state);
        mSlideBtnState = findViewById(R.id.guide_dog_slide_btn_state);
        statussignal=findViewById(R.id.statussignal);

        //show device_id
        mDeviceId.setText(deviceId);

        //connect button Listener
        mConnect.setOnClickListener(instance);


        //BluetoothManager
        mBluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Log.e(TAG, "Unable to get BluetoothManager.");
            return;
        }

        //BluetoothAdapte
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();  //open bluetooth
            }
        }

        onConnecting = true;
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TIME_OUT_MILLI);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!doAfterSleep)
                    return;
                Log.e(TAG, "Time out to connect device!");
                if (BluetoothProfile.STATE_DISCONNECTED == mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothProfile.GATT)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            Toast.makeText(instance, "Time out to connect", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
//                                mBluetoothGatt.disconnect();
                }
            }
        }).start();
        mBluetoothGatt = mBluetoothDevice.connectGatt(instance, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                Log.e(TAG, "onConnectionStateChange opstatus:" + status + ",newState=" + newState);
                if (!onConnecting && newState == BluetoothProfile.STATE_CONNECTED){
                    return;
                }

                if (status != BluetoothGatt.GATT_SUCCESS) {
                    if (BluetoothProfile.STATE_DISCONNECTED == newState) {
                        Log.e(TAG, "Exception on disaconnect：" + newState);
                        onConnected = false;
                        onConnecting = false;
                        if (status == 0x08){//BLE设备丢失连接
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(instance, "guidedog loss!", Toast.LENGTH_SHORT).show();
                                    finish();
                                    return;
                                }
                            });
                        }
                        //change view states
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Toast.makeText(instance, "DEVICE LOSS!", Toast.LENGTH_SHORT).show();
                                mConnect.setText(getString(R.string.guide_dog_connect));
                                mConnect.setEnabled(true);
                            }
                        });


                    }
                    return;
                }

                //Device connect success！
                if (BluetoothProfile.STATE_CONNECTED == newState && status == BluetoothGatt.GATT_SUCCESS) {
                    onConnected = true;
                    gatt.discoverServices();
                    //change view states
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(instance, "CONNECT SUCCESS!", Toast.LENGTH_SHORT).show();
                            dismissProgressDialog();
                            mConnect.setEnabled(true);
                            mConnect.setText(getString(R.string.guide_dog_disconnect));
                           statussignal.setImageResource(R.color.connectedcolor);

                        }
                    });

                }
                //Device disconnect success！
                else if (BluetoothProfile.STATE_DISCONNECTED == newState && status == BluetoothGatt.GATT_SUCCESS) {
                    onConnected = false;
                    onConnecting = false;
                    if (onDestroy) {
                        gatt.close();//release GATT
                    } else {
                        //change view states
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(instance, "DISCONNECT！", Toast.LENGTH_SHORT).show();
                                mConnect.setEnabled(true);
                                mConnect.setText(getString(R.string.guide_dog_connect));
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Other connection state");
                }

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "Discovered GATT failure：" + status);
                    gatt.disconnect();
                    return;
                }

                //write licencesCode
                BluetoothGattService service = gatt.getService(BCS_UUID_SERVICE);
                BluetoothGattCharacteristic mPasswordChar = service.getCharacteristic(UUID.fromString(password_UUID));
                mPasswordChar.setValue(licencesCode);
                gatt.writeCharacteristic(mPasswordChar);

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "Write GATT failure：" + status);
                    gatt.disconnect();
                    return;
                }
                firstNotification = true;
                //Notificate guidedog state
                BluetoothGattCharacteristic mRxChar = gatt.getService(BCS_UUID_SERVICE).getCharacteristic(NOTIFI_CHAR_UUID);
                gatt.setCharacteristicNotification(mRxChar, true);
                enableNotifications(mRxChar);
            }

            private boolean firstNotification = false;//firsttime to load slidebar;

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);

                //decrypt the notification byte[]
                GuideDog.getInstance().notifyDataDecrypt(characteristic.getValue());
                if (firstNotification){
                    //first load need to get slide state and
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (GuideDog.getInstance().getSlideSwitchState() == GuideDog.ButtonState.ON)
                                mSlideBtnState.setText(getString(R.string.slide_on));
                            if (GuideDog.getInstance().getSlideSwitchState() == GuideDog.ButtonState.OFF)
                                mSlideBtnState.setText(getString(R.string.slide_off));
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (GuideDog.getInstance().getChargeState() == GuideDog.ChargeState.CHARGING)
                                mCharging.setText(getString(R.string.charging));
                            if (GuideDog.getInstance().getChargeState() == GuideDog.ChargeState.STOP)
                                mCharging.setText(getString(R.string.charge_stop));
                        }
                    });

                    firstNotification = false;
                }

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                       mSlideBtnState.setText("="+GuideDog.getInstance().getSlideSwitchState());
//                    }
//                });1
//                Message msg = handler.obtainMessage();
//
//                if (GuideDog.ButtonState.ON == GuideDog.getInstance().getSlideSwitchState())
//                    msg.what = HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_ON;
//                else if (GuideDog.ButtonState.OFF == GuideDog.getInstance().getSlideSwitchState())
//                    msg.what = HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_OFF;
//                else if (GuideDog.ButtonState.OFF == GuideDog.getInstance().getSlideSwitchState())
//                    msg.what = HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_OFF;
//                handler.sendMessage(msg);
            }

            /**
             * Enables notifications on given characteristic
             * @return true is the request has been sent, false if one of the arguments was <code>null</code> or the characteristic does not have the CCCD.
             */
            protected final boolean enableNotifications(final BluetoothGattCharacteristic characteristic) {
                final BluetoothGatt gatt = mBluetoothGatt;
                if (gatt == null || characteristic == null)
                    return false;

                // Check characteristic property
                final int properties = characteristic.getProperties();
                if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
                    return false;
                gatt.setCharacteristicNotification(characteristic, true);
                final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    return gatt.writeDescriptor(descriptor);
                }
                return false;
            }
        });

        //Receive callback function. show GuideDog state.
        GuideDog.getInstance().setCallback(new GuideDog.GuideDogCallback() {

            @Override
            public void onProximity(GuideDog.ProximityState proximityState) {
                Message msg = handler.obtainMessage();
                switch (proximityState) {
                    case NONE:
                        msg.what = HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_NONE;
                        break;
                    case CLEAR:
                        msg.what = HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_CLEAR;
                        break;
                    case CAUTION:
                        msg.what = HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_CAUTION;
                        break;
                    case DANGER:
                        msg.what = HANDLER_GUIDEDOG_CALLBACK_PROXIMITY_DANGER;
                        break;
                }
                handler.sendMessage(msg);
            }

            @Override
            public void onBigButtonClick() {
                Message msg = handler.obtainMessage();
                msg.what = HANDLER_GUIDEDOG_CALLBACK_BIG_BTN_CLICK;
                handler.sendMessage(msg);
            }

            @Override
            public void onBigButtonLongPress() {
                Message msg = handler.obtainMessage();
                msg.what = HANDLER_GUIDEDOG_CALLBACK_BIG_BTN_LONG_PRESS;
                handler.sendMessage(msg);

            }

            @Override
            public void onSmallButtonClick() {
                Message msg = handler.obtainMessage();
                msg.what = HANDLER_GUIDEDOG_CALLBACK_SMALL_BTN_CLICK;
                handler.sendMessage(msg);

            }

            @Override
            public void onSmallButtonLongPress() {
                Message msg = handler.obtainMessage();
                msg.what = HANDLER_GUIDEDOG_CALLBACK_SMALL_BTN_LONG_PRESS;
                handler.sendMessage(msg);

            }

            @Override
            public void onAngle(int i) {
                Message msg = handler.obtainMessage();
                msg.what = HANDLER_GUIDEDOG_CALLBACK_ANGLE;
                msg.getData().putInt(GUIDEDOG_CALLBACK_ANGLE, i);
                handler.sendMessage(msg);
            }

            @Override
            public void onCharge(GuideDog.ChargeState chargeState) {
                Message msg = handler.obtainMessage();
                if (chargeState == GuideDog.ChargeState.CHARGING){
                    msg.what = HANDLER_GUIDEDOG_CALLBACK_CHARGEING;
                }
                else if (chargeState == GuideDog.ChargeState.STOP){
                    msg.what = HANDLER_GUIDEDOG_CALLBACK_CHARGE_STOP;

                }
                else if (chargeState == GuideDog.ChargeState.NONE){

                    msg.what = HANDLER_GUIDEDOG_CALLBACK_CHARGE_NONE;
                }
                handler.sendMessage(msg);

            }

            @Override
            public void onSlideSwitch(GuideDog.ButtonState buttonState) {
                Message msg = handler.obtainMessage();
                if (buttonState == GuideDog.ButtonState.OFF) {
                    msg.what = HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_OFF;
                } else if (buttonState == GuideDog.ButtonState.ON) {
                    msg.what = HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_ON;
                } else {
                    msg.what = HANDLER_GUIDEDOG_CALLBACK_SLIDE_BUTTON_STATE_NONE;
                    if (buttonState == GuideDog.ButtonState.NONE) {
                    } else {
                        Log.e(TAG, "Undefined Slide state");
                    }
                }
                handler.sendMessage(msg);
            }
        });
    }

    private boolean onConnected = false;//gatt on connect
    private boolean onConnecting = false;//gatt on connecting. Some device will do connect success callback when device do disconnect.

    private boolean onDestroy = false;//onDestroy time


    @Override
    protected void onDestroy() {
        super.onDestroy();
        doAfterSleep = false;
        if (onConnected) {
            onDestroy = true;
            if (null != mBluetoothGatt)
                mBluetoothGatt.disconnect();
        } else {
            if (null != mBluetoothGatt)
                mBluetoothGatt.close();
        }
    }

    private final int TIME_OUT_MILLI = 10 * 1000;

    @Override
    public void onClick(View v) {
        if (isMultiClick())
            return;
        switch (v.getId()) {
            case R.id.guide_dog_connect:
                if (null == mBluetoothGatt)
                    return;
                mConnect.setEnabled(false);
                if (onConnected) {
                    mBluetoothGatt.disconnect();
                } else {
                    onConnecting = true;
                    mBluetoothGatt.connect();
                    //connect time out
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(TIME_OUT_MILLI);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!doAfterSleep)
                                return;
                            Log.e(TAG, "Time out to connect device!");
                            if (BluetoothProfile.STATE_DISCONNECTED == mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothProfile.GATT)){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissProgressDialog();
                                        Toast.makeText(instance, "Time out to connect", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
//                                mBluetoothGatt.disconnect();
                            }
                        }
                    }).start();
                }

                break;
            default:
                break;
        }
    }

    private ProgressDialog progressDialog;
    public ProgressDialog showProgressDialog() {
        progressDialog = new ProgressDialog(instance);
        progressDialog.setMessage("connecting...");
        progressDialog.show();
        return progressDialog;
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    /* dExclude double click events */
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    public static boolean isMultiClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) < MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

}
