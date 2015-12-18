package ru.ivied.wastedtime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TimerService extends Service {

    private static final String TAG = "TimerService";

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

}
