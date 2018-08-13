package com.hixos.cameracontroller;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hixos.cameracontroller.communication.DeviceFinder;

import java.util.ArrayList;

public class IPSelectionDialogFragment extends DialogFragment {
    public interface IPSelectionDialogListener
    {
        void onIPSelected(String ip);
        void onIPCanceled();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (IPSelectionDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_ip, null);
        builder.setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mSelectedIP = mIPEditText.getText().toString();
                        mListener.onIPSelected(mSelectedIP);

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        IPSelectionDialogFragment.this.getDialog().cancel();
                        mListener.onIPCanceled();
                    }
                })
                .setTitle(R.string.connect);

        LinearLayout ll = v.findViewById(R.id.llayout_devicelist);
        mIPEditText = v.findViewById(R.id.edittext_ip);

        ArrayList<DeviceFinder.Device> devices = DeviceFinder.find();
        for(DeviceFinder.Device d : devices)
        {
            inflater.inflate(R.layout.list_item_textview, ll);
            TextView tview = (TextView)ll.getChildAt(ll.getChildCount() - 1);
            tview.setText(d.IPAddress);
            tview.setClickable(true);
            tview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView tv = (TextView)view;
                    mIPEditText.setText(tv.getText());
                }
            });
        }

        return builder.create();
    }

    EditText mIPEditText;
    IPSelectionDialogListener mListener;
    String mSelectedIP = "";
}
