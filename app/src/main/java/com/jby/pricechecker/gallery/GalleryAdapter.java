package com.jby.pricechecker.gallery;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.pricechecker.R;
import com.jby.pricechecker.sharePreference.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;

public class GalleryAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> galleryList;

    public GalleryAdapter(Context context, ArrayList<String> galleryList)
    {
        this.context = context;
        this.galleryList = galleryList;
    }

    @Override
    public int getCount() {
        return galleryList.size();
    }

    @Override
    public String getItem(int i) {
        return galleryList.get(i);
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null){
            view = View.inflate(this.context, R.layout.activity_setting_gallery_list_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        String fileType = getFileType(getVideoName(galleryList.get(i)));
        if(fileType.equals(".mp4")) viewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.mp4));
        else viewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.picture));

        viewHolder.name.setText(getVideoName(galleryList.get(i)));

        return view;
    }

    private String getVideoName(String videoPath){
        String[] separatePath = videoPath.split("/");
        if(separatePath.length > 0)
            return separatePath[separatePath.length-1];
        else return  "null.mp4";
    }

    private String getFileType(String videoPath){
        return videoPath.substring(videoPath.length()-4,  videoPath.length());
    }

    private static class ViewHolder{
        private TextView name;
        private ImageView fileType;

        ViewHolder (View view){
            fileType = view.findViewById(R.id.file_type);
            name = view.findViewById(R.id.file_name);

        }
    }
}
