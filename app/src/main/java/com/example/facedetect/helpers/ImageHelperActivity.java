package com.example.facedetect.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.facedetect.R;
import com.example.facedetect.images.FaceDetectionActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ImageHelperActivity extends AppCompatActivity {

    private int REQUEST_PICK_IMAGE = 1000;
    private int REQUEST_CAPTURE_IMAGE = 1001;
    private ImageView inputImageView;
    private TextView outputTextView;

    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_helper);

        inputImageView = findViewById(R.id.imageView);

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }

        if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(ImageHelperActivity.class.getSimpleName(), "Grant result for " + permissions[0] + "is" + grantResults[0]);
    }

    public void onPickImage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(intent,REQUEST_PICK_IMAGE);
    }

    public void onClickImage(View view){
        photoFile = createPhotoFile();
        Uri fileUri = FileProvider.getUriForFile(this,"com.example.facedetect.provider",photoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

        startActivityForResult(intent , REQUEST_CAPTURE_IMAGE);
    }

    private File createPhotoFile(){
        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"ML_IMAGE_HELPER");
        if(!photoFile.exists()){
            photoFile.mkdirs();
        }
        String name = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File file = new File(photoFile.getPath() + File.separator + name);
        return file;
    }

    private Bitmap getCapturedImage(){
        int targetW = inputImageView.getWidth();
        int targetH = inputImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scale = Math.max(1,Math.min(photoH/targetH , photoW/targetW));

        bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scale;
        bmOptions.inMutable = true;
        return BitmapFactory.decodeFile(photoFile.getAbsolutePath(),bmOptions);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

                if (requestCode == REQUEST_PICK_IMAGE) {
                    Uri uri = data.getData();
                    Bitmap bitmap = loadFromUri(uri);
                    inputImageView.setImageBitmap(bitmap);
                } else if (requestCode == REQUEST_CAPTURE_IMAGE) {
                    Bitmap bitmap = getCapturedImage();
                    inputImageView.setImageBitmap(bitmap);
                    Log.d("TAG", "onActivityResult: Received Call Back");
                }
                
            /*Intent intent = new Intent(this, FaceDetectionActivity.class);
            startActivity(intent);*/
        }


    }

    private Bitmap loadFromUri(Uri uri){
        Bitmap bitmap = null;

        try {
            ImageDecoder.Source source = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                source = ImageDecoder.createSource(getContentResolver(),uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }else{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return bitmap;
    }

    protected void runDetection(Bitmap bitmap){};

    protected TextView getOutputTextView() {
        return outputTextView;
    }


    protected Bitmap drawDetectionResult(
            Bitmap bitmap,
            List<BoxWithText> detectionResults
    ) {
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);
        Paint pen = new Paint();
        pen.setTextAlign(Paint.Align.LEFT);

        for (BoxWithText box : detectionResults) {
            // draw bounding box
            pen.setColor(Color.RED);
            pen.setStrokeWidth(8F);
            pen.setStyle(Paint.Style.STROKE);
            canvas.drawRect(box.rect, pen);

            Rect tagSize = new Rect(0, 0, 0, 0);

            // calculate the right font size
            pen.setStyle(Paint.Style.FILL_AND_STROKE);
            pen.setColor(Color.YELLOW);
            pen.setStrokeWidth(2F);

            pen.setTextSize(96F);
            pen.getTextBounds(box.text, 0, box.text.length(), tagSize);
            float fontSize = pen.getTextSize() * box.rect.width() / tagSize.width();

            // adjust the font size so texts are inside the bounding box
            if (fontSize < pen.getTextSize()) {
                pen.setTextSize(fontSize);
            }

            float margin = (box.rect.width() - tagSize.width()) / 2.0F;
            if (margin < 0F) margin = 0F;
            canvas.drawText(
                    box.text, box.rect.left + margin,
                    box.rect.top + tagSize.height(), pen
            );
        }
        return outputBitmap;
    }
}