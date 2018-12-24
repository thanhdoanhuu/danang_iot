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

public class IRTTemperature extends GenericBleProfile {
    public IRTTemperature(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService, BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService, device);
        List<BluetoothGattCharacteristic> characterList = bluetoothGattService.getCharacteristics();
        for (BluetoothGattCharacteristic c : characterList) {

            if (c.getUuid().toString().equals(GattInfo.UUID_IRT_DATA.toString())) {
                this.normalData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_IRT_CONF.toString())) {
                this.configData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_IRT_PERI.toString())) {
                this.periodData = c;
            }
        }
    }


    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(GattInfo.UUID_IRT_SERV.toString())) == 0) {
            return true;
        } else return false;
    }


    public void configureService() {
        //mBluetoothLeService.writeCharacteristic(configData,(byte)0x01);
        mBluetoothLeService.setCharacteristicNotification(normalData, true);
    }

    @Override
    public void updateData(byte[] value) {
        Point3D v = Sensor.IR_TEMPERATURE.convert(value);
        if (mOnDataChangedListener != null) {
            mOnDataChangedListener.onDataChanged(String.format("%.1f°C", v.y));
        }

        if (mOnIRTemperatureListener != null) {
            mOnIRTemperatureListener.onIRTemperatureChanged(v.y);
        }
        Log.d("IRTTemperature: ", Double.toString(v.y));
    }


    public interface OnIRTemperatureListener {
        void onIRTemperatureChanged(double tem);
    }

    OnIRTemperatureListener mOnIRTemperatureListener;

}
