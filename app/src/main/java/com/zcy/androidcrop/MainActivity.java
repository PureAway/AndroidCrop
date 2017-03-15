package com.zcy.androidcrop;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;
import com.zcy.imagecrop.UCrop;

import java.io.File;

import rx.Subscriber;

public class MainActivity extends AppCompatActivity {


    private static final int GALLERY_REQUEST_CODE = 0;    // 相册选图标记
    private static final int CAMERA_REQUEST_CODE = 1;    // 相机拍照标记
    private static final int CUT_REQUEST_CODE = 2;
    // 拍照临时图片
    private String mTempPhotoPath;
    // 剪切后图像文件
    private Uri mDestinationUri;
    private Button btnCamera, btnGallery;
    private ImageView imageView;
    private RxPermissions rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rxPermissions = new RxPermissions(this);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnGallery = (Button) findViewById(R.id.btnGallery);
        imageView = (ImageView) findViewById(R.id.imageView);
        mDestinationUri = Uri.fromFile(new File(MainActivity.this.getCacheDir(), "cropImage.png"));
        mTempPhotoPath = Environment.getExternalStorageDirectory() + File.separator + "photo.png";

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });
    }

    private void pickFromGallery() {
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "权限拒绝", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                            // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            startActivityForResult(pickIntent, GALLERY_REQUEST_CODE);
                        } else {
                            Toast.makeText(MainActivity.this, "权限拒绝", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void takePhoto() {
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "权限拒绝", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            //下面这句指定调用相机拍照后的照片存储的路径
                            takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mTempPhotoPath)));
                            startActivityForResult(takeIntent, CAMERA_REQUEST_CODE);
                        } else {
                            Toast.makeText(MainActivity.this, "权限拒绝", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE:   // 调用相机拍照
                    File temp = new File(mTempPhotoPath);
                    UCrop.of(Uri.fromFile(temp), mDestinationUri)
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(512, 512)
                            .withTargetActivity(CropActivity.class)
                            .start(MainActivity.this, CUT_REQUEST_CODE);
                    break;
                case GALLERY_REQUEST_CODE:  // 直接从相册获取
                    UCrop.of(data.getData(), mDestinationUri)
                            .withAspectRatio(1, 1)
                            .withMaxResultSize(512, 512)
                            .withTargetActivity(CropActivity.class)
                            .start(MainActivity.this, CUT_REQUEST_CODE);
                    break;
                case CUT_REQUEST_CODE:    // 裁剪图片结果
                    handleCropResult(data);
                    break;
                case UCrop.RESULT_ERROR:    // 裁剪图片错误
                    handleCropError(data);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 处理剪切成功的返回值
     *
     * @param result
     */
    private void handleCropResult(Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (null != resultUri) {
            imageView.setImageURI(resultUri);
        } else {
            Toast.makeText(MainActivity.this, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理剪切失败的返回值
     *
     * @param result
     */
    private void handleCropError(Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Toast.makeText(MainActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
    }
}
