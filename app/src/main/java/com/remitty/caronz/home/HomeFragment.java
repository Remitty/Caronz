package com.remitty.caronz.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remitty.caronz.R;
import com.remitty.caronz.Search.FragmentSearch;
import com.remitty.caronz.Search.HireSearchMapFragment;
import com.remitty.caronz.avis.AvisFragment;
import com.remitty.caronz.utills.SettingsMain;

import java.util.Calendar;


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
                ((HomeActivity)getActivity()).replaceFragment(fragment_cat, "FragmentAllCategories");
                ((HomeActivity)getActivity()).changeToolbarTitle("Buy");
            }
        });

        rentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvisFragment fragment_cat = new AvisFragment();
                ((HomeActivity)getActivity()).replaceFragment(fragment_cat, "AvisFragment");
                ((HomeActivity)getActivity()).changeToolbarTitle("Rent");
            }
        });

        hireLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                HireSearchMapFragment fragment_cat = new HireSearchMapFragment();
//                Bundle bundle = new Bundle();
//                bundle.putString("catId", "0");
//                fragment_cat.setArguments(bundle);
//                replaceFragment(fragment_cat, "HireSearchMapFragment");
                FragmentAllCategories fragment_cat = new FragmentAllCategories();
                Bundle bundle = new Bundle();
                bundle.putString("method", "hire");
                fragment_cat.setArguments(bundle);
                ((HomeActivity)getActivity()).replaceFragment(fragment_cat, "FragmentAllCategories");
                ((HomeActivity)getActivity()).changeToolbarTitle("Hire");
            }
        });

        displayGreeting();

        return view;
    }

    private void displayGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            tvGreeting.setText("Good morning " + settingsMain.getUserFirstName());
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            tvGreeting.setText("Good Afternoon " + settingsMain.getUserFirstName());
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            tvGreeting.setText("Good Evening " + settingsMain.getUserFirstName());
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            tvGreeting.setText("Good Night " + settingsMain.getUserFirstName());
        }
    }

}
