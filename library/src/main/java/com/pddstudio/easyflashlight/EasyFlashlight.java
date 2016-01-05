package com.pddstudio.easyflashlight;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

/**
 * This Class was created by Patrick J
 * on 05.01.16. For more Details and Licensing
 * have a look at the README.md
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class EasyFlashlight {

    private enum State {
        ON,
        OFF
    }

    private static EasyFlashlight easyFlashlight;

    private static final int PERM_CODE = 42;

    private final Context context;
    private boolean hasFlashlight;
    private boolean hasPermission;

    private CameraManager cameraManager;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mBuilder;
    private CameraCaptureSession mSession;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private DeviceStateCallback deviceStateCallback;

    private boolean fireEvent = false;
    private State state;

    private EasyFlashlight(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.deviceStateCallback = new DeviceStateCallback();
        checkPermissions();
        try {
            checkHardwareControls();
        } catch (CameraAccessException c) {
            c.printStackTrace();
        }
    }

    public static void init(Context context) {
        if(easyFlashlight == null) easyFlashlight = new EasyFlashlight(context);
    }

    public static EasyFlashlight getInstance() {
        return easyFlashlight;
    }

    private void checkPermissions() {
        int code = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if(code == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true;
        } else {
            hasPermission = false;
        }
    }

    private void requestPermission(boolean fireEventAfter, State state) {
        ActivityCompat.requestPermissions((Activity) context, new String[] { Manifest.permission.CAMERA }, 42);
        this.fireEvent = fireEventAfter;
        this.state = state;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void checkHardwareControls() throws CameraAccessException {
        //hasFlashlight = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics("0");
        if(cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) != null) {
            hasFlashlight = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if(hasFlashlight) {
                if(hasPermission) cameraManager.openCamera("0", deviceStateCallback, null);
            }
        } else {
            hasFlashlight = false;
        }
    }

    /**
     * Check whether we can access the flashlight or not
     * @return
     */
    public boolean canAccessFlashlight() {
        return hasFlashlight && hasPermission;
    }

    /**
     * Check whether the permission was granted or not on API >= 23
     */
    public boolean checkPermission() {
        return hasPermission;
    }

    /**
     * Request Permission callback
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERM_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.hasPermission = true;
                try {
                    checkHardwareControls();
                } catch (CameraAccessException cm) {
                    cm.printStackTrace();
                }
            } else {
                this.hasPermission = false;
            }
        }
    }

    /**
     * Turn the flashlight on.
     */
    public void turnOn() {
        if(canAccessFlashlight()) {
            try {
                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                mSession.setRepeatingRequest(mBuilder.build(), null, null);
            } catch (CameraAccessException cm) {
                cm.printStackTrace();
            }
        } else {
            requestPermission(true, State.ON);
        }
    }

    /**
     * Turn the flashlight off.
     */
    public void turnOff() {
        if(canAccessFlashlight()) {
            try {
                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                mSession.setRepeatingRequest(mBuilder.build(), null, null);
            } catch (CameraAccessException cm) {
                cm.printStackTrace();
            }
        } else {
            requestPermission(true, State.OFF);
        }
    }

    /**
     * Disconnect all active connections.
     */
    public void close() {
        if(mCameraDevice != null || mSession != null) {
            return;
        }
        mSession.close();
        mCameraDevice.close();
        mCameraDevice = null;
        mSession = null;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class DeviceStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            //get builder
            try {
                mBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
                //flash on, default is on
                mBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                //mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                List<Surface> list = new ArrayList<Surface>();
                mSurfaceTexture = new SurfaceTexture(1);
                Size size = getSmallestSize(mCameraDevice.getId());
                mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                mSurface = new Surface(mSurfaceTexture);
                list.add(mSurface);
                mBuilder.addTarget(mSurface);
                camera.createCaptureSession(list, new CaptureSessionCallback(), null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    }

    private class CaptureSessionCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mSession = session;
            try {
                mSession.setRepeatingRequest(mBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    }

    private Size getSmallestSize(String cameraId) throws CameraAccessException {
        Size[] outputSizes = cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null || outputSizes.length == 0) {
            throw new IllegalStateException(
                    "Camera " + cameraId + "doesn't support any outputSize.");
        }
        Size chosen = outputSizes[0];
        for (Size s : outputSizes) {
            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                chosen = s;
            }
        }
        return chosen;
    }

}
