package com.remitty.caronz.utills;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.plaid.link.Plaid;
import com.plaid.linkbase.models.configuration.LinkConfiguration;
import com.plaid.linkbase.models.configuration.PlaidEnvironment;
import com.plaid.linkbase.models.configuration.PlaidProduct;
import com.plaid.linkbase.models.connection.PlaidLinkResultHandler;
//import com.plaid.link.configuration.LinkConfiguration;
//import com.plaid.link.configuration.PlaidProduct;
//import com.plaid.link.result.PlaidLinkResultHandler;
//import com.plaid.link.configuration.LinkConfiguration;
////import com.plaid.link.configuration.LinkTokenConfiguration;
//import com.plaid.link.configuration.PlaidProduct;
//import com.plaid.link.result.PlaidLinkResultHandler;
//import com.plaid.linkbase.models.configuration.LinkConfiguration;
//import com.plaid.linkbase.models.configuration.PlaidProduct;
//import com.plaid.linkbase.models.connection.PlaidLinkResultHandler;

import java.util.ArrayList;
import java.util.Locale;

import kotlin.Unit;

public class PlaidConnect {
    private Activity mContext;
    private String linkToken="link-sandbox-754d35c8-24a2-47a8-b4fd-145a907bf3c9";

    PlaidConnect(Activity context){
        mContext = context;

        new PlaidLinkResultHandler(0,
                onSuccess ->{
                    Log.d("plaidsuccess", onSuccess.toString());
                    sendPublicToken(onSuccess.getPublicToken());
                    return Unit.INSTANCE;
                },
                onExit -> {
                    Log.d("plaidexit", onExit.toString());
                    return Unit.INSTANCE;
                },
                onCancelled -> {
                    Log.d("plaidcancel", onCancelled.toString());
                    return Unit.INSTANCE;
                }
        );

        getLinkTokenFromServer();
        setOptionalEventListener();
        openLink();

    }

    public static void initPlaid(Activity context){
        new PlaidConnect(context);
    }

    /**
     * Optional, set an <a href="https://plaid.com/docs/link/android/#handling-onevent">event listener</a>.
     */
    private void setOptionalEventListener() {
        Plaid.setLinkEventListener(linkEvent -> {
            Log.i("Plaid Event", linkEvent.toString());
            return Unit.INSTANCE;
        });
    }

    /**
     * For all Link configuration options, have a look at the
     * <a href="https://plaid.com/docs/link/android/#parameter-reference">parameter reference</>
     */
    private void openLink() {
        ArrayList<PlaidProduct> products = new ArrayList<>();
        products.add(PlaidProduct.TRANSACTIONS);

        ArrayList<String> countries = new ArrayList<>();
        countries.add("US");

        Plaid.openLink(
                mContext,
                new LinkConfiguration.Builder("clientName", products)
                        .webhook("https://xamoapp.com")
//                        .token(linkToken)
                        .countryCodes(countries)
                        .language(Locale.ENGLISH.getLanguage())
                        .environment(PlaidEnvironment.SANDBOX)
//                        .userEmailAddress("remittyinc@yahoo.com")
//                        .userLegalName("Brian Zifac")
//                        .userPhoneNumber("+15204063923")
                        .build(),
                0);
    }

    private void getLinkTokenFromServer() {
        Log.d("getlinktoken", "here");
    }

    private void sendPublicToken(String publicToken) {
        Log.d("publictoken", publicToken);
    }

    public void displayMessage(String toastString){
        Toast.makeText(mContext,toastString,Toast.LENGTH_SHORT).show();
    }

}
