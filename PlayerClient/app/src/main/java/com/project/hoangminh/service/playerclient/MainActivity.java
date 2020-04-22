package com.project.hoangminh.service.playerclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.project.hoangminh.service.audiocommon.MusicInterface;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private MusicInterface mInterface;
    private boolean isBound = false;
    private ArrayList<String> history = new ArrayList<String>();
    private ArrayAdapter adapter;

    private Handler uiThreadHanlder;
    private BotHandler botHandler = new BotHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiThreadHanlder = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 2) {
                    history.add("Play clip " + msg.obj.toString());
                    adapter.notifyDataSetChanged();
                } else if(msg.what == 0) {
                    Message toBot = botHandler.obtainMessage(1);
                    botHandler.sendMessage(toBot);
                }
            }
        };

        //text input for track number
        final EditText trackNo = (EditText) findViewById(R.id.editText);

        //list view display history
        final ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, history);
        listView.setAdapter(adapter);

        //Play button
        //When clicked, play a clip and update history
        final Button playBtn = (Button) findViewById(R.id.playButton);

        /////////////////
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String TAG = "HttpGetTask";
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        botHandler = new BotHandler();
                        Looper.loop();
                    }
                });
                thread.start();

                Message toUI = uiThreadHanlder.obtainMessage(0);
                uiThreadHanlder.sendMessage(toUI);
            }
        });
        /////////////////

//        playBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    if(isBound) {
//                        mInterface.play(trackNo.getText().toString());
//                        if(trackNo.getText().toString().equals("1") ||
//                                trackNo.getText().toString().equals("2") ||
//                                trackNo.getText().toString().equals("3")) {
//                            history.add("Play clip " + trackNo.getText().toString());
//                            adapter.notifyDataSetChanged();
//                        }
//                    } else {
//                        Log.i("ERROR", "Service was not bound!");
//                    }
//                } catch (RemoteException e) {
//                    Log.e("ERROR", e.toString());
//                }
//            }
//        });

        //Pause button
        //When clicked, pause the clip if it is being played
        //Update history
        final Button pauseBtn = (Button) findViewById(R.id.pauseButton);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isBound) {
                        mInterface.pause();
                        history.add("Pause playing clip " + trackNo.getText().toString());
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.i("ERROR", "Service was not bound!");
                    }
                } catch (RemoteException e) {
                    Log.e("ERROR", e.toString());
                }
            }
        });

        //Resume button
        //When clicked, resume playing if it is currently being paused
        //Update history
        final Button resumeBtn = (Button) findViewById(R.id.resumeButton);
        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isBound) {
                        mInterface.resume();
                        history.add("Resume playing clip" + trackNo.getText().toString());
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.i("ERROR", "Service was not bound!");
                    }
                } catch (RemoteException e) {
                    Log.e("ERROR", e.toString());
                }
            }
        });

        //Stop button
        //When clicked, stop the media player
        //Update history
        final Button stopBtn = (Button) findViewById(R.id.stopButton);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isBound) {
                        mInterface.stop();
                        history.add("Stop playing");
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.i("ERROR", "Service was not bound!");
                    }
                } catch (RemoteException e) {
                    Log.e("ERROR", e.toString());
                }
            }
        });
    }

    //Define a ServiceConnection and implement its onServiceConnected and onServiceDisconnected methods
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mInterface = MusicInterface.Stub.asInterface(service);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mInterface = null;
            isBound = false;
        }
    };

    //Bind to service in onResume
    @Override
    protected void onResume() {
        super.onResume();
        if(!isBound) {
            Intent intent = new Intent(MusicInterface.class.getName());
            ResolveInfo info = getPackageManager().resolveService(intent, PackageManager.GET_META_DATA);
            intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            boolean b = bindService(intent, this.mConnection, Context.BIND_AUTO_CREATE);
            if(b) {
                Log.i("INFO", "bindService() succeeded!");
            } else {
                Log.i("INFO", "bindService() failed!");
            }
        }
    }

    //Unbind from the service in onDestroy
    @Override
    protected void onDestroy() {
        if(isBound) {
            unbindService(mConnection);
            super.onDestroy();
        }
    }

    public class BotHandler extends Handler {
        public void handleMessage(Message m) {
            Message toUI = uiThreadHanlder.obtainMessage(2);
            String data = "";
            if(m.what == 1) {
                try {
                    if(isBound) {
                        data = mInterface.getData();
                        Log.i("DATA", "NOTHING");
                        //Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG);
                    } else {
                        Log.i("ERROR", "Service was not bound!");
                    }
                }catch (Exception e) {
                    Log.e("ERROR", e.toString());
                }
                toUI.obj = data;
                uiThreadHanlder.sendMessage(toUI);
            }
        }
    }
}
