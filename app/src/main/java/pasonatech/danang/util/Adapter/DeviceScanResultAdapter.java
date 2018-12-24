package pasonatech.danang.util.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pasonatech.danang.R;
import pasonatech.danang.model.BLEDeviceDAO;

public class DeviceScanResultAdapter extends BaseAdapter {

    ArrayList<BLEDeviceDAO> mBLEDeviceList = new ArrayList<BLEDeviceDAO>();
    LayoutInflater layoutInflater = null;
    Context mContext;

    public DeviceScanResultAdapter(Context _context) {
        this.mContext = _context;
        this.layoutInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setmBLEDeviceList(ArrayList<BLEDeviceDAO> mBLEDeviceList) {
        this.mBLEDeviceList = mBLEDeviceList;
    }

    @Override
    public int getCount() {
        return mBLEDeviceList.size();
    }

    @Override
    public Object getItem(int i) {
        return mBLEDeviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = layoutInflater.inflate(R.layout.ble_device_scan_list,viewGroup,false);
        TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
        TextView deviceAddress = view.findViewById(R.id.deviceAddrress);
        deviceAddress.setText(mBLEDeviceList.get(i).getDeviceAddress());
        deviceName.setText(mBLEDeviceList.get(i).getDeviceName());

        return view;
    }
}
