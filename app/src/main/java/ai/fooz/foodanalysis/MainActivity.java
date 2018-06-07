package ai.fooz.foodanalysis;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.pm.PackageManager;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import ai.fooz.foodanalysis.env.MyUtility;
import ai.fooz.foodanalysis.env.RecyclerTouchListener;
import ai.fooz.models.Prediction;
import ai.fooz.models.RefImage;

public class MainActivity extends Activity {

    TextView activity_main_text_day_of_month;
    TextView activity_main_text_day_of_week;

    private List<FoodItem> foodList = new ArrayList<FoodItem>();
    private RecyclerView recyclerView;
    private FoodList mAdapter;

    private static final String LABEL_FILE = "file:///android_asset/labels.txt";
    ArrayList<Long> refImgIds = new ArrayList<Long>();

    String yearrr  = "";

    private Calendar mcalendar;
    private  int mYear; 
    private  int mMonth;
    private  int mDay;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();

        activity_main_text_day_of_month = (TextView)findViewById(R.id.activity_main_text_day_of_month);
        activity_main_text_day_of_week = (TextView)findViewById(R.id.activity_main_text_day_of_week);


        ImageView fab = (ImageView) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callCameraActivity();
            }
        });

        setupCalendar();
        setupRecyclerView();
    }


    @Override
    public void onResume(){
        super.onResume();
        setupListView();
    }

    public void setupCalendar() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy EEEE");
        //  tv_date_set.setText(format.format(cal.getTime()));

        String todayDate = format.format(cal.getTime());
        StringTokenizer tokenizer = new StringTokenizer(todayDate," ");
        String dateee = tokenizer.nextToken();
        String month = tokenizer.nextToken();
        yearrr = tokenizer.nextToken();
        String dayy = tokenizer.nextToken();

        activity_main_text_day_of_month.setText(dateee+" "+month);
        activity_main_text_day_of_week.setText(dayy+"");

        selectedDate = MyUtility.getDateWithFormat(cal.getTime(), "yyyy-MM-dd");
    }

    public void setupRecyclerView() {

        recyclerView = (RecyclerView) findViewById(R.id.list);
        mAdapter = new FoodList(foodList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                FoodItem fi = foodList.get(position);
                Toast.makeText(getApplicationContext(), fi.getName() + " is selected!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), CameraActivity.class);
                intent.putExtra("REF_DATA_ID", fi.getId());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    public void setupListView() {

        refImgIds.clear();
        foodList.clear();

//        List<RefImage> refImagesList = RefImage.find(RefImage.class, "timestamp = ?", selectedDate);
//        RefImage.executeQuery("VACUUM");
//        List<RefImage> refImagesList = RefImage.findWithQuery(RefImage.class, "SELECT * FROM RefImage where timestamp = ? ORDER BY id DESC", selectedDate);
//        List<RefImage> refImagesList = Select.from(RefImage.class).where(Condition.prop("timestamp").eq(selectedDate),.orderBy("id").list();

        List<RefImage> refImagesList = Select.from(RefImage.class)
                .where(Condition.prop("timestamp").eq(selectedDate))
                .orderBy("id DESC")
                .list();

        for (int i=0; i<refImagesList.size(); i++) {
            RefImage img = refImagesList.get(i);
            refImgIds.add(img.getId());
            List<Prediction> preds = img.getPredictions();
            Prediction pd = preds.get(0);
            FoodItem fi = new FoodItem();
            fi.setId(img.getId());
            fi.setName(MyUtility.toTitleCase(pd.title));
            fi.setImage(img.name);
            foodList.add(fi);
        }

        mAdapter.notifyDataSetChanged();
    }

    public void callCameraActivity() {
        Intent i = new Intent(this, CameraActivity.class);
        i.putExtra("selectedDate", selectedDate);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
//        finish();
    }

    public void nextDay(View view) {
        String dateSetStr = activity_main_text_day_of_month.getText().toString();
        String daySetStr = activity_main_text_day_of_week.getText().toString();

        String fulldate = dateSetStr+" "+" "+yearrr+" "+daySetStr;

        String DATE_FORMAT = "dd MMM yyyy EEEE";
        String date_string = fulldate;
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat(DATE_FORMAT);
        Date date = null;
        try {
            date = (Date) sdf.parse(date_string);
            Calendar c1 = Calendar.getInstance();
            c1.setTime(date);
            c1.add(Calendar.DATE, 1);
            String todayDate = sdf.format(c1.getTime());
            selectedDate = MyUtility.getDateWithFormat(c1.getTime(), "yyyy-MM-dd");
            StringTokenizer tokenizer = new StringTokenizer(todayDate," ");
            String dateee = tokenizer.nextToken();
            String month = tokenizer.nextToken();
            yearrr = tokenizer.nextToken();
            String dayy = tokenizer.nextToken();

            activity_main_text_day_of_month.setText(dateee+" "+month);
            activity_main_text_day_of_week.setText(dayy+"");

            setupListView();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    public void previousDay(View view) {

        String dateSetStr = activity_main_text_day_of_month.getText().toString();
        String daySetStr = activity_main_text_day_of_week.getText().toString();

        String fulldate = dateSetStr+" "+" "+yearrr+" "+daySetStr;

        String DATE_FORMAT = "dd MMM yyyy EEEE";

        String date_string = fulldate;
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat(DATE_FORMAT);
        Date date = null;
        try {
            date = (Date) sdf.parse(date_string);
            Calendar c1 = Calendar.getInstance();
            c1.setTime(date);
            c1.add(Calendar.DATE, -1);
            String todayDate = sdf.format(c1.getTime());
            selectedDate = MyUtility.getDateWithFormat(c1.getTime(), "yyyy-MM-dd");
            StringTokenizer tokenizer = new StringTokenizer(todayDate," ");
            String dateee = tokenizer.nextToken();
            String month = tokenizer.nextToken();
            yearrr = tokenizer.nextToken();
            String dayy = tokenizer.nextToken();

            activity_main_text_day_of_month.setText(dateee+" "+month);
            activity_main_text_day_of_week.setText(dayy+"");

            setupListView();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void openCalendar(View view) {

        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)


            {
                monthOfYear = monthOfYear + 1;

                String dtStart = dayOfMonth+"/"+monthOfYear+"/"+year;
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

                try {
                 Date   date = format.parse(dtStart);
                    format= new SimpleDateFormat("dd MMM yyyy EEEE");
                    String udatedate = format.format(date);
                    System.out.println("Date ->" + date);
                    StringTokenizer tokenizer = new StringTokenizer(udatedate," ");
                    String dateee = tokenizer.nextToken();
                    String month = tokenizer.nextToken();
                    yearrr = tokenizer.nextToken();
                    String dayy = tokenizer.nextToken();

                    activity_main_text_day_of_month.setText(dateee+" "+month);
                    activity_main_text_day_of_week.setText(dayy+"");
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }};
        DatePickerDialog dpDialog=new DatePickerDialog(this, listener, mYear, mMonth, mDay);
        dpDialog.show();

    }

    public void helpClicked(View view) {

        String actualFilename = LABEL_FILE.split("file:///android_asset/")[1];
        String lbls = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                lbls = lbls+"\n"+ MyUtility.toTitleCase(line);
            }
            br.close();

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("I can predict these items");
            alertDialog.setMessage(lbls);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!" , e);
        }
    }

    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
            }

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }
}
