package com.hixos.cameracontroller.communication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads /proc/net/arp to find connected devices to the wifi hotspot
 */
public class DeviceFinder {
    private static final String LOGTAG = "DeviceFinder";
    public static class Device
    {
        public String IPAddress;
        public String Name = "";
    }
    private DeviceFinder() {
    }

    public static ArrayList<Device> find()
    {
        ArrayList<Device> out = new ArrayList<>();
        Pattern p = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
        FileReader f;
        try {
            f = new FileReader("/proc/net/arp");
        }
        catch (Exception e)
        {
            Log.e(LOGTAG, e.getMessage());
            return null;
        }
        BufferedReader reader = new BufferedReader(f);
        String line;
        try {
            int i = 1;
            while ((line = reader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    MatchResult mr = m.toMatchResult();

                    Device d = new Device();
                    d.IPAddress = mr.group(1);

                    out.add(d);
                }else
                {
                    Log.e(LOGTAG, "No match for line "  + i++ + "  " );
                }
            }
        }catch (Exception e)
        {
            Log.e(LOGTAG, e.getMessage());
        }finally {
            try{
                reader.close();
                f.close();
            }catch (IOException e)
            {
                Log.e(LOGTAG, "Error closing readers: " + e.getMessage());
            }
        }

        /*for(Device d : out)
        {
            try {
                InetAddress address = InetAddress.getByName(d.IPAddress);
                Log.w(LOGTAG, " -- " + d.IPAddress);
                if(address.isReachable(1000))
                {
                    d.Name = address.getCanonicalHostName();
                    if(d.Name.equals(d.IPAddress))
                    {
                        d.Name = "NOPE";
                    }
                }else
                {
                    d.Name = "UNREACHABLE";
                }
                Log.w(LOGTAG, d.Name);
            }catch (Exception e)
            {
                Log.e(LOGTAG, "Error getting hostname: " + e.getMessage());
            }
        }*/
        return out;
    }
}


