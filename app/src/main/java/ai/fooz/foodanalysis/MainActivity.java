package ai.fooz.foodanalysis;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ai.fooz.models.Prediction;
import ai.fooz.models.RefImage;

public class MainActivity extends AppCompatActivity {

    ListView list;
    ArrayList<Long> refImgIds = new ArrayList<Long>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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



        FoodList adapter = new
                FoodList(MainActivity.this, titles, images);
        list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
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

    }

    public void callCameraActivity() {
        Intent i = new Intent(this, CameraActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
//        finish();
    }

}
