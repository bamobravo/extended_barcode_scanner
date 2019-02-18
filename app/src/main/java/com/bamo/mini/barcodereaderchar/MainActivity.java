package com.bamo.mini.barcodereaderchar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.nfc.Tag;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Camera cam;
    private SurfaceView surface;
    private SurfaceHolder holder;
    private List<Size> mSupportedPreviewSizes;
    private int mPreviewState;
    private EditText textContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        cam = Camera.open();
          setContentView(R.layout.activity_main);
          surface=findViewById(R.id.cameraView);
          textContainer = findViewById(R.id.codeDisplay);
//        holder = surface.getHolder();
//        holder.addCallback(new CameraHandler());
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void  onResume(){
        super.onResume();
//        cam = Camera.open();
////        surface=(SurfaceView)findViewById(R.id.cameraView);
//        holder = surface.getHolder();
//        holder.addCallback(new CameraHandler());
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    public void showCamera(View buttonView){
        try {
            FrameCallback encoder = new FrameCallback(this,(EditText) findViewById(R.id.codeDisplay));
            encoder.startProcess(this,(EditText) findViewById(R.id.codeDisplay));
            if (cam==null){
                cam = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }

//            cam.startPreview();
            setCamera(cam);
        }
        catch(Exception ex){
            Toast.makeText(this,"Cannot start camera",3000).show();
            return;
        }
//        cam.startPreview();
    }

    public void setCamera(Camera camera) {
//        if (cam == camera) { return; }
//        stopPreviewAndFreeCamera();
//        cam = camera;
        if (cam != null) {
            List<Size> localSizes = cam.getParameters().getSupportedPreviewSizes();
            mSupportedPreviewSizes = localSizes;
            Size size = localSizes.get(0);
            Camera.Parameters parameters = cam.getParameters();
            parameters.setPreviewSize(size.width, size.height);
            try {
                cam.setPreviewDisplay(holder);
                cam.setPreviewCallback(new FrameCallback(this,textContainer));
            } catch (IOException e) {
                e.printStackTrace();
            }
            cam.startPreview();
        }
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            cam = Camera.open(id);
            qOpened = (cam != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        setCamera(null);
        if (cam != null) {
            cam.release();
            cam = null;
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (cam != null) {
            cam.stopPreview();
            cam.release();
            cam = null;
        }
    }


    public Bitmap getTestBitMap(){

        Bitmap result = BitmapFactory.decodeResource(getResources(),R.drawable.generated);
        return result;
    }
//    inner class for the surfaceView and surface holder

    class CameraHandler implements SurfaceHolder.Callback{

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            if (surfaceHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            if(cam!=null) {
                try {
                    cam.stopPreview();
                    Camera.Parameters parameters = cam.getParameters();
                    List<Size> localSizes = cam.getParameters().getSupportedPreviewSizes();
                    Size size = localSizes.get(0);
                    parameters.setPreviewSize(size.width, size.height);
                    cam.setParameters(parameters);
                    cam.setPreviewDisplay(surfaceHolder);
                    cam.startPreview();
                }
                catch (Exception e){
                    Log.d("eRROR", "Error starting camera preview: " + e.getMessage());
                }
            }

        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        if(cam!=null){
            try {
                cam.setPreviewDisplay(surfaceHolder);
                cam.startPreview();
            }
            catch (Exception ex){
                Log.d("eRROR","Error setting camera previoew");
            }
        }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (cam != null) {
                cam.stopPreview();
            }

        }

    }
}
