package com.example.tanya.foto;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FotoRecognition extends Activity {
    // it is request code for action take picture
    private static final int TAKE_PICTURE_CODE = 100;
    // Parameter in MAX_FACES pass the maximum number of people who expect to find
    private static final int MAX_FACES = 5;
    // photo save in cameraBitmap
    private Bitmap cameraBitmap = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_the_first);

        Button takePicture = (Button) findViewById(R.id.take_picture);
        takePicture.setOnClickListener(btnClick);
    }

    // This method send Intent to Android Service and open Camera
    private void openCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE_CODE);
    }

    // this method processed requestCode
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (TAKE_PICTURE_CODE == requestCode && resultCode == RESULT_OK) {
            processCameraImage(intent);
        }
    }

    private void processCameraImage(Intent intent) {
        // open the next layout
        setContentView(R.layout.detectlayout);
        // attach listener to button, which we use for detect face on our photo
        Button detectFace = (Button) findViewById(R.id.detect_face);
        detectFace.setOnClickListener(btnClick);

        // get and set bitmap in image view
        cameraBitmap = (Bitmap) intent.getExtras().get("data");
        ((ImageView) findViewById(R.id.image_view)).setImageBitmap(cameraBitmap);
    }

    private void detectFaces() {
        if (cameraBitmap != null) {
            int width = cameraBitmap.getWidth();
            int height = cameraBitmap.getHeight();

        // Create instance of class FaceDector, an
            FaceDetector detector = new FaceDetector(width, height, FotoRecognition.MAX_FACES);
            Face[] faces = new Face[FotoRecognition.MAX_FACES];

            //convert bitmap to bitmap with a parameter RGB_565 another person will not be recognized ,
            // why is it only understands the format.
            Bitmap bitmap565 = Bitmap.createBitmap(width, height, Config.RGB_565);

            // tool for painting
            Paint ditherPaint = new Paint();
            Paint drawPaint = new Paint();

            // set params
            ditherPaint.setDither(true);
            drawPaint.setColor(Color.RED);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(2);

            // create canvas and draw our image
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap565);
            canvas.drawBitmap(cameraBitmap, 0, 0, ditherPaint);

            //looking for the faces in the photo, get an array of faces .
            int facesFound = detector.findFaces(bitmap565, faces);
            //midPoint - coordinate between the eyes
            PointF midPoint = new PointF();
            float eyeDistance = 0.0f;

            if (facesFound > 0) {
                for (int index = 0; index < facesFound; ++index) {
                    //get distance between eyes
                    faces[index].getMidPoint(midPoint);
                    eyeDistance = faces[index].eyesDistance();

                    // this method draw rect around face
                    canvas.drawRect((int) midPoint.x - eyeDistance,
                            (int) midPoint.y - eyeDistance,
                            (int) midPoint.x + eyeDistance,
                            (int) midPoint.y + eyeDistance, drawPaint);

                    Toast.makeText(getApplicationContext(), "Good Face!", Toast.LENGTH_SHORT).show();

                }
                // if don't found face, you will see Toast
            } else if (facesFound == 0) {
                Toast.makeText(getApplicationContext(), "Try again!", Toast.LENGTH_SHORT).show();
            }


            // save foto to SD card
            String filepath = Environment.getExternalStorageDirectory() + "/facedetect" + System.currentTimeMillis() + ".jpg";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filepath);
                bitmap565.compress(CompressFormat.JPEG, 100, fos);

                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // set result image
            ImageView imageView = (ImageView) findViewById(R.id.image_view);
            imageView.setImageBitmap(bitmap565);
        }
    }

    // it is listener for buttons
    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    openCamera();
                    break;
                case R.id.detect_face:
                    detectFaces();
                    break;
            }
        }
    };

    // back to the first activity, if you would like Try again
    public void back(View view) {
        Intent intent = new Intent(getApplicationContext(), FotoRecognition.class);
        startActivity(intent);
    }
}