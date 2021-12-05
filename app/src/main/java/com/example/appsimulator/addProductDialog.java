package com.example.appsimulator;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class addProductDialog extends AppCompatDialogFragment {

    private EditText editTextItemName;
    private EditText editTextItemBrand;
    private EditText editTextItemPrice;
    private String sessionID;
    private Products p = new Products();

    public boolean isValid(String input, String type){
        //Consider the type to compare corresponding regex with
        boolean v = true;
        switch(type){
            case "itemName":
                if(input.isEmpty()){
                    displayError("Empty Item Name. Try Again", "itemName");
                    v = false;
                }
                break;
            case "brandName":
                if(input.isEmpty()){
                    displayError("Empty Brand Name. Try Again", "brandName");
                    v = false;
                }
                break;
            case "itemPrice":
                if(input.isEmpty()){
                    displayError("Empty Price. Try Again", "itemPrice");
                    v = false;
                }
                else if(!(Pattern.compile("(\\d+\\.\\d{1,2}|[1-9][0-9]*)")).matcher(input).matches()){ //a decimal number or whole number
                    displayError("Invalid Price. Try Again", "itemPrice");
                    v = false;
                }
                break;
        }
        return v;
    }

    public void displayError(String error, String type) {
        //displays the corresponding error based on type of info
        switch(type){
            case "itemName":
                editTextItemName.setError(error);
                editTextItemName.requestFocus();
                return;
            case "brandName":
                editTextItemBrand.setError(error);
                editTextItemBrand.requestFocus();
                return;
            case "itemPrice":
                editTextItemPrice.setError(error);
                editTextItemPrice.requestFocus();
                return;
        }
    }

    //adds Store's products to DB
    public void updateStoreProductDB(Stores store){

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("Users").child("Store Owner").child(sessionID).child("Store"); // path is hardcoded

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean storeExists = false;
                //code below checks for duplicates in stores, need to change it to check for dupes in products
//                for (DataSnapshot ds : snapshot.getChildren()){
//                    if (ds.getKey().equals(Integer.toString(store.getStoreID()))) { // store exists will output error
//                        storeExists = true;
//                        break;
//                    }
//                }
                for (Products p : store.getProducts())
                    ref.child(Integer.toString(p.hashCode())).setValue(p); // adds in database
                // section below is to portray error if store already in db
//                else {
//                    Toast.makeText(RegisterUser.this,"User Already Exists, Please Login",
//                            Toast.LENGTH_LONG).show();
//                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_product_dialog,null);

        //create a view with the layout add_product_dialog
        builder.setView(view).setTitle("Add a Product to Store").setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        })
        .setPositiveButton("ADD", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        editTextItemName = view.findViewById(R.id.add_item);
        editTextItemBrand = view.findViewById(R.id.add_brand);
        editTextItemPrice = view.findViewById(R.id.add_price);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null){
            Button pd = d.getButton(Dialog.BUTTON_POSITIVE);
            pd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String itemName = editTextItemName.getText().toString();
                    if(!isValid(itemName, "itemName")) return;

                    String brandName = editTextItemBrand.getText().toString();
                    if(!isValid(brandName, "brandName")) return;

                    String itemPrice = editTextItemPrice.getText().toString();
                    if(!(isValid(itemPrice, "itemPrice")))return;

                    p.setItem(itemName);
                    p.setBrand(brandName);
                    p.setPrice(itemPrice);
                    d.dismiss();

                    //add data to database
                    Stores s = new Stores(sessionID); // value is hard coded for testing
                    s.addProductStore(p);
                    updateStoreProductDB(s);
                }
            });
        }
    }

    public Products getProduct(){
        return p;
    }

    public void setSessionID(String sessionID){ this.sessionID = sessionID;}

}
