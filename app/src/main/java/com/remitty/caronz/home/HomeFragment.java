package com.remitty.caronz.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.remitty.caronz.R;
import com.remitty.caronz.Search.FragmentSearch;
import com.remitty.caronz.Search.HireSearchMapFragment;
import com.remitty.caronz.avis.AvisFragment;
import com.remitty.caronz.utills.SettingsMain;


public class HomeFragment extends Fragment {

    LinearLayout buyLayout, rentLayout, hireLayout;
    TextView tvGreeting;

    public String method="";
    private SettingsMain settingsMain;


    public HomeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        getActivity().setTitle(R.string.app_name);

        settingsMain = new SettingsMain(getActivity());

        buyLayout = view.findViewById(R.id.buy_layout);
        rentLayout = view.findViewById(R.id.rent_layout);
        hireLayout = view.findViewById(R.id.hire_layout);
        tvGreeting = view.findViewById(R.id.tv_greeting);


        buyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentAllCategories fragment_cat = new FragmentAllCategories();
                Bundle bundle = new Bundle();
                bundle.putString("method", "buy");
                fragment_cat.setArguments(bundle);
                replaceFragment(fragment_cat, "FragmentAllCategories");
            }
        });

        rentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvisFragment fragment_cat = new AvisFragment();
                replaceFragment(fragment_cat, "AvisFragment");
            }
        });

        hireLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HireSearchMapFragment fragment_cat = new HireSearchMapFragment();
                Bundle bundle = new Bundle();
                bundle.putString("catId", "0");
                fragment_cat.setArguments(bundle);
                replaceFragment(fragment_cat, "HireSearchMapFragment");
            }
        });

        tvGreeting.setText("Good morning " + settingsMain.getUserFirstName());

        return view;
    }

    void replaceFragment(Fragment someFragment, String tag) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.right_enter, R.anim.left_out, R.anim.left_enter, R.anim.right_out);
        transaction.replace(R.id.frameContainer, someFragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }
}
