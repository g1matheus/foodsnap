package ai.fooz.foodanalysis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FoodList extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> title;
    private final ArrayList<String> imageUri;
    public FoodList(Activity context,
                    ArrayList title, ArrayList imageUri) {
        super(context, R.layout.list_single, title);
        this.context = context;
        this.title = title;
        this.imageUri = imageUri;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(title.get(position));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        final Bitmap bitmap = BitmapFactory.decodeFile(Uri.parse(imageUri.get(position)).getPath(),
                options);

        imageView.setImageBitmap(bitmap);
        return rowView;
    }
}