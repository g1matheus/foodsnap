package ai.fooz.foodanalysis;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends RecyclerView.Adapter<FoodList.ViewHolder> {

    private FoodList context;
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> images = new ArrayList<String>();

    public FoodList(MainActivity mainActivity, ArrayList<String> titles, ArrayList<String> images) {
        this.context =  context;
        this.titles =  titles;
        this.images =  images;
    }

    @Override
    public FoodList.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_single, null);
        FoodList.ViewHolder rcv = new FoodList.ViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(FoodList.ViewHolder holder, final int i) {


    }

    @Override
    public int getItemCount() {
      //  return images.size();
        return 20;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivService;
        TextView tvService;
        ImageView ivRightback;

        public ViewHolder(View itemView) {
            super(itemView);

        }
    }
}
