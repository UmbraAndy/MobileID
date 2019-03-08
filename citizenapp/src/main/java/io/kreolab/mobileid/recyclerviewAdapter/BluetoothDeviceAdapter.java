/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.recyclerviewAdapter;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.kreolab.mobileid.R;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder>  {


    private final List<BluetoothDevice> mDevices;
    private final ItemClickedListener mItemClickedListener;

    public BluetoothDeviceAdapter(List<BluetoothDevice> devices, ItemClickedListener itemClickedListener) {
        mItemClickedListener = itemClickedListener;
        this.mDevices = devices;
    }


    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_item, parent, false);

        return new DeviceViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bindData(mDevices.get(position));
    }




    public interface ItemClickedListener{
        void onItemClicked(BluetoothDevice device);
    }
    public class DeviceViewHolder extends ViewHolder implements View.OnClickListener {

        @BindView(R.id.bluetoothDevice)
        TextView deviceView;

        DeviceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        @Override
        public void onClick(View v) {
            int itemSelected = getAdapterPosition();
            mItemClickedListener.onItemClicked(mDevices.get(itemSelected));
        }

        void bindData(BluetoothDevice bluetoothDevice) {
            deviceView.setText(bluetoothDevice.getName());
            int drawable = 0;
            switch (bluetoothDevice.getBluetoothClass().getMajorDeviceClass()) {
                case BluetoothClass.Device.Major.PHONE:
                    drawable = R.drawable.ic_smartphone;
                    break;
                case BluetoothClass.Device.Major.COMPUTER:
                    drawable = R.drawable.ic_computer;
                    break;
            }
            deviceView.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
            deviceView.setOnClickListener(this);
        }



    }
}
