package com.remitty.caronz.avis;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.remitty.caronz.R;
import com.remitty.caronz.Search.FragmentSearch;
import com.remitty.caronz.Search.HireSearchMapFragment;
import com.remitty.caronz.home.HomeActivity;
import com.remitty.caronz.home.HomeFragment;


public class AvisFragment extends Fragment {

    LinearLayout ownerLayout, avisLayout, budgetLayout, paylessLayout;

    public String method="";
    private ImageButton btnBack;


    public AvisFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AvisFragment newInstance() {
        AvisFragment fragment = new AvisFragment();
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
        View view = inflater.inflate(R.layout.fragment_avis, container, false);

        getActivity().setTitle("Rent");

        ownerLayout = view.findViewById(R.id.owner_layout);
        avisLayout = view.findViewById(R.id.avis_layout);
        paylessLayout = view.findViewById(R.id.payless_layout);
        budgetLayout = view.findViewById(R.id.budget_layout);


        ownerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FragmentSearch fragment_cat = new FragmentSearch();
//                Bundle bundle = new Bundle();
//                bundle.putString("method", "rent");
//                bundle.putString("catId", "0");
//                fragment_cat.setArguments(bundle);
//                replaceFragment(fragment_cat, "FragmentSearch");
                ((HomeActivity)getActivity()).method = "rent";
                ((HomeActivity)getActivity()).moveFindNavigation();
            }
        });

        avisLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AvisPickupActivity.class);
                intent.putExtra("brand", "Avis");
                startActivity(intent);
            }
        });

        paylessLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AvisPickupActivity.class);
                intent.putExtra("brand", "Payless");
                startActivity(intent);
            }
        });

        budgetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AvisPickupActivity.class);
                intent.putExtra("brand", "Budget");
                startActivity(intent);
            }
        });

//        btnBack = view.findViewById(R.id.btn_back);
//        btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                HomeFragment fragment_home = new HomeFragment();
//                replaceFragment(fragment_home, "HomeFragment");
//            }
//        });

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
