package pasonatech.danang.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.List;

import pasonatech.danang.common.GattInfo;
import pasonatech.danang.service.BluetoothLeService;
import pasonatech.danang.util.Adapter.Point3D;
import pasonatech.danang.util.Adapter.sensor.Sensor;

public class LuxometerProfile extends GenericBleProfile {
    public LuxometerProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService, BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService, device);

        List<BluetoothGattCharacteristic> charalist = bluetoothGattService.getCharacteristics();
        for (BluetoothGattCharacteristic c : charalist) {

            if (c.getUuid().toString().equals(GattInfo.UUID_OPT_DATA.toString())) {
                this.normalData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_OPT_CONF.toString())) {
                this.configData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_OPT_PERI.toString())) {
                this.periodData = c;
            }
        }
    }


    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(GattInfo.UUID_OPT_SERV.toString())) == 0) {
            return true;
        } else return false;
    }

    public void updateData(byte[] value) {
        Point3D v = Sensor.LUXOMETER.convert(value);
        if (mOnDataChangedListener != null) {
            mOnDataChangedListener.onDataChanged(String.format("%.1f", v.x) + "");
        }
    }

    public interface OnLuxometerListener {
        void onLuxometerChanged(double lux);
    }

    private OnLuxometerListener mOnLuxometerListener;

    public void setmOnLuxometerListener(OnLuxometerListener mOnLuxometerListener) {
        this.mOnLuxometerListener = mOnLuxometerListener;
    }
}
