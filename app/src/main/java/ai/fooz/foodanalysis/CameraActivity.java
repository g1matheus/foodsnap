package ai.fooz.foodanalysis;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ai.fooz.foodanalysis.env.Classifier;
import ai.fooz.foodanalysis.env.MyUtility;
import ai.fooz.foodanalysis.env.TensorFlowImageClassifier;
import ai.fooz.models.Prediction;
import ai.fooz.models.RefImage;

public class CameraActivity extends AppCompatActivity {

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "FoodAnalysisImages";

    public Uri fileUri; // file url to store image/video
    public RefImage existingImg;

    private ImageView imgPreview;
    private VideoView videoPreview;
    private TextView itemCals, itemCarbs, itemFats, itemProts;

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";
    private static final String NUTRI_STATS_FILE = "file:///android_asset/nutrition_stats.txt";

    private Classifier classifier;
    private PieChart pieChart;

    private Executor executor = Executors.newSingleThreadExecutor();

//    Recycler view
    RecyclerView recyclerView;
    ArrayList<String> Number;
    RecyclerView.LayoutManager RecyclerViewLayoutManager;
    RecyclerViewAdapter RecyclerViewHorizontalAdapter;
    LinearLayoutManager HorizontalLayout ;
    View ChildView ;
    int RecyclerViewItemPosition ;
    List<Classifier.Recognition> predResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        itemCals = (TextView) findViewById(R.id.itemCal);
        itemCarbs = (TextView) findViewById(R.id.itemCarbs);
        itemFats = (TextView) findViewById(R.id.itemFats);
        itemProts = (TextView) findViewById(R.id.itemProts);


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        initTensorFlowAndLoadModel();

        Long eId = getIntent().getLongExtra("REF_DATA_ID", -99);
        existingImg = RefImage.findById(RefImage.class, eId);

        if (existingImg != null)
            setupExistingData(existingImg);
        else {
            captureImage();
        }
    }

    public void setupExistingData(RefImage eimg) {
        try {
            imgPreview.setVisibility(View.VISIBLE);
            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(Uri.parse(existingImg.name).getPath(),
                    options);
            imgPreview.setImageBitmap(bitmap);
            setupItemValues(eimg.getPredictions());
            setupRecyclerViewForPredictions(eimg.getPredictions());
           // setPieChart();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /*
     * Capturing Camera Image will lauch camera app requrest image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Receiving activity result method will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /*
     * Display image from a path to ImageView
     */
    private void previewCapturedImage() {
        try {
            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);
            classifyImage(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void classifyImage(Bitmap bitmap){

        Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        predResults = classifier.recognizeImage(croppedBitmap);
        setNutrients(predResults);
    }

    public void setNutrients(List results) {
        String actualFilename = NUTRI_STATS_FILE.split("file:///android_asset/")[1];
//        Log.i(TAG, "Reading labels from: " + actualFilename);
        BufferedReader br = null;
        try {
            JSONArray jsonArray;
            br = new BufferedReader(new InputStreamReader(getAssets().open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                Log.d("Nutri Stats", line);
                jsonArray = new JSONArray(line);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject explrObject = jsonArray.getJSONObject(i);
//                    Log.d("Results: ", results.get(0)["title"]);
                    if(results.get(0).toString().toLowerCase().contains(explrObject.getString("item").toLowerCase())){
                        String values = explrObject.getString("item")+"\n\nOn Serving: "+explrObject.getString("serving")+"\nCalories: " + explrObject.getString("calories")+"\nCarbs: "+explrObject.getString("carbs")+"\nProtein: "+explrObject.getString("protein")+"\nFats: "+explrObject.getString("fat");
//                        nutriStats.setText(values);
                        break;
                    }
                }
            }
            br.close();

          //  setPieChart();
            setupItemValues(results);
            setupRecyclerViewForPredictions(results);
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!" , e);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setPieChart(){
        pieChart.setUsePercentValues(true);
        ArrayList<Entry> yvalues = new ArrayList<Entry>();
        yvalues.add(new Entry(30f, 0));
        yvalues.add(new Entry(15f, 1));
        yvalues.add(new Entry(55f, 2));

        PieDataSet dataSet = new PieDataSet(yvalues, "");
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("Carbs");
        xVals.add("Fat");
        xVals.add("Protein");

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.DKGRAY);

        pieChart.animateXY(1400, 1400);
        pieChart.setDescription("");
        pieChart.setDrawHoleEnabled(true);
        pieChart.setTransparentCircleRadius(30f);
        pieChart.setHoleRadius(30f);
        pieChart.setData(data);
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier =
                            TensorFlowImageClassifier.create(
                                    getAssets(),
                                    MODEL_FILE,
                                    LABEL_FILE,
                                    INPUT_SIZE,
                                    IMAGE_MEAN,
                                    IMAGE_STD,
                                    INPUT_NAME,
                                    OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }


    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }



//    ======================================================

    public void setupRecyclerViewForPredictions(List results) {
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview1);

        RecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(RecyclerViewLayoutManager);

        // Adding items to RecyclerView.
        AddItemsToRecyclerViewArrayList(results);

        RecyclerViewHorizontalAdapter = new RecyclerViewAdapter(Number,CameraActivity.this,recyclerView);

        HorizontalLayout = new LinearLayoutManager(CameraActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);

        recyclerView.setAdapter(RecyclerViewHorizontalAdapter);


        // Adding on item click listener to RecyclerView.
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            GestureDetector gestureDetector = new GestureDetector(CameraActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {

                    return true;
                }

            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

                ChildView = Recyclerview.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                if(ChildView != null && gestureDetector.onTouchEvent(motionEvent)) {

                    //Getting clicked value.
                    RecyclerViewItemPosition = Recyclerview.getChildAdapterPosition(ChildView);

                    // Showing clicked item value on screen using toast message.
                    Toast.makeText(CameraActivity.this, Number.get(RecyclerViewItemPosition), Toast.LENGTH_LONG).show();

                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

    }

    // function to add items in RecyclerView.
    public void AddItemsToRecyclerViewArrayList(List<Prediction> results){

        Number = new ArrayList<>();

        if (existingImg != null) {
            for (int i=0; i<results.size(); i++) {
                Prediction val = results.get(i);
                Number.add(val.title);
            }

        } else if(predResults.size() > 0) {
            RefImage refImage = new RefImage();
            refImage.name = fileUri.toString();
            String sd = getIntent().getStringExtra("selectedDate");
            if(sd != null)
                refImage.timestamp = sd;
            else
                refImage.timestamp = MyUtility.getDateWithFormat(Calendar.getInstance().getTime(), "yyyy-MM-dd");
            refImage.save();

            for (int i=0; i<predResults.size(); i++) {
                Classifier.Recognition val = predResults.get(i);

                Prediction pred = new Prediction();
                pred.title = val.getTitle();
                pred.confidence = val.getConfidence().toString();
                pred.calories = Integer.parseInt(itemCals.getText().toString().replaceAll("[^0-9]", ""));
                pred.carbs = Integer.parseInt(itemCarbs.getText().toString().replaceAll("[^0-9]", ""));
                pred.fats = Integer.parseInt(itemFats.getText().toString().replaceAll("[^0-9]", ""));
                pred.proteins = Integer.parseInt(itemProts.getText().toString().replaceAll("[^0-9]", ""));
                pred.refimage = refImage;
                pred.save();

                Number.add(val.getTitle());
            }
        }
    }

    public void backPressed(View view) {
        finish();
    }

    public void setupItemValues(List<Prediction> results) {

        if (existingImg != null) {
            for (int i=0; i<1; i++) {
                Prediction val = results.get(i);
                itemCals.setText(String.valueOf(val.calories));
                itemCarbs.setText(String.valueOf(val.carbs)+" gms");
                itemFats.setText(String.valueOf(val.fats)+" gms");
                itemProts.setText(String.valueOf(val.proteins)+" gms");
            }
        } else {
            itemCals.setText(String.valueOf(MyUtility.getRandomNumber(50, 300)));
            itemCarbs.setText(String.valueOf(MyUtility.getRandomNumber(30, 300))+" gms");
            itemFats.setText(String.valueOf(MyUtility.getRandomNumber(5, 60))+" gms");
            itemProts.setText(String.valueOf(MyUtility.getRandomNumber(5, 30))+" gms");
        }
    }
}
