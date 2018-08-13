package com.hixos.cameracontroller.commands;

import android.util.Log;

import com.hixos.cameracontroller.communication.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command {
    private static final String LOGTAG = "Commands";

    public static final String CMDKEY_CMDID = "cmd_id";
    public static final String CMDKEY_NUM_EXPOSURES = "num_exposures";
    public static final String CMDKEY_EXPOSURE_TIME = "exposure_time";
    public static final String CMDKEY_DOWNLOAD = "download";

    public static final int CMD_ID_SHUTDOWN = 1;
    public static final int CMD_ID_REBOOT   = 2;

    public static final int CMD_ID_FUNCTIONSTART = 5;
    public static final int CMD_ID_FUNCTIONSTOP  = 6;

    public static final int CMD_ID_CAMERA_TEST_CONNECTION = 9;
    public static final int CMD_ID_CAMERA_RECONNECT = 10;

    public static final int CMD_ID_FUNCTION_TEST_CAPTURE = 18;
    public static final int CMD_ID_DOWNLOAD_AFTER_EXPOSURE = 19;

    public static final int CMD_ID_SEQUENCERSETUP = 20;
    public static final int CMD_ID_INTERVALOMETERSETUP = 30;

    public static Message emptyCommand(int cmdid)
    {
        JSONObject obj = new JSONObject();
        try {
            obj.put(CMDKEY_CMDID, cmdid);
        }
        catch (JSONException je)
        {
            Log.e(LOGTAG, je.getMessage());
        }

        try {
            return generateMessage(obj.toString().getBytes("UTF-8"));
        }catch (UnsupportedEncodingException uee)
        {
            Log.e(LOGTAG, uee.getMessage());
            return new Message();
        }
    }

    public static Message setupSequencerCommand(int numExposures, float expTime, boolean download)
    {
        JSONObject obj = new JSONObject();
        try {
            obj.put(CMDKEY_CMDID, CMD_ID_SEQUENCERSETUP);
            obj.put(CMDKEY_EXPOSURE_TIME, (int)(expTime*1000));
            obj.put(CMDKEY_NUM_EXPOSURES, numExposures);
            obj.put(CMDKEY_DOWNLOAD, download);
        }
        catch (JSONException je)
        {
            Log.e(LOGTAG, je.getMessage());
        }

        try {
            return generateMessage(obj.toString().getBytes("UTF-8"));
        }catch (UnsupportedEncodingException uee)
        {
            Log.e(LOGTAG, uee.getMessage());
            return new Message();
        }
    }

    public static Message downloadAfterExposureCommand(boolean download)
    {
        JSONObject obj = new JSONObject();
        try {
            obj.put(CMDKEY_CMDID, CMD_ID_DOWNLOAD_AFTER_EXPOSURE);
            obj.put(CMDKEY_DOWNLOAD, download);
        }
        catch (JSONException je)
        {
            Log.e(LOGTAG, je.getMessage());
        }

        try {
            return generateMessage(obj.toString().getBytes("UTF-8"));
        }catch (UnsupportedEncodingException uee)
        {
            Log.e(LOGTAG, uee.getMessage());
            return new Message();
        }
    }

    private static Message generateMessage(byte[] payload)
    {
        Message m = new Message();

        m.type = Message.MSGTYPE_TELECOMMAND;
        m.size = payload.length;
        m.data = payload;
        return m;
    }
}
