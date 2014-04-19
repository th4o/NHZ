package org.nhzcrypto.service;

import org.nhzcrypto.droid.Settings;
import org.nhzcrypto.droid.ToolsPage;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class NHZBackgroudService extends IntentService{

    public NHZBackgroudService() {
        super("NHZBackgroudService");
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("NHZBackgroudService", "onDestroy");
        if ( null != mTicker ){
            mTicker.stop();
            mTicker = null;
        }
    }
    
    private Ticker mTicker;
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("NHZBackgroudService", "onHandleIntent");
        if ( null != mTicker )
            return;

        InfoCenter.shardInstance().init();
        mTicker = new Ticker();
        //mTicker.start(60 * 10 * 1000, new Ticker.TickerResponse() {
        int interval = ToolsPage.getRefreshInterval(NHZBackgroudService.this)
                * 60 * 1000;
        mTicker.start(interval, new Ticker.TickerResponse() {
            @Override
            public void onTick() {
                if ( !Settings.sharedInstance().isNotificationEnable(NHZBackgroudService.this) ){
                    if ( null != mTicker ){
                        mTicker.stop();
                        mTicker = null;
                    }
                    NHZBackgroudService.this.stopSelf();
                    return;
                }

                if ( null == mTicker )
                    return;

                Log.v("NHZBackgroudService", "onTick");
                InfoCenter.shardInstance().refresh(NHZBackgroudService.this);
                mTicker.setInterval(ToolsPage.getRefreshInterval(NHZBackgroudService.this)
                        * 60 * 1000);
            }
        });
        
        try {
            while(Settings.sharedInstance().isNotificationEnable(NHZBackgroudService.this)
                    && null != mTicker){
                    Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        LOG("Service onBind");
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public NHZBackgroudService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NHZBackgroudService.this;
        }
    }
    
    private void LOG(String log){
        Log.v("NHZBackgroudService", log);
    }
}
