package com.xycm.poc.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.xycm.poc.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FaceCaptureActivity extends BaseActivity
        implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.PictureCallback {

    private SurfaceView surfaceView;
    private TextView titleTextView, countdownTextView;
    private Button captureButton;
    private ImageView closeButton;

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private boolean isCameraFrontFacing = true;
    private Camera.Size previewSize;
    private int previewWidth = 0, previewHeight = 0;

    private boolean isCapturing = false, isPreviewing = false, isProcessing = false;

    private MediaActionSound mediaSound;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private Handler mainHandler;
    private CountDownTimer countdownTimer;

    private final Map<String, Object> config = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_capture);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupImmersiveMode();
        initUI();

        mediaSound = new MediaActionSound();
        mainHandler = new Handler(Looper.getMainLooper());
        loadConfig();
        initGestureDetectors();
    }

    private void setupImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    private void initUI() {
        surfaceView = findViewById(R.id.surfaceView);
        titleTextView = findViewById(R.id.titleTextView);
        countdownTextView = findViewById(R.id.countdownTextView);
        captureButton = findViewById(R.id.captureButton);
        closeButton = findViewById(R.id.close_btn);

        closeButton.setOnClickListener(v -> {
            if (isProcessing) {
                Toast.makeText(this, "正在处理图片，请稍候...", Toast.LENGTH_SHORT).show();
                return;
            }
            cancelCapture();
        });

        captureButton.setOnClickListener(v -> capturePhoto());

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void loadConfig() {
        config.put("quality", 0.85);
        config.put("timeout", 30000L);
        config.put("needSound", true);
        config.put("maxSize", 1024);
        config.put("needBase64", true);
        config.put("autoCapture", false);
        config.put("countdown", 3);
        updateGuideText("请保证光线充足，面容整洁的情况下进行人脸识别");
    }

    private void initGestureDetectors() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                switchCamera();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                performAutoFocus(e);
                return true;
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        adjustZoom(detector.getScaleFactor());
                        return true;
                    }
                });

        surfaceView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void performAutoFocus(MotionEvent event) {
        if (camera == null || !isPreviewing) return;
        Camera.Parameters params = camera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 1000));
            params.setFocusAreas(focusAreas);
            camera.setParameters(params);
            camera.autoFocus((success, cam) -> {
            });
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = (int) (200 * coefficient);
        int left = Math.max(0, Math.min((int) (x - areaSize / 2), previewWidth));
        int top = Math.max(0, Math.min((int) (y - areaSize / 2), previewHeight));
        int right = Math.max(0, Math.min(left + areaSize, previewWidth));
        int bottom = Math.max(0, Math.min(top + areaSize, previewHeight));
        return new Rect(left, top, right, bottom);
    }

    private void adjustZoom(float scaleFactor) {
        if (camera == null) return;
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int newZoom = (int) Math.max(1, Math.min(params.getZoom() * scaleFactor, params.getMaxZoom()));
            params.setZoom(newZoom);
            camera.setParameters(params);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null && isPreviewing) restartPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        isProcessing = true;
        new Thread(() -> processImageData(data)).start();
    }

    private void processImageData(byte[] data) {
        try {
            if ((Boolean) config.getOrDefault("needSound", true))
                mediaSound.play(MediaActionSound.SHUTTER_CLICK);
            Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap rotated = rotateAndMirrorBitmap(original);
            Bitmap finalBitmap = compressToSquare(rotated, 390);
            String filePath = saveBitmapToFile(finalBitmap);
            String base64 = bitmapToBase64(finalBitmap);

            Map<String, Object> result = new HashMap<>();
            result.put("action", "capture");
            result.put("base64Image", base64);
            result.put("filePath", filePath);
            result.put("timestamp", System.currentTimeMillis());
            result.put("imageWidth", finalBitmap.getWidth());
            result.put("imageHeight", finalBitmap.getHeight());
            result.put("quality", config.get("quality"));
            result.put("cameraFacing", isCameraFrontFacing ? "front" : "back");

            mainHandler.post(() -> sendResultAndFinish(result));
            recycleBitmaps(original, rotated, finalBitmap);
        } catch (Exception e) {
            Log.e("FaceCapture", "处理失败: " + e.getMessage(), e);
            mainHandler.post(() -> {
                Toast.makeText(this, "处理失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                sendErrorResult("处理失败: " + e.getMessage());
            });
        } finally {
            isProcessing = false;
            isCapturing = false;
            mainHandler.post(() -> {
                if (camera != null) {
                    camera.startPreview();
                    isPreviewing = true;
                }
            });
        }
    }

    private void capturePhoto() {
        if (camera == null || isCapturing || isProcessing) return;
        isCapturing = true;
        int countdown = (int) config.getOrDefault("countdown", 0);
        if (countdown > 0) startCountdown(countdown, this::takePicture);
        else takePicture();
    }

    private void startCountdown(int seconds, Runnable onFinish) {
        countdownTextView.setVisibility(View.VISIBLE);
        if (countdownTimer != null) countdownTimer.cancel();
        countdownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int s = (int) (millisUntilFinished / 1000) + 1;
                countdownTextView.setText(String.valueOf(s));
                countdownTextView.setTextColor(s <= 3 ? Color.RED : Color.WHITE);
            }

            @Override
            public void onFinish() {
                countdownTextView.setVisibility(View.GONE);
                onFinish.run();
            }
        }.start();
    }

    private void takePicture() {
        if (camera != null) camera.takePicture(null, null, this);
    }

    private void switchCamera() {
        if (isCapturing || isProcessing) return;
        int count = Camera.getNumberOfCameras();
        if (count < 2) {
            Toast.makeText(this, "未找到第二个摄像头", Toast.LENGTH_SHORT).show();
            return;
        }
        currentCameraId = (currentCameraId + 1) % count;
        isCameraFrontFacing = currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;
        stopPreview();
        mainHandler.postDelayed(() -> {
            startCamera();
            updateGuideText(isCameraFrontFacing ? "请将人脸对准框内" : "请将身份证对准框内");
        }, 300);
    }

    private void updateGuideText(String text) {
        if (titleTextView != null) titleTextView.setText(text);
    }

    private Bitmap rotateAndMirrorBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraId, info);
        Matrix matrix = new Matrix();
        int rotation = info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? 270 : 90;
        matrix.postRotate(rotation);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) matrix.postScale(-1, 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap compressToSquare(Bitmap bitmap, int targetSize) {
        if (bitmap == null) return null;
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        float scale = (float) targetSize / Math.min(w, h);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, (int) (w * scale), (int) (h * scale), true);
        int x = (scaled.getWidth() - targetSize) / 2;
        int y = (scaled.getHeight() - targetSize) / 2;
        return Bitmap.createBitmap(scaled, x, y, targetSize, targetSize);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int q = (int) (((Number) config.getOrDefault("quality", 0.85)).floatValue() * 100);
        bitmap.compress(Bitmap.CompressFormat.JPEG, q, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        if (bitmap == null) return "";
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 201)) return "";
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) return "";
        try {
            File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FaceCapture");
            if (!storageDir.exists() && !storageDir.mkdirs()) Log.e("FaceCapture", "创建目录失败");
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File file = new File(storageDir, "FACE_" + ts + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            int q = (int) (((Number) config.getOrDefault("quality", 0.85)).floatValue() * 100);
            bitmap.compress(Bitmap.CompressFormat.JPEG, q, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e("FaceCapture", "保存失败: " + e.getMessage());
            return "";
        }
    }

    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    private void recycleBitmaps(Bitmap... bitmaps) {
        for (Bitmap b : bitmaps) {
            if (b != null && !b.isRecycled()) b.recycle();
        }
    }

    private void sendResultAndFinish(Map<String, Object> result) {
        Intent intent = new Intent();
        intent.putExtra("action", "capture");
        intent.putExtra("base64Image", (String) result.get("base64Image"));
        intent.putExtra("filePath", (String) result.get("filePath"));
        intent.putExtra("timestamp", String.valueOf(result.get("timestamp")));
        intent.putExtra("imageWidth", (Integer) result.get("imageWidth"));
        intent.putExtra("imageHeight", (Integer) result.get("imageHeight"));
        intent.putExtra("quality", (Double) config.getOrDefault("quality", 0.85));
        intent.putExtra("cameraFacing", isCameraFrontFacing ? "front" : "back");
        setResult(RESULT_OK, intent);
        finish();
    }

    private void sendErrorResult(String msg) {
        Intent intent = new Intent();
        intent.putExtra("error", msg);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void cancelCapture() {
        if (countdownTimer != null) countdownTimer.cancel();
        if (isProcessing) {
            Toast.makeText(this, "正在处理中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("action", "cancel");
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void showErrorAndExit(String msg) {
        if (isFinishing() || isDestroyed()) return;
        runOnUiThread(() -> {
            new AlertDialog.Builder(FaceCaptureActivity.this)
                    .setTitle("错误")
                    .setMessage(msg)
                    .setPositiveButton("确定", (d, w) -> {
                        d.dismiss();
                        sendErrorResult(msg);
                    })
                    .setCancelable(false).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countdownTimer != null) countdownTimer.cancel();
        stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownTimer != null) countdownTimer.cancel();
        releaseCamera();
        if (mediaSound != null) mediaSound.release();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startCamera();
        else if (requestCode == 201 && (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED))
            Toast.makeText(this, "需要存储权限保存照片", Toast.LENGTH_SHORT).show();
        else if (requestCode == 200) showErrorAndExit("需要摄像头权限才能使用人脸识别功能");
    }

    private void startCamera() {
        if (!checkPermission(Manifest.permission.CAMERA, 200)) return;
        initCamera();
    }

    private void initCamera() {
        try {
            releaseCamera();
            camera = Camera.open(currentCameraId);

            Camera.Parameters parameters = camera.getParameters();

            // 预览尺寸
            previewSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(),
                    surfaceView.getWidth(), surfaceView.getHeight());
            if (previewSize != null) {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
            }

            // 图片尺寸
            Camera.Size pictureSize = getOptimalPictureSize(parameters.getSupportedPictureSizes());
            if (pictureSize != null)
                parameters.setPictureSize(pictureSize.width, pictureSize.height);

            // 对焦模式
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            // 图片格式与质量
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality((int) (((Number) config.get("quality")).floatValue() * 100));

            camera.setParameters(parameters);

            // 预览方向
            setCameraDisplayOrientation(currentCameraId);

            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);
            camera.startPreview();
            isPreviewing = true;
        } catch (Exception e) {
            Log.e("FaceCapture", "启动相机失败: " + e.getMessage(), e);
            showErrorAndExit("无法启动相机: " + e.getMessage());
        }
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
        return optimalSize != null ? optimalSize : sizes.get(0);
    }

    private Camera.Size getOptimalPictureSize(List<Camera.Size> sizes) {
        if (sizes == null || sizes.isEmpty()) return null;
        Camera.Size optimal = sizes.get(0);
        for (Camera.Size s : sizes) {
            if (s.width * s.height > optimal.width * optimal.height) optimal = s;
        }
        return optimal;
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
        camera.setDisplayOrientation(result);
    }

    private void restartPreview() {
        stopPreview();
        startPreview();
    }

    private void startPreview() {
        if (camera != null && !isPreviewing) {
            camera.startPreview();
            isPreviewing = true;
        }
    }

    private void stopPreview() {
        if (camera != null && isPreviewing) {
            camera.stopPreview();
            isPreviewing = false;
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            isPreviewing = false;
        }
    }
}
