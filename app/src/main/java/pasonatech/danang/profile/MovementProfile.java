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

public class MovementProfile extends GenericBleProfile {
    public MovementProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService, BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService, device);


        List<BluetoothGattCharacteristic> charList = bluetoothGattService.getCharacteristics();
        for (BluetoothGattCharacteristic c : charList) {

            if (c.getUuid().toString().equals(GattInfo.UUID_MOV_DATA.toString())) {
                this.normalData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_MOV_CONF.toString())) {
                this.configData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_MOV_PERI.toString())) {
                this.periodData = c;
            }
        }

    }


    public static boolean isCorrectService(BluetoothGattService service) {
        return (service.getUuid().toString().compareTo(GattInfo.UUID_MOV_SERV.toString())) == 0;
    }


    @Override
    public void enableService() {
        byte b[] = new byte[]{0x7F, 0x00};

        int error = mBluetoothLeService.writeCharacteristic(this.configData, b);
        if (error != 0) {
            if (this.configData != null)
                Log.d("SenTagMovementProfile", "Sensor config failed: " + this.configData.getUuid().toString() + " Error: " + error);
        }

        this.isEnabled = true;
    }

    public void updateData(byte[] value) {
        Point3D vAcc;
        Point3D vGyro;
        Point3D vMag;
        String resGYRO = "";

        vAcc = Sensor.MOVEMENT_ACC.convert(value);

        vGyro = Sensor.MOVEMENT_GYRO.convert(value);
        resGYRO += vGyro.x + "-" + vGyro.y + "-" + vGyro.z + "\n";

        vMag = Sensor.MOVEMENT_MAG.convert(value);

        if (mOnDataChangedListener != null) {
            mOnDataChangedListener.onDataChanged(resGYRO);
        }

        if (mOnMovementListener != null) {
            mOnMovementListener.onMovementACCChanged(vAcc.x, vAcc.y, vAcc.z);
            mOnMovementListener.onMovementGYROChanged(vGyro.x, vGyro.y, vGyro.z);
            mOnMovementListener.onMovementMAGChanged(vMag.x, vMag.y, vMag.z);
        }

    }

    public interface OnMovementListener {
        void onMovementACCChanged(double x, double y, double z);

        void onMovementGYROChanged(double x, double y, double z);

        void onMovementMAGChanged(double x, double y, double z);
    }

    private OnMovementListener mOnMovementListener;

    public void setOnMovementListener(OnMovementListener mOnMovementListener) {
        this.mOnMovementListener = mOnMovementListener;
    }
}
