package com.example.scannerijinkaryawan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.scannerijinkaryawan.databinding.ActivityScannerBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.camera.CameraSourceConfig;
import com.google.mlkit.vision.camera.CameraXSource;
import com.google.mlkit.vision.camera.DetectionTaskCallback;
import com.google.mlkit.vision.common.InputImage;
import java.util.List;

import androidx.activity.OnBackPressedCallback;

public class ScannerActivity extends AppCompatActivity {
    ActivityScannerBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 1000;

    CameraXSource cameraXSource;
    String currentBarcode = null;
    int confirmCounter = 0;
    final static int CONFIRM_VALUE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                binding.cameraView.setVisibility(View.VISIBLE);
                cameraLauncher();
            }
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(binding.cameraView.getVisibility() == View.VISIBLE) {
                    binding.cameraView.setVisibility(View.GONE);
                    cameraXSource.stop();
                }
            }
        });
    }

    private void cameraLauncher() {
        BarcodeScannerOptions barcodeScannerOptions =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_CODE_39, //39 code
                                Barcode.FORMAT_CODE_128, //128 code
                                Barcode.FORMAT_CODE_93, //93 code
                                Barcode.FORMAT_QR_CODE) // barcode
                        .build();

        BarcodeScanner barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);

        CameraSourceConfig cameraSourceConfig = new CameraSourceConfig.Builder(this, barcodeScanner, new DetectionTaskCallback<List<Barcode>>() {
            @Override
            public void onDetectionTaskReceived(@NonNull Task<List<Barcode>> task) {
                task = barcodeScanner.process(InputImage.fromBitmap(binding.cameraView.getBitmap(), 0));
                task.addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(@NonNull List<Barcode> barcodes) {
                        if (barcodes.size() != 0) {
                            if(currentBarcode != null && currentBarcode.equals(barcodes.get(0).getDisplayValue())){
                                confirmCounter++;
                                if(confirmCounter > CONFIRM_VALUE){
                                    binding.etNik.post(new Runnable() {    // Use the post method of the TextView
                                        public void run() {
                                            binding.etNik.setText(currentBarcode);
                                            cameraXSource.stop();
                                            binding.cameraView.setVisibility(View.GONE);

                                        }
                                    });
                                }
                            }else {
                                currentBarcode = barcodes.get(0).getDisplayValue();
                                confirmCounter = 0;
                            }
                        }
                    }
                });
            }
        }).setFacing(CameraSourceConfig.CAMERA_FACING_BACK).build();
        cameraXSource = new CameraXSource(cameraSourceConfig, binding.cameraView);

        if (ActivityCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ScannerActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            return;
        }
        cameraXSource.start();
    }
}