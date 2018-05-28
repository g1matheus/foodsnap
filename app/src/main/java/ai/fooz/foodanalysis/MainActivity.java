package ai.fooz.foodanalysis;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

import ai.fooz.models.Prediction;
import ai.fooz.models.RefImage;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<RefImage> refImagesList = RefImage.listAll(RefImage.class);
        for (int i=0; i<refImagesList.size(); i++) {
            RefImage img = refImagesList.get(i);
            List<Prediction> preds = img.getPredictions();
            Toast.makeText(getApplicationContext(),
                    "test", Toast.LENGTH_LONG)
                    .show();
            img.delete();
        }

        setupListView();
        callCameraActivity();
    }

    public void setupListView() {

    }

    public void callCameraActivity() {
        Intent i = new Intent(this, CameraActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
//        finish();
    }

}
