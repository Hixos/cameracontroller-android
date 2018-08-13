package com.hixos.cameracontroller;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hixos.cameracontroller.communication.DeviceFinder;

import java.util.ArrayList;
import java.util.logging.Logger;

public class IPListAdapter extends RecyclerView.Adapter<IPListAdapter.ViewHolder> {
    private ArrayList<DeviceFinder.Device> mDevices;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public IPListAdapter(ArrayList<DeviceFinder.Device> devices) {
        mDevices = devices;
        Log.w("GRR", String.valueOf(devices.size()));
    }

    // Create new views (invoked by the layout manager)
    @Override
    public IPListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_textview, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDevices.get(position).IPAddress);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDevices.size();
    }
}
