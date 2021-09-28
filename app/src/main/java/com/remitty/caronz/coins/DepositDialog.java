package com.remitty.caronz.coins;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.WriterException;
import com.remitty.caronz.R;
import com.remitty.caronz.zxing.encoding.EncodingHandler;

import org.json.JSONObject;

public class DepositDialog extends DialogFragment {

    private Listener mListener;
    private int mView;
    private String mAddress, mqrCode, mCoinName;
    private ImageButton btnCopy;
    private TextView address, coinname;
    ImageView imgQrcode;
    Bitmap bmpQrcode;

    public DepositDialog() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public DepositDialog(int view, JSONObject address, String coinanme) {
        mView = view;
        mAddress = address.optString("address");
        mqrCode = address.optString("qrcode");
        mCoinName = coinanme;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(mView, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        btnCopy = view.findViewById(R.id.btn_copy);
        address = view.findViewById(R.id.tv_wallet_address);
        imgQrcode = view.findViewById(R.id.image_qrcode);
        coinname = view.findViewById(R.id.tv_coin_name);
        registerForContextMenu(address);

        try {
            if(!mqrCode.isEmpty()) {
                bmpQrcode = EncodingHandler.createQRCode(mqrCode, 400);
                imgQrcode.setImageBitmap(bmpQrcode);
            }
            else
                imgQrcode.setVisibility(View.GONE);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        address.setText(mAddress);
        coinname.setText(mCoinName);
        builder.setView(view);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", address.getText());
                manager.setPrimaryClip(clipData);
                mListener.onOk();
            }
        });
        return builder.create();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        // TODO: Update argument type and name
        void onOk();
    }
}
