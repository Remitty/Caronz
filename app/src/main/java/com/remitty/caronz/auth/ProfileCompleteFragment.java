package com.remitty.caronz.auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.remitty.caronz.R;

public class ProfileCompleteFragment extends Fragment {

    public ProfileCompleteFragment() {
        // Required empty public constructor
    }
    public static ProfileCompleteFragment newInstance(String param1, String param2) {
        ProfileCompleteFragment fragment = new ProfileCompleteFragment();
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
        return inflater.inflate(R.layout.content_profile_complete, container, false);
    }
}
