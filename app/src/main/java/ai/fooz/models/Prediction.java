package ai.fooz.models;

import com.orm.SugarRecord;

public class Prediction extends SugarRecord {
    public String title;
    public String confidence;
    public RefImage refImage;

    public Prediction() {

    }

    public Prediction(String title, String confidence, RefImage refImage) {
        this.title = title;
        this.confidence = confidence;
        this.refImage = refImage;
    }
}
