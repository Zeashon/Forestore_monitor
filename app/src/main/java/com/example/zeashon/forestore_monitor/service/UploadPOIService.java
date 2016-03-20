package com.example.zeashon.forestore_monitor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.zeashon.forestore_monitor.utils.Constants;


/**
 * Created by coder80 on 2014/10/31.
 */
public class UploadPOIService extends Service implements Runnable{
    private String TAG = UploadPOIService.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        uploadPOIInfo();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "UploadPOIService onDestroy here.... ");
    }

    private void uploadPOIInfo() {
    	//simulation HTTP request to server 
    	new Thread(this).start();
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
            Intent intentSend = new Intent();
            intentSend.setAction(Constants.ACTION_STATUS);
//            Bundle bundle = new Bundle();
//            bundle.putString("i", "1");
//            intentSend.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intentSend.putExtra("statusAction","startTakePhotoRecord");
//            this.startActivity(intentSend);
            intentSend.putExtra("i", 1);
            sendBroadcast(intentSend);
			Log.i(TAG, "UploadPOIService beign to upload POI to server");
//            Log.i(TAG, String.valueOf(System.currentTimeMillis()));
			Thread.sleep(5 * 1000);
//            Log.i(TAG, String.valueOf(System.currentTimeMillis()));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopSelf();
	}

}
