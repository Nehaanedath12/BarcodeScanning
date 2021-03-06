package com.example.barcodescanning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ImageView barcode;
    SurfaceView surfaceView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    TextView barcodeText;
    String url;
    Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        barcode=findViewById(R.id.barcode);
        surfaceView=findViewById(R.id.surfaceView);
        barcodeText=findViewById(R.id.barcodeText);
        search=findViewById(R.id.search);



        barcode.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},101);
            }
            else {
                if (surfaceView.getVisibility() == View.VISIBLE) {
                    if(cameraSource!=null){
                        cameraSource.stop();
                    }
                    surfaceView.setVisibility(View.GONE);
                    barcodeText.setVisibility(View.GONE);
                } else if (surfaceView.getVisibility() == View.GONE) {

                    surfaceView.setVisibility(View.VISIBLE);
                    barcodeText.setVisibility(View.VISIBLE);
                    barcodeScanning();
                }
            }
        });

    }

    private void barcodeScanning() {

        barcodeDetector=new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        cameraSource=new CameraSource.Builder(getApplicationContext(),barcodeDetector).setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1080,1920).build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},101);
                }
                else {
                    try {
                        cameraSource.start(surfaceView.getHolder());
                    } catch (IOException e) {
                       Log.d("exception", e.toString()) ;
                    }
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

                Toast.makeText(MainActivity.this, "barcode released", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> array = detections.getDetectedItems();
                if (array.size() > 0) {
                    runOnUiThread(() -> {
                        barcodeText.setText(array.valueAt(0).displayValue);
                    });
            }
        }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url="http://"+barcodeText.getText().toString();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                surfaceView.setVisibility(View.GONE);
                barcodeText.setVisibility(View.GONE);
                if(cameraSource!=null){
                    cameraSource.stop();
                }
            }
        });
    }
}