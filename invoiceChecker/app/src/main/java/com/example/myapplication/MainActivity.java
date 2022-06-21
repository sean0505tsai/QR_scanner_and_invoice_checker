package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.common.StringUtils;

import java.io.IOException;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    SurfaceView mCameraView;
    TextView mResultTextView;
    TextView mInvoiceInfo;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    BottomNavigationView mBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = findViewById(R.id.cameraSurfaceView);
        mResultTextView = findViewById(R.id.resultText);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE | Barcode.CODE_39).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).
                setAutoFocusEnabled(true).setRequestedPreviewSize(300, 300).build();
        mBottomNav = findViewById(R.id.bottom_nav_bar);
        mBottomNav.getMenu().setGroupCheckable(0, false, false);
        mBottomNav.getMenu().getItem(2).setEnabled(false);
        mInvoiceInfo = findViewById(R.id.invoiceTextView);

        bottomMenuHandler();

        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    cameraSource.start(surfaceHolder);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes=detections.getDetectedItems();
                if(qrCodes.size()!=0){
                    mResultTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            scanResultStringHandler(qrCodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });
    }

    protected void scanResultStringHandler(String input){
        // AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        String http_check = input.substring(0, 3);
        String invoice_check = input.substring(0, 10);
        String number;
        String date;
        int amount;

        //mResultTextView.setText(input);
        //Linkify.addLinks(mResultTextView, Linkify.WEB_URLS);

        if (checkIfIsInvoice(input)){
            number = input.substring(0, 10);
            date = input.substring(10, 17);
            String date_show = date.substring(0, 3) + "/" + date.substring(3, 5)
                     + "/" + date.substring(5, 7);
            amount = Integer.parseInt(input.substring(29, 37), 16);
            mResultTextView.setText("發票號碼: " + number + "\n" + "日期: " + date_show + "\n" +
                    "金額: " + amount);
            invoiceChecker(input);

            // AD32397643 9 1110505 16 2059 20 00000033 28 00000036 36 0000000088099061gQ==:**********:1:1:1: 應稅 :1:54
         }else {
            mResultTextView.setText(input);
        }


    }

    protected void bottomMenuHandler(){
        mBottomNav.setOnNavigationItemSelectedListener((item) -> {
            switch (item.getItemId()) {

                case R.id.nav_scan:
                    Toast.makeText(this, "掃描", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_analysis:
                    Toast.makeText(this, "查看消費分析，功能未開放", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_record:
                    Toast.makeText(this, "查看紀錄", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });
    }

    protected boolean checkIfIsInvoice(String info){
        if (isInteger(info.substring(2, 9))) {
            return true;
        }else
            return false;
    }

    protected void invoiceChecker(String invoiceInfo){
        String number = invoiceInfo.substring(0, 10);
        String date = invoiceInfo.substring(10, 17);
        if(date.substring(3, 5).matches("03") || date.substring(3, 5).matches("04")){

        }else{
            mInvoiceInfo.setText("尚未開獎");
        }
    }

    protected static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
}