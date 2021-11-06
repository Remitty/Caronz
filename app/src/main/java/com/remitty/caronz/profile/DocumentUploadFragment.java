package com.remitty.caronz.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.remitty.caronz.R;

public class DocumentUploadFragment extends Fragment {

    public DocumentUploadFragment() {
        // Required empty public constructor
    }

    public static DocumentUploadFragment newInstance(String param1, String param2) {
        DocumentUploadFragment fragment = new DocumentUploadFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.content_document_upload, container, false);
    }
}
