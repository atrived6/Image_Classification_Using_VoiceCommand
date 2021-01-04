package com.androidcodeman.simpleimagegallery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.transition.Fade;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.androidcodeman.simpleimagegallery.fragments.pictureBrowserFragment;
import com.androidcodeman.simpleimagegallery.utils.MarginDecoration;
import com.androidcodeman.simpleimagegallery.utils.PicHolder;
import com.androidcodeman.simpleimagegallery.utils.itemClickListener;
import com.androidcodeman.simpleimagegallery.utils.pictureFacer;
import com.androidcodeman.simpleimagegallery.utils.picture_Adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ImageDisplay extends AppCompatActivity implements itemClickListener {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    RecyclerView imageRecycler;
    ArrayList<pictureFacer> allpictures;
    ArrayList<pictureFacer> male;
    ArrayList<pictureFacer> female;
    ArrayList<pictureFacer> indoor;
    ArrayList<pictureFacer> outdoor;
    ArrayList<pictureFacer> inMale;
    ArrayList<pictureFacer> outMale;
    ArrayList<pictureFacer> inFemale;
    ArrayList<pictureFacer> outFemale;
    ProgressBar load;
    String folderPath;
    TextView folderName;
    ImageButton btn;
    String command = "default";
    private ProgressDialog progress;
    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progress.incrementProgressBy(1);
        }
    };

    private static boolean isContain(String source, String subItem) {
        String pattern = "\\b" + subItem + "\\b";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(source);
        return m.find();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        folderName = findViewById(R.id.foldername);
        folderName.setText(getIntent().getStringExtra("folderName"));

        folderPath = getIntent().getStringExtra("folderPath");
        allpictures = new ArrayList<>();
        male = new ArrayList<>();
        female = new ArrayList<>();
        indoor = new ArrayList<>();
        outdoor = new ArrayList<>();
        inFemale = new ArrayList<>();
        inMale = new ArrayList<>();
        outFemale = new ArrayList<>();
        outMale = new ArrayList<>();
        imageRecycler = findViewById(R.id.recycler);
        imageRecycler.addItemDecoration(new MarginDecoration(this));
        imageRecycler.hasFixedSize();
        load = findViewById(R.id.loader);
        btn = findViewById(R.id.voicebtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();

            }
        });


        if (allpictures.isEmpty()) {
            load.setVisibility(View.VISIBLE);
            allpictures = getAllImagesByFolder(folderPath);
            imageRecycler.setAdapter(new picture_Adapter(allpictures, ImageDisplay.this, this));
            load.setVisibility(View.GONE);
        } else {

        }

        progressBar();

    }

    public void progressBar() {
        progress = new ProgressDialog(this);
        progress.setMax(allpictures.size());
        progress.setMessage("Images are classified according to Gender and Surrounding.");
        progress.setTitle("Classifying Images");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setCanceledOnTouchOutside(false);
        progress.setCancelable(false);
        progress.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result;
                String result_place;
                int count = 0;
                try {
                    while (progress.getProgress() <= progress.getMax()) {
                        Thread.sleep(200);
                        if (count < allpictures.size()) {
                            System.out.println(allpictures.get(count).getPicturePath());
                            result = upload(allpictures.get(count).getPicturePath());
                            result_place = places(allpictures.get(count).getPicturePath());
                           if(result.toLowerCase().equals("man") && result_place.toLowerCase().equals("indoor")){
                               inMale.add(allpictures.get(count));
                               male.add(allpictures.get(count));
                               indoor.add(allpictures.get(count));
                           }
                           else if(result.toLowerCase().equals("man") && result_place.toLowerCase().equals("outdoor")){
                               outdoor.add(allpictures.get(count));
                               outMale.add(allpictures.get(count));
                               male.add(allpictures.get(count));
                           }
                           else if(result.toLowerCase().equals("woman") && result_place.toLowerCase().equals("indoor")){female.add(allpictures.get(count));indoor.add(allpictures.get(count));inFemale.add(allpictures.get(count));}
                           else if(result.toLowerCase().equals("woman") && result_place.toLowerCase().equals("outdoor")){female.add(allpictures.get(count));outdoor.add(allpictures.get(count));outFemale.add(allpictures.get(count));}
                           else if(result.toLowerCase().equals("woman")){female.add(allpictures.get(count));}
                           else if(result.toLowerCase().equals("man")){male.add(allpictures.get(count));}
                           else if(result_place.toLowerCase().equals("indoor")){indoor.add(allpictures.get(count));}
                           else if(result_place.toLowerCase().equals("outdoor")){outdoor.add(allpictures.get(count));}
                           else {
                               System.out.println("Nothing assigned to image");
                           }
                            count = count + 1;
                        }

                        handle.sendMessage(handle.obtainMessage());
                        if (progress.getProgress() == progress.getMax()) {
                            progress.dismiss();
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    String upload(String path) {
        final String Name = getIntent().getStringExtra("Name");
        final String Gesture = getIntent().getStringExtra("GESTURE_ID");


        String url = "http://168.61.177.30:5000/gender";
        final OkHttpClient clientOk = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "test.jpg", RequestBody.create(MediaType.parse("image/jpg"), new File(path)))
                .build();

        Request request = new Request.Builder()
                .addHeader("Connection", "close")
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = clientOk.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return responseBody;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return Name;

    }

    String places(String path) {
        final String Name = getIntent().getStringExtra("Name");
        final String Gesture = getIntent().getStringExtra("GESTURE_ID");


        String url = "http://168.61.177.30:5001/places";
        final OkHttpClient clientOk = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "test.jpg", RequestBody.create(MediaType.parse("image/jpg"), new File(path)))
                .build();

        Request request = new Request.Builder()
                .addHeader("Connection", "close")
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = clientOk.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return responseBody;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return Name;

    }

    /**
     * This Method gets all the images in the folder paths passed as a String to the method and returns
     * and ArrayList of pictureFacer a custom object that holds data of a given image
     *
     * @param path a String corresponding to a folder path on the device external storage
     */
    public ArrayList<pictureFacer> getAllImagesByFolder(String path) {
        ArrayList<pictureFacer> images = new ArrayList<>();
        Uri allVideosuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};
        Cursor cursor = ImageDisplay.this.getContentResolver().query(allVideosuri, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{"%" + path + "%"}, null);
        try {
            cursor.moveToFirst();
            do {
                pictureFacer pic = new pictureFacer();

                pic.setPicturName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));

                pic.setPicturePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));

                pic.setPictureSize(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));


                images.add(pic);


            } while (cursor.moveToNext());
            cursor.close();
            ArrayList<pictureFacer> reSelection = new ArrayList<>();
            for (int i = images.size() - 1; i > -1; i--) {
                reSelection.add(images.get(i));
            }
            images = reSelection;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return images;
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak your command");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    command = result.get(0).toLowerCase();
                    if (isContain(command, "indoor") && isContain(command, "man")) {
                        imageRecycler.setAdapter(new picture_Adapter(inMale, ImageDisplay.this, this));
                    } else if (isContain(command, "outdoor") && isContain(command, "man")) {
                        imageRecycler.setAdapter(new picture_Adapter(outMale, ImageDisplay.this, this));
                    } else if (isContain(command, "indoor") && isContain(command, "woman")) {
                        imageRecycler.setAdapter(new picture_Adapter(inFemale, ImageDisplay.this, this));
                    } else if (isContain(command, "outdoor") && isContain(command, "woman")) {
                        imageRecycler.setAdapter(new picture_Adapter(outFemale, ImageDisplay.this, this));
                    } else if (isContain(command, "man")) {
                        imageRecycler.setAdapter(new picture_Adapter(male, ImageDisplay.this, this));
                    } else if (isContain(command, "woman")) {
                        imageRecycler.setAdapter(new picture_Adapter(female, ImageDisplay.this, this));
                    } else if (isContain(command, "indoor")) {
                        imageRecycler.setAdapter(new picture_Adapter(indoor, ImageDisplay.this, this));
                    } else if (isContain(command, "outdoor")) {
                        imageRecycler.setAdapter(new picture_Adapter(outdoor, ImageDisplay.this, this));
                    } else {
                        imageRecycler.setAdapter(new picture_Adapter(allpictures, ImageDisplay.this, this));
                    }
                    load.setVisibility(View.GONE);
                }
            }
            break;
        }
    }

    /**
     * @param holder   The ViewHolder for the clicked picture
     * @param position The position in the grid of the picture that was clicked
     * @param pics     An ArrayList of all the items in the Adapter
     */
    @Override
    public void onPicClicked(PicHolder holder, int position, ArrayList<pictureFacer> pics) {
        pictureBrowserFragment browser = pictureBrowserFragment.newInstance(pics, position, ImageDisplay.this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            browser.setEnterTransition(new Fade());
            browser.setExitTransition(new Fade());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(holder.picture, position + "picture")
                .add(R.id.displayContainer, browser)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onPicClicked(String pictureFolderPath, String folderName) {

    }


}
