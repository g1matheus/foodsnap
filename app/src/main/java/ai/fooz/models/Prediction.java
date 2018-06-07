package ai.fooz.models;

import com.orm.SugarRecord;

public class Prediction extends SugarRecord {
    public String title;
    public String confidence;
    public RefImage refimage;

    public Prediction() {

    }
}
