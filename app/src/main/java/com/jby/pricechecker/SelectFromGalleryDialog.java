package com.jby.pricechecker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

public class SelectFromGalleryDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private TextView selectFromGalleryDialogVideo, selectFromGalleryDialogImage;
    public SelectFromGalleryDialogCallBack selectFromGalleryDialogCallBack;

    public SelectFromGalleryDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.select_from_gallery, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        selectFromGalleryDialogCallBack = (SelectFromGalleryDialogCallBack) getActivity();

        selectFromGalleryDialogImage = rootView.findViewById(R.id.select_from_gallery_picture);
        selectFromGalleryDialogVideo = rootView.findViewById(R.id.select_from_gallery_video);
    }
    private void objectSetting(){
        selectFromGalleryDialogImage.setOnClickListener(this);
        selectFromGalleryDialogVideo.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(d.getWindow()).setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.select_from_gallery_picture:
                selectFromGalleryDialogCallBack.selectFromGallery(2, 1);
                break;
            case R.id.select_from_gallery_video:
                selectFromGalleryDialogCallBack.selectFromGallery(3, 5);
                break;

        }
        dismiss();
    }

    public interface SelectFromGalleryDialogCallBack{
        void selectFromGallery(int type, int maxSelection);
    }
}