package com.example.printing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.print.PrintHelper;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.VectorDrawable;
import android.hardware.Camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    MediaPlayer mPlayer;
    Button playButton, pauseButton, stopButton;
    SeekBar volumeControl;
    AudioManager audioManager;
    TextView tv;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Camera camera;
    private final int REQUEST_CODE_PERMISSION_CAMERA = 1;
    MediaRecorder mediaRecorder;
    File photoFile;
    File videoFile;
    ImageView iv;
    VideoView vv;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.start_camera);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        iv = (ImageView)findViewById(R.id.iv);
        vv = (VideoView)findViewById(R.id.vv);

        tv = (TextView) findViewById(R.id.music);
        mPlayer=MediaPlayer.create(this, R.raw.music);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlay();
            }
        });
        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curValue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeControl = findViewById(R.id.volumeControl);
        volumeControl.setMax(maxVolume);
        volumeControl.setProgress(curValue);
        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    public void btnCamera(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, 1);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnVideo(View view){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, 2);
        }else Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            iv.setImageBitmap(imageBitmap);
        }
        else if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            vv.setVideoURI(videoUri);
            vv.setMediaController(new MediaController(this));
            vv.requestFocus(0);
            vv.start(); // начинаем воспроизведение автоматически
        }
    }

    private void stopPlay(){
        tv.setText("Nothing is being played");
        mPlayer.stop();
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        try {
            mPlayer.prepare();
            mPlayer.seekTo(0);
            playButton.setEnabled(true);
        }
        catch (Throwable t) {
            Toast.makeText(this, t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void play(View view){
        tv.setText("Now playing: Filmix - UP");
        mPlayer.start();
        playButton.setEnabled(false);
        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);
    }
    public void pause(View view){
        tv.setText("Play on pause");
        mPlayer.pause();
        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(true);
    }
    public void stop(View view){
        stopPlay();
    }

    public void btnPrint(View view){
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
        photoPrinter.printBitmap("rodion.jpg - test print", bitmap);
    }
    public void btnHTML(View view){
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument = "<html><body><h1>Test printing</h1><p>Rodion, " +
                "testing, testing...</p></body></html>";
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }
    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

        String jobName = getString(R.string.app_name) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        // Create a print job with name and adapter instance
        PrintJob printJob = printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer.isPlaying()) {
            stopPlay();
        }
    }


}

