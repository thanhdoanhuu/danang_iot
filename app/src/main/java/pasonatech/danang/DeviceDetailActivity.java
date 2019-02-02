package pasonatech.danang;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import pasonatech.danang.controllers.BLEController;
import pasonatech.danang.profile.AmbientTemperatureProfile;
import pasonatech.danang.profile.GenericBleProfile;
import pasonatech.danang.profile.HumidityProfile;
import pasonatech.danang.profile.IRTTemperature;
import pasonatech.danang.profile.LuxometerProfile;
import pasonatech.danang.profile.MovementProfile;
import pasonatech.danang.service.BluetoothLeService;

public class DeviceDetailActivity extends AppCompatActivity {

    BLEController mainController;
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Activity mActivity;
    private BroadcastReceiver receiver;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothDevice mBluetoothDevice = null;
    MidiControl midiControl = new MidiControl();

    ArrayList<GenericBleProfile> bleProfiles = new ArrayList<GenericBleProfile>();
    List<BluetoothGattService> bleServiceList = new ArrayList<BluetoothGattService>();
    ArrayList<BluetoothGattCharacteristic> characteristicList = new ArrayList<BluetoothGattCharacteristic>();
    private static final int CHARACTERISTICS_FOUND = 1;

    private static final String CHARACTERISTICS_FOUND_RESULT = "CHARACTERISTICS_FOUND_RESULT";
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";


    private UIHandler mUIHandler = new UIHandler();
    private BluetoothGatt mBtGatt = null;
    public ProgressDialog progressDialog;
    private List<GenericBleProfile> mProfiles;
    private Boolean mIsSensorTag2 = false;
    protected static DeviceDetailActivity mThis = null;

    private String acceleration_x = "";
    private String acceleration_y = "";
    private String acceleration_z = "";
    private String luxMeter = "";
    private String humidity = "";
    private String scale = "";
    private Button scaleView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail);
//        startBLEService();
        mBluetoothLeService = BluetoothLeService.getInstance();
        Intent intent = getIntent();
        mBluetoothDevice = intent.getParcelableExtra(EXTRA_DEVICE);
        mainController = new BLEController(this);
        mProfiles = new ArrayList<GenericBleProfile>();

//        scaleView = (Button) findViewById(R.id.scalevalue);
//        scaleView.setOnClickListener(onClick(this);
        setViews();

        mIsSensorTag2 = false;
        // Determine type of SensorTagGatt
        String deviceName = mBluetoothDevice.getName();
        if ((deviceName.equals("SensorTag2")) || (deviceName.equals("CC2650 SensorTag"))) {
            mIsSensorTag2 = true;
        }

        initialLayout();
        initialReceiver();
        onViewInfalted();
        initialProgressBar();
        mThis = this;
    }

    public boolean isSensorTag2() {
        return mIsSensorTag2;
    }

    public static DeviceDetailActivity getInstance() {
        return (DeviceDetailActivity) mThis;
    }


    private void initialProgressBar() {
        progressDialog = new ProgressDialog(DeviceDetailActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Discovering Services");
        progressDialog.setMessage("");
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    private void initialLayout() {
        mActivity = this;
    }


    private void selectItem(int position) {
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        ChangeContent(position);
    }

    private void ChangeContent(int n) {

    }

    private int getResourceId(String pVariableName, String pResourcename, String pPackageName) {
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void onViewInfalted() {
        mBtGatt = BluetoothLeService.getBtGatt();

        if (mBtGatt.discoverServices()) {
            boolean succuess = true;
        } else {
            boolean succuess = false;
        }
    }


    private void initialReceiver() {

        receiver = new BroadcastReceiver() {

            List<BluetoothGattService> serviceList;
            List<BluetoothGattCharacteristic> charList = new ArrayList<BluetoothGattCharacteristic>();

            @Override
            public void onReceive(Context context, Intent intent) {

                final String action = intent.getAction();
                final int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);

                if (intent.getAction().equals(BluetoothLeService.ACTION_DATA_NOTIFY)) {
                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);

                    for (int i = 0; i < characteristicList.size(); i++) {
                        BluetoothGattCharacteristic bleCharacteristic = characteristicList.get(i);
                        if (bleCharacteristic.getUuid().toString().equals(uuidStr)) {
                            for (int j = 0; j < mProfiles.size(); j++) {
                                if (mProfiles.get(j).checkNormalData(uuidStr)) {
                                    mProfiles.get(j).updateData(value);
                                }
                            }
                        }
                    }
                } else if (intent.getAction().equals(BluetoothLeService.ACTION_DATA_READ)) {
                    // Data read
                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                    for (int ii = 0; ii < charList.size(); ii++) {
                        BluetoothGattCharacteristic tempC = charList.get(ii);
                        if ((tempC.getUuid().toString().equals(uuidStr))) {
                            for (int jj = 0; jj < mProfiles.size(); jj++) {
                                GenericBleProfile p = mProfiles.get(jj);
                                p.didReadValueForCharacteristic(tempC);
                            }
                            //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                            break;
                        }
                    }
                } else if (intent.getAction().equals(BluetoothLeService.ACTION_DATA_WRITE)) {

                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                    for (int ii = 0; ii < charList.size(); ii++) {
                        BluetoothGattCharacteristic tempC = charList.get(ii);
                        if ((tempC.getUuid().toString().equals(uuidStr))) {
                            for (int jj = 0; jj < mProfiles.size(); jj++) {
                                GenericBleProfile p = mProfiles.get(jj);
                                p.didWriteValueForCharacteristic(tempC);
                            }
                            //Log.d("DeviceActivity","Got Characteristic : " + tempC.getUuid().toString());
                            break;
                        }
                    }

                } else if (intent.getAction().equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        bleServiceList = mBluetoothLeService.getBLEService();
                        if (bleServiceList.size() > 0) {
                            for (int i = 0; i < bleServiceList.size(); i++) {
                                List<BluetoothGattCharacteristic> characteristics = bleServiceList.get(i).getCharacteristics();
                                if (characteristics.size() > 0) {
                                    for (int j = 0; j < characteristics.size(); j++) {
                                        characteristicList.add(characteristics.get(j));
                                    }
                                }
                            }
                        }

                        serviceList = mBluetoothLeService.getSupportedGattServices();
                        if (serviceList.size() > 0) {
                            for (int ii = 0; ii < serviceList.size(); ii++) {
                                BluetoothGattService s = serviceList.get(ii);
                                List<BluetoothGattCharacteristic> c = s.getCharacteristics();
                                if (c.size() > 0) {
                                    for (int jj = 0; jj < c.size(); jj++) {
                                        charList.add(c.get(jj));
                                    }
                                }
                            }
                        }

                        Thread work = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                //Iterate through the services and add GenericBluetoothServices for each service
                                int nrNotificationsOn = 0;
                                int maxNotifications;
                                int servicesDiscovered = 0;
                                int totalCharacteristics = 0;
                                //serviceList = mBtLeService.getSupportedGattServices();
                                for (BluetoothGattService s : serviceList) {
                                    List<BluetoothGattCharacteristic> chars = s.getCharacteristics();
                                    totalCharacteristics += chars.size();
                                }

                                //Special
                                if (totalCharacteristics == 0) {
                                    //Something bad happened, we have a problem
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast toast = Toast.makeText(getApplicationContext(), "Service discovered but not characteristics has been found", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    });
                                    return;
                                }

                                final int final_totalCharacteristics = totalCharacteristics;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(getApplicationContext(), "Found a total of " + serviceList.size() + " services with a total of " + final_totalCharacteristics + " characteristics on this device", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });

                                if (Build.VERSION.SDK_INT > 18) maxNotifications = 7;
                                else {
                                    maxNotifications = 4;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Android version 4.3 detected, max 4 notifications enabled", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                for (int ii = 0; ii < serviceList.size(); ii++) {
                                    BluetoothGattService s = serviceList.get(ii);
                                    List<BluetoothGattCharacteristic> chars = s.getCharacteristics();
                                    if (chars.size() == 0) {
                                        Log.d("DeviceActivity", "No characteristics found for this service !!!");
                                    }
                                    servicesDiscovered++;
                                    final float serviceDiscoveredcalc = (float) servicesDiscovered;
                                    final float serviceTotalcalc = (float) serviceList.size();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.setProgress((int) ((serviceDiscoveredcalc / (serviceTotalcalc - 1)) * 100));
                                        }
                                    });
                                    Log.d("DeviceActivity", "Configuring service with uuid : " + s.getUuid().toString());

                                    if (LuxometerProfile.isCorrectService(s)) {
                                        LuxometerProfile lux = new LuxometerProfile(mBluetoothLeService, s, mBluetoothDevice);
                                        mProfiles.add(lux);
                                        if (nrNotificationsOn < maxNotifications) {
                                            lux.configureService();
                                            nrNotificationsOn++;
                                        }
                                        lux.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
                                            @Override
                                            public void onDataChanged(String data) {
                                                luxMeter = data;
                                                updateScreen();
                                            }
                                        });
                                    }

                                    if (HumidityProfile.isCorrectService(s)) {
                                        HumidityProfile hum = new HumidityProfile(mBluetoothLeService, s, mBluetoothDevice);

                                        hum.setmOnHumidityListener(new HumidityProfile.OnHumidityListener() {
                                            @Override
                                            public void onHumidityChanged(double data) {
                                                humidity = String.format("%.2f", data);
                                                updateScreen();
                                            }
                                        });

                                        mProfiles.add(hum);
                                        if (nrNotificationsOn < maxNotifications) {
                                            hum.configureService();
                                            nrNotificationsOn++;
                                        }
                                        Log.d("DeviceActivity", "Found Humidity !");
                                    }


                                    if (MovementProfile.isCorrectService(s)) {
                                        MovementProfile mov = new MovementProfile(mBluetoothLeService, s, mBluetoothDevice);

                                        mov.setOnMovementListener(new MovementProfile.OnMovementListener() {
                                            @Override
                                            public void onMovementACCChanged(double x, double y, double z) {
                                                acceleration_x = String.format("%.2f", x);
                                                acceleration_y = String.format("%.2f", y);
                                                acceleration_z = String.format("%.2f", z);
                                                updateScreen();
                                            }

                                            @Override
                                            public void onMovementGYROChanged(double x, double y, double z) {
                                            }

                                            @Override
                                            public void onMovementMAGChanged(double x, double y, double z) {
                                            }
                                        });

                                        mProfiles.add(mov);
                                        if (nrNotificationsOn < maxNotifications) {
                                            mov.configureService();
                                            nrNotificationsOn++;
                                        }
                                        Log.d("DeviceActivity", "Found Motion !");
                                    }
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.setTitle("Enabling Services");
                                        progressDialog.setMax(mProfiles.size());
                                        progressDialog.setProgress(0);
                                    }
                                });

                                for (final GenericBleProfile p : mProfiles) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            p.enableService();
                                            progressDialog.setProgress(progressDialog.getProgress() + 1);
                                        }
                                    });
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.hide();
                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        });
                        work.start();

                        Message msg = new Message();
                        msg.what = CHARACTERISTICS_FOUND;
                        Bundle bundle = new Bundle();
                        bundle.putInt(CHARACTERISTICS_FOUND_RESULT, characteristicList.size());
                        msg.setData(bundle);
                        mUIHandler.sendMessage(msg);


                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "not success get services", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                } else if (intent.getAction().equals(BluetoothLeService.ACTION_DATA_NOTIFY)) {
                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                    for (int i = 0; i < characteristicList.size(); i++) {
                        BluetoothGattCharacteristic bleCharacteristic = characteristicList.get(i);
                        if (bleCharacteristic.getUuid().toString().equals(uuidStr)) {
                            for (int j = 0; j < bleProfiles.size(); j++) {
                                if (bleProfiles.get(j).checkNormalData(uuidStr)) {
                                    bleProfiles.get(j).updateData(value);
                                }
                            }
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.FIND_NEW_BLE_DEVICE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
        registerReceiver(receiver, intentFilter);
    }

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CHARACTERISTICS_FOUND:
                    int res = msg.getData().getInt(CHARACTERISTICS_FOUND_RESULT);
                    showToast(res + "");
                    break;
            }
        }
    }

    private void showToast(String str) {
        Toast toast = Toast.makeText(mActivity, str, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     *
     */
    private void updateScreen() {

        //TODO こっちで実装してください。

        //加速度X
        ((TextView) mActivity.findViewById(R.id.movementValue1X)).setText(acceleration_x);

        //加速度Y
        ((TextView) mActivity.findViewById(R.id.movementValue1Y)).setText(acceleration_y);

        //加速度Z
        ((TextView) mActivity.findViewById(R.id.movementValue1Z)).setText(acceleration_z);

        //湿度
        ((TextView) mActivity.findViewById(R.id.humidityValue)).setText(humidity);

        //周囲の光
        ((TextView) mActivity.findViewById(R.id.luxometerValue)).setText(luxMeter);

        //加速度 Xをランク分け
        int lv_x = 0;
        int lv_y = 0;
        int lv_z = 0;
        int lv_h = 0;
        int lv_l = 0;

        if (acceleration_x != "") {

            float x = Float.valueOf(acceleration_x);
            lv_x = 1;
//            if (x < 0) {
//                lv_x = 1;
//            } else if (x < 5) {
//                lv_x = 2;
//            } else lv_x = 3;
        }

        //加速度 yをランク分け
        if (acceleration_y != "") {

            float y = Float.valueOf(acceleration_y);
            lv_y = 1;
//            if (y < 0) {
//                lv_y = 1;
//            } else if (y < 5) {
//                lv_y = 2;
//            } else lv_y = 3;
        }

        //加速度 Zをランク分け
        if (acceleration_z != "") {

            float z = Float.valueOf(acceleration_z);
            lv_z = 1;
//            if (z < 0) {
//                lv_z = 1;
//            } else if (z < 5) {
//                lv_z = 2;
//            } else lv_z = 3;
        }

        //湿度をランク分け
        if (humidity != "") {

            float h = Float.valueOf(humidity);
            lv_h = 1;
//            if (h < 30) {
//                lv_h = 1;
//            } else if (h < 60) {
//                lv_h = 2;
//            } else lv_h = 3;
        }

        //光の強さをランク分け
        if (luxMeter != "") {

            float l = Float.valueOf(luxMeter);
            if (l < 100) {
                lv_l = 1;
            } else if (l < 400) {
                lv_l = 2;
            } else lv_l = 3;
        }

        if ((lv_x == 1) && (lv_y == 3) && (lv_z == 2) && (lv_h == 2) && (lv_l == 3)) {
            scale = "A#";

        } else if ((lv_x == 2) && (lv_y == 2) && (lv_z == 1) && (lv_h == 3) && (lv_l == 2)) {
            scale = "c#";

        } else if ((lv_x == 3) && (lv_y == 1) && (lv_z == 3) && (lv_h == 1) && (lv_l == 3)) {
            scale = "b#";

        } else if ((lv_x == 1) && (lv_y == 1) && (lv_z == 1) && (lv_h == 1) && (lv_l == 1)) {
            scale = "b";

        } else scale = "c";
        ((TextView) mActivity.findViewById(R.id.scalevalue)).setText(scale);
    }

    private void setViews(){
        Button button=(Button)findViewById(R.id.button_scale);
        button.setOnClickListener(onClick_button);
    }

//    public void onClick(View v) {
    private View.OnClickListener onClick_button=new View.OnClickListener() {
    @Override

        public void onClick(View v) {
            ((TextView) mActivity.findViewById(R.id.scalevalue)).setText(scale);
        }
};
}
//            MidiControl.MIDImode midimode = MidiControl.MIDImode.Mode1;
//            PlayMusic.Key key = PlayMusic.Key.A;
//            int instrumentNo = 1;
//
//            Instrument instrument = new Instrument(midiControl,instrumentNo,midimode);
//            PlayMusic playMusic = new PlayMusic(instrument, key, 3,1);
//            playMusic.Play();
