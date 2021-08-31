package com.remitty.caronz.home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.remitty.caronz.R;
import com.remitty.caronz.Search.FragmentSearch;


public class HomeFragment extends Fragment {

    LinearLayout buyLayout, rentLayout, hireLayout;

    public String method="";


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

        buyLayout = view.findViewById(R.id.buy_layout);
        rentLayout = view.findViewById(R.id.rent_layout);
        hireLayout = view.findViewById(R.id.hire_layout);

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
                FragmentSearch fragment_cat = new FragmentSearch();
                Bundle bundle = new Bundle();
                bundle.putString("method", "rent");
                bundle.putString("catId", "0");
                fragment_cat.setArguments(bundle);
                replaceFragment(fragment_cat, "FragmentSearch");
            }
        });

        hireLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentSearch fragment_cat = new FragmentSearch();
                Bundle bundle = new Bundle();
                bundle.putString("method", "hire");
                bundle.putString("catId", "0");
                fragment_cat.setArguments(bundle);
                replaceFragment(fragment_cat, "FragmentSearch");
            }
        });

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