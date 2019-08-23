package com.hixos.cameracontroller;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hixos.cameracontroller.commands.Command;
import com.hixos.cameracontroller.communication.ConnectionListener;
import com.hixos.cameracontroller.communication.MessageService;
import com.hixos.cameracontroller.communication.TCPClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import br.com.simplepass.loading_button_lib.interfaces.OnAnimationEndListener;

import static android.transition.Fade.IN;
import static android.transition.Fade.OUT;

public class MainActivity extends AppCompatActivity implements IPSelectionDialogFragment.IPSelectionDialogListener, ConnectionListener{
    private static final String LOGTAG = "MainActivity";

    private MessageService mService;
    private boolean mBound = false;

    private static final int PORT = 8888;

    private TextView mTextViewIPAddr;
    private CircularProgressButton mButtonConnect;

    Handler mHandler = new Handler();

   // private ViewGroup mRoot;
    private View mPagerRootView;
    private ViewGroup mRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(LOGTAG, "main onCreate");

        SharedPreferences pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(TCPClient.PREF_KEY_PORT, PORT);
        editor.apply();

        TabLayout tabLayout = findViewById(R.id.tablayout);
        TabItem tabSequencer = findViewById(R.id.tab_sequencer);
        TabItem tabIntervalometer = findViewById(R.id.tab_intervalometer);

        ViewPager viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(viewPager);

        PageAdapter pageAdapter = new PageAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pageAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        mPagerRootView = findViewById(R.id.pager_root);
        mPagerRootView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        mPagerRootView.setAlpha(0);

        mRoot = (ViewGroup)findViewById(R.id.fragment_root);

        mButtonConnect = findViewById(R.id.button_connect);
        mTextViewIPAddr = findViewById(R.id.edittext_ipaddr);
        mTextViewIPAddr.setText(pref.getString(TCPClient.PREF_KEY_IP_ADDR, ""));
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBound)
                {
                    Log.i(LOGTAG, "onClick");
                    if(!mService.isConnected()) {

                        SharedPreferences pref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(TCPClient.PREF_KEY_IP_ADDR, mTextViewIPAddr.getText().toString());
                        editor.commit();

                        if (mService.connect()) {
                            mButtonConnect.startAnimation();
                        }
                    }else{
                        mService.disconnect();
                        mButtonConnect.startAnimation();
                    }
                }
            }
        });

        mTextViewIPAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IPSelectionDialogFragment dialogFragment = new IPSelectionDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), "connectdialogfragment");
            }
        });

        /*Button btn = (Button)findViewById(R.id.button5);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(LOGTAG, "Button clicked");

               /* IPSelectionDialogFragment dialogFragment = new IPSelectionDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), "connectdialogfragment");*/
                /*Intent intent = new Intent(MainActivity.this, SequentiometerActivity.class);

                startActivity(intent);
                Message m = Command.setupSequencer(1,2, false);
                if(mBound)
                {
                    mService.send(m);
                }
            }
        });*/

    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.w(LOGTAG, "main onStart");
        Intent i = new Intent(this, MessageService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);

        mBound = false;
        mService = null;
        Log.w(LOGTAG, "omain nStop");
    }

    @Override
    protected void onDestroy() {

        Log.w(LOGTAG, "main onDestroy1");


        mButtonConnect.dispose();
        Log.w(LOGTAG, "main onDestroy2");
        super.onDestroy();
        Log.w(LOGTAG, "main onDestroy3");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.shutdown: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.shutdown_confirmation);
                builder.setTitle(R.string.shutdown);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mService.send(Command.emptyCommand(Command.CMD_ID_SHUTDOWN));
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create().show();

                return true;
            }
            case R.id.reboot: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.reboot_confirmation);
                builder.setTitle(R.string.reboot);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mService.send(Command.emptyCommand(Command.CMD_ID_REBOOT));
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create().show();
                return true;
            }
            case R.id.reconnect:
                mService.send(Command.emptyCommand(Command.CMD_ID_CAMERA_RECONNECT));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(LOGTAG, "main onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(LOGTAG, "main onResume");
    }

    @Override
    public void onIPSelected(String ip) {
        mTextViewIPAddr.setText(ip);
    }

    @Override
    public void onIPCanceled() {

    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(LOGTAG, "Main onServiceConnected");

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MessageService.MessageBinder binder = (MessageService.MessageBinder) service;
            mService = binder.getService();
            mBound = true;

            mService.registerOnConnectionListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOGTAG, "main OnServiceDisconnected");
            mBound = false;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.w(LOGTAG, "DEAD");
        }
    };

    @Override
    public void onConnectionAttempt(int attempt_num) {

    }

    @Override
    public void onConnect() {
        Log.w(LOGTAG, "main OnConnect");

        connectButtonSuccess(R.string.disconnect);

        TransitionSet set = new TransitionSet();

        Fade f = new Fade(IN);
        f.setDuration(200);

        ChangeBounds b = new ChangeBounds();
        b.setDuration(300);
        b.setInterpolator(new OvershootInterpolator(1f));

        set.addTransition(f);
        set.addTransition(b);

        TransitionManager.beginDelayedTransition(mRoot, set);

        mPagerRootView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mPagerRootView.setAlpha(1);

        b.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                Log.d(LOGTAG, "invalidate");
                mPagerRootView.invalidate();

            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    @Override
    public void onConnectionFailed() {
        connectButtonFailed();
    }

    @Override
    public void onDisconnect() {
        Log.w(LOGTAG, "Main onDisconnecting");
        connectButtonSuccess(R.string.connect);

        TransitionSet set = new TransitionSet();

        Fade f = new Fade(OUT);
        f.setDuration(200);

        ChangeBounds b = new ChangeBounds();
        b.setDuration(300);
        b.setInterpolator(new AnticipateInterpolator(1f));

        set.addTransition(f);
        set.addTransition(b);

        TransitionManager.beginDelayedTransition(mRoot, set);

        mPagerRootView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        mPagerRootView.setAlpha(0);
    }

    @Override
    public void onDataReceived(byte[] data) {

    }

    private void connectButtonSuccess(int newText)
    {
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_buttton_success);
        int color = getResources().getColor(R.color.colorGreen);
        if(mButtonConnect.isAnimating()) {
            mButtonConnect.doneLoadingAnimation(color, icon);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mButtonConnect.revertAnimation(new OnAnimationEndListener() {
                        @Override
                        public void onAnimationEnd() {
                            mButtonConnect.setText(newText);
                        }
                    });
                }
            }, 2000);
        }else {
            mButtonConnect.setText(newText);
        }
    }

    private void connectButtonFailed()
    {
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_buttton_failure);
        int color = getResources().getColor(R.color.colorRed);

        mButtonConnect.doneLoadingAnimation(color, icon);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mButtonConnect.revertAnimation(new OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        mButtonConnect.setText(R.string.connect);
                    }
                });
            }
        }, 2000);
    }
}
