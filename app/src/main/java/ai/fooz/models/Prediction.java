package ai.fooz.models;

import com.orm.SugarRecord;

import java.util.Date;

public class Prediction extends SugarRecord {
    public String title;
    public String confidence;
    public RefImage refimage;

    public Prediction() {

    }
}
