package ai.fooz.foodanalysis;

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
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import ai.fooz.models.Prediction;
import ai.fooz.models.RefImage;

public class MainActivity extends Activity {

    TextView activity_main_text_day_of_month;
    TextView activity_main_text_day_of_week;

    RecyclerView list;
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";
    ArrayList<Long> refImgIds = new ArrayList<Long>();

    String yearrr  = "";

    private Calendar mcalendar;
    private  int mYear; 
    private  int mMonth;
    private  int mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main_text_day_of_month = (TextView)findViewById(R.id.activity_main_text_day_of_month);
        activity_main_text_day_of_week = (TextView)findViewById(R.id.activity_main_text_day_of_week);


        ImageView fab = (ImageView) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
//                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
//                startActivity(intent);
                callCameraActivity();
            }
        });

    }


    @Override
    public void onResume(){
        super.onResume();
        setupListView();

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
    }

    public void setupListView() {

        refImgIds.clear();

        List<RefImage> refImagesList = RefImage.listAll(RefImage.class);

        final ArrayList<String> titles = new ArrayList<String>();
        final ArrayList<String> images = new ArrayList<String>();

        for (int i=0; i<refImagesList.size(); i++) {
            RefImage img = refImagesList.get(i);
            refImgIds.add(img.getId());
            List<Prediction> preds = img.getPredictions();
            Prediction pd = preds.get(0);
            titles.add(pd.title);
            images.add(img.name);
        }





      //  FoodList adapter = new FoodList(MainActivity.this, titles, images);
        list=(RecyclerView) findViewById(R.id.list);
        list.setLayoutManager(new GridLayoutManager(this, 1, LinearLayoutManager.VERTICAL, false));
        list.setAdapter(new FoodList(this,titles ,images));
       /* list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(MainActivity.this, "You Clicked at " +titles.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), CameraActivity.class);
                intent.putExtra("REF_DATA_ID", refImgIds.get(position));
                startActivity(intent);
            }
        });
*/
    }

    public void callCameraActivity() {
        Intent i = new Intent(this, CameraActivity.class);
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
            StringTokenizer tokenizer = new StringTokenizer(todayDate," ");
            String dateee = tokenizer.nextToken();
            String month = tokenizer.nextToken();
            yearrr = tokenizer.nextToken();
            String dayy = tokenizer.nextToken();

            activity_main_text_day_of_month.setText(dateee+" "+month);
            activity_main_text_day_of_week.setText(dayy+"");




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
            StringTokenizer tokenizer = new StringTokenizer(todayDate," ");
            String dateee = tokenizer.nextToken();
            String month = tokenizer.nextToken();
            yearrr = tokenizer.nextToken();
            String dayy = tokenizer.nextToken();

            activity_main_text_day_of_month.setText(dateee+" "+month);
            activity_main_text_day_of_week.setText(dayy+"");




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
                lbls = lbls+"\n"+toTitleCase(line);
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

    public static String toTitleCase(String str) {

        if (str == null) {
            return null;
        }

        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }
}
