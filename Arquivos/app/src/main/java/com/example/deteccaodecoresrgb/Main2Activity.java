package com.example.deteccaodecoresrgb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main2Activity extends AppCompatActivity {

    private ImageView imagemOriginal;
    private ImageView imagemResultRed;
    private ImageView imagemResultGreen;
    private ImageView imagemResultBlue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        OpenCVLoader.initDebug();
        imagemOriginal = (ImageView) findViewById(R.id.imageID);
        imagemResultRed   = (ImageView) findViewById(R.id.image1ID);
        imagemResultGreen   = (ImageView) findViewById(R.id.image2ID);
        imagemResultBlue   = (ImageView) findViewById(R.id.image3ID);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String value = extras.getString("key");
            Uri uri = Uri.parse(value);

            Bitmap bitmapOriginal = null;

            try {
                bitmapOriginal = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            imagemOriginal.setImageBitmap(resizeImage(Main2Activity.this, bitmapOriginal, 400,400));

            Bitmap bitmap = ((BitmapDrawable)imagemOriginal.getDrawable()).getBitmap();

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            Bitmap bitmapResult = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);

            Scalar lowerRed = new Scalar(0,50,120);
            Scalar upperRed = new Scalar(10,255,255);

            Scalar lowerGreen = new Scalar(40,70,80);
            Scalar upperGreen = new Scalar(70,255,255);

            Scalar lowerBlue = new Scalar(90,60,0);
            Scalar upperBlue = new Scalar(121,255,255);

            Mat matRed = colorDetection(mat,lowerRed,upperRed);
            Mat matGreen = colorDetection(mat, lowerGreen, upperGreen);
            Mat matBlue = colorDetection(mat, lowerBlue, upperBlue);

            Utils.matToBitmap(matRed, bitmapResult);
            imagemResultRed.setImageBitmap(resizeImage(Main2Activity.this,bitmapResult,400,400));

            Utils.matToBitmap(matGreen, bitmapResult);
            imagemResultGreen.setImageBitmap(resizeImage(Main2Activity.this,bitmapResult,400,400));

            Utils.matToBitmap(matBlue, bitmapResult);
            imagemResultBlue.setImageBitmap(resizeImage(Main2Activity.this,bitmapResult,400,400));

        }

    }

    public Mat colorDetection(Mat imagem, Scalar lower, Scalar upper){

        Mat mat = imagem.clone();

        Imgproc.medianBlur(mat, mat, 9);

        Mat hsvImage = new Mat(mat.rows(), mat.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(mat, hsvImage, Imgproc.COLOR_RGB2HSV, 3);

        Mat mascara = new Mat(hsvImage.rows(), hsvImage.cols(), CvType.CV_8U, new Scalar(3));
        Core.inRange(hsvImage, lower, upper, mascara);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mascara,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(mat, contours, i, new Scalar(0, 0, 0), 5);
        }

        return mat;
    }

    public Bitmap resizeImage(Context context, Bitmap bmpOriginal,
                              float newWidth, float newHeight) {

        Bitmap novoBmp = null;

        int w = bmpOriginal.getWidth();
        int h = bmpOriginal.getHeight();

        float densityFactor = context.getResources().getDisplayMetrics().density;
        float novoW = newWidth * densityFactor;
        float novoH = newHeight * densityFactor;

        float scalaW = novoW / w;
        float scalaH = novoH / h;

        Matrix matrix = new Matrix();
        matrix.postScale(scalaW, scalaH);
        novoBmp = Bitmap.createBitmap(bmpOriginal, 0, 0, w, h, matrix, true);

        return novoBmp;
    }


    public  void onBackPressed(){
        finish();
        super.onBackPressed();
    }
}
