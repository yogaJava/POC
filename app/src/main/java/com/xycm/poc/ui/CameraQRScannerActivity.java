package com.xycm.poc.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.xycm.poc.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class CameraQRScannerActivity extends AppCompatActivity
        implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    private static final String TAG = "CameraQRScanner";
    private static final int SCAN_INTERVAL = 500; // 扫描间隔(ms)

    private TextureView textureView;
    private View scanLine, scanFrame;
    private Button btnBack, btnFlash;

    private Camera camera;
    private boolean isFlashOn = false;
    private boolean scanningEnabled = true;

    private MultiFormatReader multiFormatReader;
    private Handler mainHandler;
    private Vibrator vibrator;

    private long lastScanTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_qr);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mainHandler = new Handler(Looper.getMainLooper());
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        initViews();
        multiFormatReader = new MultiFormatReader();
    }

    private void initViews() {
        textureView = findViewById(R.id.textureView);
        scanLine = findViewById(R.id.scanLine);
        scanFrame = findViewById(R.id.scanFrame);
        btnBack = findViewById(R.id.btnBack);
        btnFlash = findViewById(R.id.btnFlash);

        textureView.setSurfaceTextureListener(this);

        btnBack.setOnClickListener(v -> finish());
        btnFlash.setOnClickListener(v -> toggleFlash());

        textureView.post(this::startScanLineAnimation);
    }

    private void startScanLineAnimation() {
        if (scanFrame == null || scanLine == null) return;

        if (scanFrame.getHeight() == 0) {
            scanFrame.post(this::setupScanAnimation);
        } else {
            setupScanAnimation();
        }
    }

    private void setupScanAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                scanLine, "translationY", -scanFrame.getHeight() / 2F, scanFrame.getHeight() / 2F);
        animator.setDuration(1800);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.start();
    }

    private void toggleFlash() {
        if (camera == null) return;

        Camera.Parameters parameters = camera.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null || !flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
            Toast.makeText(this, "设备不支持闪光灯", Toast.LENGTH_SHORT).show();
            return;
        }

        parameters.setFlashMode(isFlashOn ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
        isFlashOn = !isFlashOn;
        btnFlash.setText(isFlashOn ? "关闭闪光" : "开启闪光");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void startCamera() {
        try {
            int cameraId = findBackCamera();
            if (cameraId == -1) {
                Toast.makeText(this, "未找到后置摄像头", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            camera = Camera.open(cameraId);
            setCameraDisplayOrientation(cameraId);

            Camera.Parameters parameters = camera.getParameters();
            Camera.Size optimalSize = getOptimalPreviewSize(
                    parameters.getSupportedPreviewSizes(), textureView.getWidth(), textureView.getHeight());

            if (optimalSize != null) {
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            } else {
                parameters.setPreviewSize(640, 480);
            }

            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);

            camera.setPreviewTexture(textureView.getSurfaceTexture());
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "启动相机失败", e);
            Toast.makeText(this, "启动相机失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private int findBackCamera() {
        int count = Camera.getNumberOfCameras();
        for (int i = 0; i < count; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) return i;
        }
        return -1;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        if (sizes == null || w == 0 || h == 0) return null;

        double targetRatio = (double) w / h;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > 0.1) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }
        return optimalSize;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!scanningEnabled) return;

        long now = System.currentTimeMillis();
        if (now - lastScanTime < SCAN_INTERVAL) return;
        lastScanTime = now;

        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        if (previewSize == null) return;

        int width = previewSize.width;
        int height = previewSize.height;

        // 扫描区域取 scanFrame 的比例
        int frameLeft = 0, frameTop = 0, frameWidth = width, frameHeight = height;
        if (scanFrame != null) {
            float scaleX = (float) width / textureView.getWidth();
            float scaleY = (float) height / textureView.getHeight();
            frameLeft = (int) (scanFrame.getLeft() * scaleX);
            frameTop = (int) (scanFrame.getTop() * scaleY);
            frameWidth = (int) (scanFrame.getWidth() * scaleX);
            frameHeight = (int) (scanFrame.getHeight() * scaleY);
        }

        try {
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                    data, width, height, frameLeft, frameTop, frameWidth, frameHeight, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = multiFormatReader.decodeWithState(bitmap);
            if (result != null) handleScanResult(result);
        } catch (Exception e) {
            // 解码失败，继续扫描
        }
    }

    private void handleScanResult(Result result) {
        if (result == null || !scanningEnabled) return;
        scanningEnabled = false;

        if (vibrator != null && vibrator.hasVibrator()) {
            try {
                vibrator.vibrate(200);
            } catch (Exception ignored) {
            }
        }

        String content = result.getText();
        Log.d(TAG, "扫码结果: " + content);
        EventBus.getDefault().post(new QRScannerEvent(content));

        Toast.makeText(this, "扫描成功!", Toast.LENGTH_SHORT).show();

        // 停止预览
        stopCamera();

        // 延迟 finish
        mainHandler.postDelayed(this::finish, 500);
    }

    private void stopCamera() {
        scanningEnabled = false;
        if (camera != null) {
            try {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
            } catch (Exception ignored) {
            }
            camera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textureView.isAvailable() && camera == null) startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();
        if (mainHandler != null) mainHandler.removeCallbacksAndMessages(null);
    }

    private void setCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }

        if (camera != null) camera.setDisplayOrientation(result);
    }
}
