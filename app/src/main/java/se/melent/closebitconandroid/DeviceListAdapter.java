package se.melent.closebitconandroid;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by MelEnt on 2016-05-05.
 */
@Deprecated
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceRow>
{
    private final Context context;
    private final List<BluetoothConnectionInfo> devices;

    public DeviceListAdapter(Context context, List<BluetoothConnectionInfo> devices)
    {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public DeviceRow onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_row, parent, false);
        return new DeviceRow(view);
    }

    @Override
    public void onBindViewHolder(DeviceRow holder, int position)
    {
        BluetoothConnectionInfo device = devices.get(position);

        holder.setDevice(context, device);

        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        }

    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static final class DeviceRow extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final TextView deviceAddress;
        private final TextView deviceUUID;
        private final TextView deviceRssi;
        private Context context;
        private BluetoothConnectionInfo bcInfo;

        public DeviceRow(View view)
        {
            super(view);
            this.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            this.deviceUUID = (TextView) view.findViewById(R.id.device_uuid);
            this.deviceRssi = (TextView) view.findViewById(R.id.device_rssi);
            view.setOnClickListener(this);
        }

        public void setDevice(Context context, BluetoothConnectionInfo bcInfo)
        {
            this.context = context;
            this.bcInfo = bcInfo;
            deviceAddress.setText(bcInfo.getDevice().getAddress());
            deviceUUID.setText(StringUtils.join(bcInfo.getDevice().getUuids(), ", "));
            deviceRssi.setText(String.valueOf(bcInfo.getRssi()));
        }

        @Override
        public void onClick(View view)
        {
            Toast.makeText(context, "Item Clicked", Toast.LENGTH_SHORT).show();
        }
    }
}

