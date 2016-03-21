package com.example.zeashon.forestore_monitor;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.MediaRecorder;

import com.example.zeashon.forestore_monitor.utils.Constants;
import com.example.zeashon.forestore_monitor.utils.ServiceUtil;

public class UploadActivity extends Activity {
    // 报像头
    private Camera mCamera;
    // 每天拍照时间
    private String fixedTime = "21";
    //自定义变量
    String TAG = "zeashon";
    String mCurrentPhotoPath;
    private Context mContext;
    MyReceiver receiver;
    //要上传的本地文件路径
    private String uploadFile;
    private String uploadRecoder;
    private String recorderName;
    //上传到服务器的指定位置
    private String actionUrl = "http://192.18.100.100:8080/upload/upload.jsp";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate");
        Log.i("zeashon", "uploadActivity ready");
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = (SurfaceView) this
                .findViewById(R.id.surfaceView);
        // 设置参数
        surfaceView.getHolder().setFixedSize(176, 144);
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        //  启动Service
//        Intent regIntent = new Intent("com.example.zeashon.forestore_monitor.service.UploadPOIService");
//        regIntent.putExtra("statusAction", "startUploadPOIService");
//        startService(regIntent);

//        final Button start = (Button) findViewById(R.id.takepicture);
//        start.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (mCamera != null) {
////                    start.setEnabled(false);
//                    // 拍照前需要对焦 获取清析的图片
//                    mCamera.autoFocus(new AutoFocusCallback() {
//                        // 对焦结束
//                        @Override
//                        public void onAutoFocus(boolean success, Camera camera) {
//                            // 对焦成功
//                            Toast.makeText(UploadActivity.this, "对焦成功 !!",
//                                    Toast.LENGTH_SHORT).show();
//                            mCamera.takePicture(null, null, new MyPictureCallback());
//                        }
//                    });
//                }
//            }
//        });

//        final Button record = (Button) findViewById(R.id.takerecorder);
//        record.setOnClickListener(new OnClickListener() {
//
//            Boolean isRecording = false;
//            MediaRecorder mediaRecorder = MyMediaRecorder();
//            @Override
//            public void onClick(View v) {
////                if(!isRecording){
////                    mediaRecorder.start();
//////                Log.i(TAG,recordFile.getAbsolutePath());
////                    Log.i(TAG,"recording");
////                    isRecording = true;
////            }else {
////                    mediaRecorder.stop();
////                    mediaRecorder.release();
////                    isRecording = false;
////                    Log.i(TAG,"record finished");
////                }
//                takePhotoRecord();
//            }
//        });


        receiver= new MyReceiver();

        IntentFilter filter=new IntentFilter();

        filter.addAction(Constants.ACTION_STATUS);

        UploadActivity.this.registerReceiver(receiver,filter);

//        AlarmManager trigger

        Button startAlarm = (Button) findViewById(R.id.setAlarm);
        Button stopAlarm = (Button) findViewById(R.id.stopAlarm);

        startAlarm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceUtil.invokeTimerPOIService(mContext);
            }
        });
        stopAlarm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceUtil.cancleAlarmManager(mContext);
            }
        });
    }

    //拍照且录音
    public void takePhotoRecord() {
        final MediaRecorder mediaRecorder = MyMediaRecorder();
        Boolean isRecording = false;
        if (mCamera != null) {
            // 拍照前需要对焦 获取清析的图片
            mCamera.autoFocus(new AutoFocusCallback() {
                // 对焦结束
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // 对焦成功
                    Toast.makeText(UploadActivity.this, "对焦成功 !!",
                            Toast.LENGTH_SHORT).show();
                    mCamera.takePicture(null, null, new MyPictureCallback());
                }
            });
        }
        Thread recorderThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mediaRecorder.start();
//                Log.i(TAG,recordFile.getAbsolutePath());
                Log.i(TAG, "recording");
            }
        });
        recorderThread.start();
        try {
            recorderThread.sleep(60 * 1000);
            Log.i(TAG, "recorder finished");
            mediaRecorder.stop();
            mediaRecorder.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        uploadFile(recorderName);
    }


    // 照片回调
    private final class MyPictureCallback implements PictureCallback {

        public String path;

        // 照片生成后
        public void onPictureTaken(byte[] data, Camera camera) {
            try {

                System.out.println("onPictureTaken");
                Bitmap bitmap = byteToBitmap(data);
                saveImageToGallery(UploadActivity.this, bitmap);
                Toast.makeText(UploadActivity.this,
                        "保存成功 !!" + path,
                        Toast.LENGTH_SHORT).show();
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 创建图片文件
        private File createImageFile() throws IOException {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            String storageDir = Environment.getExternalStorageDirectory() + "/picupload";
            File dir = new File(storageDir);
            if (!dir.exists())
                dir.mkdir();

            File image = new File(storageDir + "/" + imageFileName + ".jpg");

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
            Log.i(TAG, "photo path = " + mCurrentPhotoPath);
            return image;
        }

        //  byte[] 转 bitmap
        public Bitmap byteToBitmap(byte[] imgByte) {
            InputStream input = null;
            Bitmap bitmap = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            input = new ByteArrayInputStream(imgByte);
            SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                    input, null, options));
            bitmap = (Bitmap) softRef.get();
            if (imgByte != null) {
                imgByte = null;
            }

            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return bitmap;
        }

        //  把bitmap 格式的图片保存到图库
        public void saveImageToGallery(Context context, Bitmap bmp) {
            // 首先保存图片
            File appDir = new File(Environment.getExternalStorageDirectory(), "Forestore");
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);
            path = file.getAbsolutePath();
            //添加到上传队列
            uploadFile =path;
//            上传至服务器
            uploadFile(fileName);

            Log.i(TAG, path.toString()+"  upload to serve.");
            mCurrentPhotoPath = path;//将图片路径提取出来
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 其次把文件插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(context.getContentResolver(),
                        file.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 最后通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/Forestore")));
        }

    }


    // 预览界面回调
    private final class SurfaceCallback implements Callback {
        // 预览界面被创建
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                System.out.println("surfaceCreated");
                mCamera = Camera.open();// 打开摄像头
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(800, 480);
                parameters.setPreviewFrameRate(5);
                parameters.setPictureSize(1024, 768);
                parameters.setJpegQuality(80);
                mCamera.setParameters(parameters);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            System.out.println("surfaceChanged");

            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mCamera.startPreview();// 开始预览
        }

        // 预览界面被销毁
        public void surfaceDestroyed(SurfaceHolder holder) {
            System.out.println("surfaceDestroyed");
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
            if(receiver!=null){
                UploadActivity.this.unregisterReceiver(receiver);
                System.out.println("receiverDestroyed");
            }
        }

    }


    // 录音并保存到 SD Card 下的 Forestore文件夹
    private MediaRecorder MyMediaRecorder() {
        MediaRecorder mediaRecorder;
        mediaRecorder = new MediaRecorder();
        // 第1步：设置音频来源（MIC表示麦克风）
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //第2步：设置音频输出格式（默认的输出格式）
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        //第3步：设置音频编码方式（默认的编码方式）
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //创建一个临时的音频输出文件
        File appDir = new File(Environment.getExternalStorageDirectory(), "Forestore");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".amr";
        File file = new File(appDir, fileName);
//    audioFile=File.createTempFile("record_",".amr");
        //第4步：指定音频输出文件
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        //获取音频文件路径
        uploadRecoder = file.getAbsolutePath();
//        音频文件名
        recorderName = fileName;
        Log.i(TAG, "record path = " + file.getAbsolutePath());
        //第5步：调用prepare方法
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaRecorder;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
//        takePhotoRecord();
//        Log.i(TAG, "takePhotoRecord() is running.");
    }

    public class MyReceiver extends BroadcastReceiver {

        //自定义一个广播接收器

        @Override

        public void onReceive(Context context, Intent intent) {

        // TODO Auto-generated method stub

            System.out.println("OnReceiver");
            if(intent!=null){
                Log.i("intent:",intent.getDataString()+"-"+intent.getData());
            }
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                int flag = bundle.getInt("i");
                if (flag == 1) {
                    takePhotoRecord();
                    Log.i(TAG, "takePhotoRecord() is running.");
                }
            }else{
                Log.i(TAG, "the bundle is null.");
            }
//            pb.setProgress(a);
//
//            tv.setText(String.valueOf(a));

            //处理接收到的内容

        }
        public MyReceiver(){
            System.out.println("MyReceiver");
        //构造函数，做一些初始化工作

        }
    }

//    上传至服务器
    private void uploadFile( String fileName)
    {
        String end = "/r/n";
        String Hyphens = "--";
        String boundary = "*****";
        try
        {
            URL url = new URL(actionUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
      /* 允许Input、Output，不使用Cache */
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
      /* 设定传送的method=POST */
            con.setRequestMethod("POST");
      /* setRequestProperty */
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
      /* 设定DataOutputStream */
            DataOutputStream ds = new DataOutputStream(con.getOutputStream());
            ds.writeBytes(Hyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; "
                    + "name=/file1/;filename=/" + fileName + "/" + end);
            ds.writeBytes(end);
      /* 取得文件的FileInputStream */
            FileInputStream fStream = new FileInputStream(uploadFile);
      /* 设定每次写入1024bytes */
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
      /* 从文件读取数据到缓冲区 */
            while ((length = fStream.read(buffer)) != -1)
            {
        /* 将数据写入DataOutputStream中 */
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(Hyphens + boundary + Hyphens + end);
            fStream.close();
            ds.flush();
      /* 取得Response内容 */
            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1)
            {
                b.append((char) ch);
            }
            System.out.println("上传成功");
            Toast.makeText(UploadActivity.this, "上传成功", Toast.LENGTH_LONG)
                    .show();
            ds.close();
        } catch (Exception e)
        {
            System.out.println("上传失败" + e.getMessage());
            Toast.makeText(UploadActivity.this, "上传失败" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}

