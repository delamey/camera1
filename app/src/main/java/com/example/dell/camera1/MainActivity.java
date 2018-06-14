package com.example.dell.camera1;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceView surfaceView;//预览摄像头
    private SurfaceHolder surfaceHolder;
    private Button button;//拍照按钮
    private Camera camera;//摄像头
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();
    }
    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.main_surface_view);
        button = (Button) findViewById(R.id.main_button);
    }

    private void initData() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void initListener() {
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "surfaceView", Toast.LENGTH_SHORT).show();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null,null,jpegCallback);
            }
        });
    }
    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback(){

        public void onPictureTaken(byte[] data, Camera camera) {
            Camera.Parameters ps = camera.getParameters();
            if(ps.getPictureFormat() == PixelFormat.JPEG){
                //存储拍照获得的图片
                String path = save(data);
//                //将图片交给Image程序处理
//                Uri uri = Uri.fromFile(new File(path));
//                Intent intent = new Intent();
//                intent.setAction("android.intent.action.VIEW");
//                intent.setDataAndType(uri, "image/jpeg");
//                startActivity(intent);
            }
        }
    };
    private String save(byte[] data){
        String path = "/sdcard/"+System.currentTimeMillis()+".jpg";
        try {
            //判断是否装有SD卡
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //判断SD卡上是否有足够的空间
                String storage = Environment.getExternalStorageDirectory().toString();
                StatFs fs = new StatFs(storage);
                long available = fs.getAvailableBlocks()*fs.getBlockSize();
                if(available<data.length){
                    //空间不足直接返回空
                    return null;
                }
                File file = new File(path);
                if(!file.exists())
                    //创建文件
                    file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return path;
    }
    private void initCamera() {
        Camera.Parameters parameters = camera.getParameters();//获取camera的parameter实例
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获取所有支持的camera尺寸
        Camera.Size optionSize = getOptimalPreviewSize(sizeList, surfaceView.getWidth(), surfaceView.getHeight());//获取一个最为适配的camera.size
        parameters.setPreviewSize(optionSize.width,optionSize.height);//把camera.size赋值到parameters
        camera.setParameters(parameters);//把parameters设置给camera
        camera.startPreview();
        camera.setDisplayOrientation(90);//将预览旋转90度
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);
        } catch (Exception e) {
            if (null != camera) {
                camera.release();
                camera = null;
            }
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "启动摄像头失败,请开启摄像头权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (null != camera) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }
}
