package com.lux.ex073cameraappvideo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    VideoView vv;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vv=findViewById(R.id.vv);
        btn=findViewById(R.id.btn);
        btn.setOnClickListener(view -> clickBtn());

        //동적 퍼미션 체크
        String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (checkSelfPermission(permissions[0])== PackageManager.PERMISSION_DENIED){
            requestPermissions(permissions,10);

            //퍼미션이 허가되어 있지 않다면 버튼 사용을 못하도록
            btn.setEnabled(false);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==10&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Camera 사용 가능", Toast.LENGTH_SHORT).show();
            btn.setEnabled(true);
        }else {
            Toast.makeText(this, "camera 사용 불가", Toast.LENGTH_SHORT).show();
            btn.setEnabled(false);  //안해도 됨.
        }
    }

    //비디오가 저장될 uri
    Uri videoUri;

    void setVideoUri(){
        //외부 저장소의 공용 영역
        File path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName="video_"+simpleDateFormat.format(new Date())+".mp4";
        File file=new File(path,fileName);

        //file -> uri로 변경
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
            videoUri=Uri.fromFile(file);
        }else {
            videoUri= FileProvider.getUriForFile(this,"com.lux.ex073cameraappvideo.FileProvider",file);
        }

        //new AlertDialog.Builder(this).setMessage(videoUri.toString()).create().show();
    }

   void clickBtn(){
        Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        setVideoUri();
        if (videoUri!=null) intent.putExtra(MediaStore.EXTRA_OUTPUT,videoUri);

        //결과를 받기위한 실행
       resultLauncher.launch(intent);
    }
    ActivityResultLauncher<Intent> resultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode()==RESULT_OK){
                vv.setVideoURI(videoUri);

                //비디오뷰에 컨트롤 바 보이도록
                MediaController controller=new MediaController(MainActivity.this);
                controller.setAnchorView(vv);
                vv.setMediaController(controller);


                //video는 용량이 커서 로딩하는데 오래걸리기에 바로 start()못하고
                //준비가 끝났을때 start()해야 함.
                vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        Toast.makeText(MainActivity.this, "prepared", Toast.LENGTH_SHORT).show();
                        vv.start();
                    }
                });
            }
        }
    });
}