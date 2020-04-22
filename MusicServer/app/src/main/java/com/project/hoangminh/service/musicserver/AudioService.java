package com.project.hoangminh.service.musicserver;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.project.hoangminh.service.audiocommon.MusicInterface;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

//Audio Service class

public class AudioService extends Service {
    public static MediaPlayer mPlayer;
    public static boolean isPaused = false;

    private final MusicInterface.Stub mBinder = new MusicInterface.Stub() {

        @Override
        public String getData() {
            String data = "";
            String QUERY = "";
            String URL;
            String TAG = "HttpGetTask";
            HttpURLConnection connection = null;
            try {
                QUERY = URLEncoder.encode("SELECT date FROM t2 WHERE transaction_type = 'withdrawal' ORDER BY date DESC LIMIT 10", "utf-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "UnsupportedEncodingException");
            }

            URL = "http://api.treasury.io/cc7znvq/47d80ae900e04f2/sql/?q=" + QUERY;

            try {
                connection = (HttpURLConnection) new URL(URL)
                        .openConnection();

                InputStream in = new BufferedInputStream(
                        connection.getInputStream());

                data = readStream(in);

            } catch (MalformedURLException exception) {
                Log.e(TAG, "MalformedURLException");
            } catch (IOException exception) {
                Log.e(TAG, "IOException");
            } finally {
                if (null != connection)
                    connection.disconnect();
            }
            return data;
        }

        private String readStream(InputStream in) {
            String TAG = "HttpGetTask";
            BufferedReader reader = null;
            // StringBuffer is a thread-safe String that can also be changed
            StringBuffer data = new StringBuffer("");
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException");
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return data.toString();
        }

        //Implement methods in the aidl file

        //Method play(String s)
        //Reset the media player
        //Based on the value of String argument s, set the data source for the media player
        //Then play the clip
        @Override
        public void play(String s) throws RemoteException {
            mPlayer.reset();
            String path = "android.resource://" + getPackageName() + "/raw/";
            switch (s) {
                case "1":
                    path += "clip1";
                    try {
                        mPlayer.setDataSource(getApplicationContext(), Uri.parse(path));
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "2":
                    path += "clip2";
                    try {
                        mPlayer.setDataSource(getApplicationContext(), Uri.parse(path));
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "3":
                    path += "clip3";
                    try {
                        mPlayer.setDataSource(getApplicationContext(), Uri.parse(path));
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        }

        //Method pause()
        //Check if the media player is playing
        //If yes, pause and set isPaused to true: use for resume
        @Override
        public void pause() throws RemoteException {
            if(mPlayer.isPlaying()) {
                mPlayer.pause();
                isPaused = true;
            }
        }

        //Method resume()
        //Check if the media player is playing and is in Paused state
        //If yes, resume and set isPaused back to false
        @Override
        public void resume() throws RemoteException {
            if(!mPlayer.isPlaying() && isPaused) {
                mPlayer.start();
                isPaused = false;
            }
        }

        //Method stop()
        //Stop the media player
        @Override
        public void stop() throws RemoteException {
            mPlayer.stop();
        }
    };

    //Release resource for the media player when client unbind from the service
    @Override
    public boolean onUnbind(Intent intent){
        mPlayer.release();
        mPlayer = null;
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //When the client bind to the service
    //Prepare the media player
    //Return IBinder object
    @Override
    public IBinder onBind(Intent intent) {
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.clip1);
        mPlayer.start();
        mPlayer.pause();
        return mBinder;
    }
}
