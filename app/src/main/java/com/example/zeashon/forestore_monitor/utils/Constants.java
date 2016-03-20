package com.example.zeashon.forestore_monitor.utils;


public class Constants {

    public static final int ELAPSED_TIME =5*60* 1000;//4 hours
    public static final int RETRIVE_SERVICE_COUNT = 50;
    public static final int ELAPSED_TIME_DELAY = 2*60*1000;//get GPS delayed
    public static final int BROADCAST_ELAPSED_TIME_DELAY = 2*60*1000;
    public static final String WORKER_SERVICE = "com.example.zeashon.forestore_monitor.service.WorkService";
    public static final String POI_SERVICE = "com.example.zeashon.forestore_monitor.service.UploadPOIService";
    public static final String POI_SERVICE_ACTION = "com.example.zeashon.forestore_monitor.service.UploadPOIService.action";
    public static final String ACTION_STATUS = "android.intent.action.UploadActivity";

}
