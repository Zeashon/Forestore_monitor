package com.example.zeashon.forestore_monitor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        final Button mButton = (Button) findViewById(R.id.monitor);
        final Button btnCamera = (Button) findViewById(R.id.camera_monitor);
        final Button btnMicro = (Button) findViewById(R.id.micro_monitor);
        final Button btnSignal = (Button) findViewById(R.id.signal_monitor);
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Context context = getApplicationContext();
                String str = mButton.getText().toString();
                if (str.equals("Monitor")) {
                    Log.i("zeashon", str);
                    mButton.setText("Guarding");
                    if (checkNetworkState()) {//网络信号
                        btnSignal.setBackgroundResource(R.drawable.state_ok);
                    } else
                        btnSignal.setBackgroundResource(R.drawable.state_err);
                    if (checkCameraDevice(context)) {//摄像头
                        btnCamera.setBackgroundResource(R.drawable.state_ok);
                    } else
                        btnCamera.setBackgroundResource(R.drawable.state_err);
                    if (checkMicroDevice()) {//麦克风
                        btnMicro.setBackgroundResource(R.drawable.state_ok);
                    } else
                        btnMicro.setBackgroundResource(R.drawable.state_err);

                } else {
                    Log.i("zeashon", str);
                    if (str.equals("Guarding")) {
                        mButton.setText("Monitor");
                        //网络信号
                        if (checkNetworkState()) {
                            btnSignal.setBackgroundResource(R.drawable.signal_nor);
                        } else
                            btnSignal.setBackgroundResource(R.drawable.signal_err);
                        //摄像头
                        if (checkCameraDevice(context)) {
                            btnCamera.setBackgroundResource(R.drawable.camera_nor);
                        } else
                            btnCamera.setBackgroundResource(R.drawable.camera_err);
                        //麦克风
                        if (checkMicroDevice()) {
                            btnMicro.setBackgroundResource(R.drawable.micro_nor);
                        } else
                            btnMicro.setBackgroundResource(R.drawable.micro_err);
                    }
                }
            }
        });

    }

    /**
     * 检测网络是否连接
     *
     * @return
     */
    private boolean checkNetworkState() {
        boolean flag = false;
        //得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
        return flag;
    }

    /**
     * 检测摄像头是否可用
     *
     * @return
     */
    private boolean checkCameraDevice(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检测麦克风是否可用
     *
     * @return
     */
    private boolean checkMicroDevice() {
        MediaRecorder mRecorder = new MediaRecorder();
        String filePath = "/dev/null";
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(filePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
            mRecorder.release();
            mRecorder = null;
            return true;
        } catch (IOException e) {
            Log.e("zeashon-MediaRecord", "prepare() failed");
            return false;
        }
    }

    //    check Sd card
    private boolean checkSdCard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(intent, 0);
        } else {
            Toast.makeText(MainActivity.this,
                    "common_msg_nosdcard", Toast.LENGTH_LONG).show();
        }
        return true;
    }
}
//
//    /**
//     * 提交参数里有文件的数据
//     *
//     * @param url
//     *            服务器地址
//     * @param param
//     *            参数
//     * @return 服务器返回结果
//     * @throws Exception
//     */
//    public static String uploadSubmit(String url, Map<String, String> param,
//                                      File file) throws Exception {
//        HttpPost post = new HttpPost(url);
//
//        MultipartEntity entity = new MultipartEntity();
//        if (param != null && !param.isEmpty()) {
//            for (Map.Entry<String, String> entry : param.entrySet()) {
//                entity.addPart(entry.getKey(), new StringBody(entry.getValue()));
//            }
//        }
//        // 添加文件参数
//        if (file != null && file.exists()) {
//            entity.addPart("file", new FileBody(file));
//        }
//        post.setEntity(entity);
//        HttpResponse response = httpClient.execute(post);
//        int stateCode = response.getStatusLine().getStatusCode();
//        StringBuffer sb = new StringBuffer();
//        if (stateCode == HttpStatus.SC_OK) {
//            HttpEntity result = response.getEntity();
//            if (result != null) {
//                InputStream is = result.getContent();
//                BufferedReader br = new BufferedReader(
//                        new InputStreamReader(is));
//                String tempLine;
//                while ((tempLine = br.readLine()) != null) {
//                    sb.append(tempLine);
//                }
//            }
//        }
//        post.abort();
//        return sb.toString();
//    }
//}
