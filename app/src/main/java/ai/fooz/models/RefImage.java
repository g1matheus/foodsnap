package ai.fooz.models;

import android.media.Image;

import com.orm.SugarRecord;

import java.util.List;

public class RefImage extends SugarRecord {
    public String name;

    public RefImage() {

    }

    public RefImage(String name) {
        this.name = name;
    }

    public List<Prediction> getPredictions(){
        return Prediction.find(Prediction.class, "image = ?", String.valueOf(this.getId()));
    }
}
