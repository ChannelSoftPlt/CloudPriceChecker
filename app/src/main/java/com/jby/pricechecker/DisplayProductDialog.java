package com.jby.pricechecker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.jby.pricechecker.others.MySingleton;
import com.jby.pricechecker.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.jby.pricechecker.shareObject.CustomToast.CustomToast;

public class DisplayProductDialog extends DialogFragment implements TextWatcher {
    View rootView;
    private ImageView displayProductDialogProductPicture;
    private TextView displayProductDialogProductBarcode, displayProductDialogProductCode, displayProductDialogProductPrice;
    private TextView displayProductDialogProductDescription, displayProductDialogProductRemarkPromotion;
    private ProgressBar displayProductDialogProductProgressBar;
    private LinearLayout displayProductDialogProductParentLayout;
    private EditText displayProductDialogProductScanResult;

    private String scanResult;
    public DisplayProductDialogCallBack displayProductDialogCallBack;

    private Handler handler;

    public DisplayProductDialog() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.display_product_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        displayProductDialogProductPicture = rootView.findViewById(R.id.display_product_dialog_image);

        displayProductDialogProductBarcode = rootView.findViewById(R.id.display_product_dialog_barcode);
        displayProductDialogProductDescription = rootView.findViewById(R.id.display_product_dialog_description_detail);
        displayProductDialogProductCode = rootView.findViewById(R.id.display_product_dialog_item_code);
        displayProductDialogProductPrice = rootView.findViewById(R.id.display_product_dialog_price);
        displayProductDialogProductRemarkPromotion = rootView.findViewById(R.id.display_product_dialog_remark_promotion);

        displayProductDialogProductScanResult = rootView.findViewById(R.id.display_product_dialog_scan_result);

        displayProductDialogProductProgressBar = rootView.findViewById(R.id.display_product_progress_bar);
        displayProductDialogProductParentLayout = rootView.findViewById(R.id.display_product_dialog_parent_layout);

        handler = new Handler();
        displayProductDialogCallBack = (DisplayProductDialogCallBack) getActivity();
    }

    private void objectSetting() {
        displayProductDialogProductScanResult.addTextChangedListener(this);

        final Bundle bundle = getArguments();
        new Thread(new Runnable() {
            @Override
            public void run() {
                scanResult = bundle.getString("item_barcode");
                fetchProductDetail(scanResult);
            }
        }).start();
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
        displayProductDialogCallBack.onResume();
        super.onDismiss(dialog);
    }

    public void fetchProductDetail(final String barcode) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, SharedPreferenceManager.getAPI(getActivity()) + "?item_barcode=" + barcode, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    switch (status) {
                        case "1":
                            setUpProductView(jsonObject);
                            break;
                        case "2":
                            CustomToast(getActivity(), "Item Not Found!");
                            dismiss();
                            break;
                        case "3":
                            CustomToast(getActivity(), "Something wrong with your api setup!");
                            dismiss();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //close progress bar and display inform
                closeProgressBar();
                //close dialog
                closeDialogAfter5Second();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomToast(getActivity(), "Unable Connect to Api!");
                dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
//                params.put("item_barcode", barcode);
                return params;
            }
        };
        MySingleton.getmInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void setUpProductView(final JSONObject jsonObject) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("item_detail");
                        displayProductDialogProductBarcode.setText(String.format("Barcode: %s", jsonArray.getJSONObject(0).getString("item_barcode")));
                        displayProductDialogProductPrice.setText(String.format("RM %s", jsonArray.getJSONObject(0).getString("item_price")));
                        displayProductDialogProductDescription.setText(jsonArray.getJSONObject(0).getString("item_description"));
                        displayProductDialogProductCode.setText(String.format("SKU: %s", jsonArray.getJSONObject(0).getString("item_code")));
                        displayProductDialogProductRemarkPromotion.setText(jsonArray.getJSONObject(0).getString("remark_promotion"));
                        String image = jsonArray.getJSONObject(0).getString("item_picture");

                        if (image.length() > 0) {
                            Glide.with(getActivity())
                                    .load(image)
                                    .into(displayProductDialogProductPicture);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void closeDialogAfter5Second() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, 30000);
    }

    private void closeProgressBar() {
        displayProductDialogProductProgressBar.setVisibility(View.GONE);
        displayProductDialogProductParentLayout.setVisibility(View.VISIBLE);
        displayProductDialogProductScanResult.setVisibility(View.VISIBLE);
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }


    @Override
    public void afterTextChanged(Editable editable) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!displayProductDialogProductScanResult.getText().toString().equals("")) {
                    displayProductDialogProductScanResult.setEnabled(false);
                    //if the scan result in this dialog not same with previous one then close this dialog and reopen again
                    if (!displayProductDialogProductScanResult.getText().toString().equals(scanResult)) {
                        dismiss();
                        displayProductDialogCallBack.openProductDialog(displayProductDialogProductScanResult.getText().toString());
                    } else {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                displayProductDialogProductScanResult.setEnabled(true);
                                displayProductDialogProductScanResult.requestFocus();
                            }
                        }, 2000);
                    }
                }
                displayProductDialogProductScanResult.setText("");
            }
        }, 600);
    }

    public interface DisplayProductDialogCallBack {
        void onResume();

        void openProductDialog(String barcode);
    }

}