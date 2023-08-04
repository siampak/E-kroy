package com.samulitfirstproject.speedbazar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.samulitfirstproject.speedbazar.adapter.TopAdapter;
import com.samulitfirstproject.speedbazar.adapter.VendorListAdapter;
import com.samulitfirstproject.speedbazar.model.ProductInfo;
import com.samulitfirstproject.speedbazar.model.VendorInfo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class VendorList extends AppCompatActivity {

    private RecyclerView vRecyclerView;
    private DatabaseReference uRef,topVendorRef;
    private ImageView back;
    private TextView empty,tv_vendor;
    private VendorListAdapter vAdapter;
    private ArrayList<VendorInfo> vList;
    private String type;
    private EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_vendor_list);

        empty = findViewById(R.id.empty_vendor_list);
        tv_vendor = findViewById(R.id.tv_vendor_list);

        back = findViewById(R.id.back);
        search = findViewById(R.id.search_field);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        type = getIntent().getStringExtra("type");

        tv_vendor.setText(type+" List");

        vRecyclerView = findViewById(R.id.vendor_list_rv);
        vRecyclerView.setHasFixedSize(true);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        uRef = FirebaseDatabase.getInstance().getReference("UsersData");
        topVendorRef = FirebaseDatabase.getInstance().getReference("TopVendor");

        vList = new ArrayList<>();
        vAdapter = new VendorListAdapter(this, vList);
        vRecyclerView.setAdapter(vAdapter);

        search.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable mEdit)
            {

                if(search.getText().toString().trim().length()>0){


                    String text = search.getText().toString().trim();

                    text = text.substring(0,1).toUpperCase() + text.substring(1).toLowerCase();

                    Query query = uRef.orderByChild("userName").startAt(text).endAt(text+"\uf8ff");

                    getList(query);


                }else {
                    Query query=uRef.orderByChild("userType");
                    getList(query);
                }



            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        Query query=uRef.orderByChild("userType");
        getList(query);

    }

    private void getList(Query query) {


        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                vList.clear();
                vAdapter.notifyDataSetChanged();

                if(dataSnapshot.exists()){

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        String temp = String.valueOf(dataSnapshot.child(postSnapshot.getKey()).child("userType").getValue());

                        if (temp.equals(type)){
                            VendorInfo info = postSnapshot.getValue(VendorInfo.class);
                            info.setVendorKey(postSnapshot.getKey());
                            vList.add(info);
                        }


                    }
                    vAdapter.notifyDataSetChanged();

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(VendorList.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }



}