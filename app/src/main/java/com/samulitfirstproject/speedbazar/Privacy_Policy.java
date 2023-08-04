package com.samulitfirstproject.speedbazar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Privacy_Policy extends AppCompatActivity {

    private TextView EditText, edit;
    private LinearLayout linearLayout;
    private ImageView back;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(), databaseReference2;

    private FirebaseUser mFirebaseuser;
    private FirebaseAuth mfirebaseAuth;
    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_privacy__policy);

        EditText = findViewById(R.id.textView3);
        edit = findViewById(R.id.tv_edit);
        linearLayout = findViewById(R.id.linearLayout);
        back = findViewById(R.id.back);

        databaseReference2 = FirebaseDatabase.getInstance().getReference().child("Details").child("privacyPolicy");
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                EditText.setText(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        try {
            mfirebaseAuth = FirebaseAuth.getInstance();

            if (mfirebaseAuth != null) {
                current_user_id = mfirebaseAuth.getCurrentUser().getUid();
            } else {
                current_user_id = " ";
                //edit.setVisibility(View.INVISIBLE);
            }

            if (current_user_id.equals("D2sMhPLPzaOiY3z21F4tvP0JAih1")) {
                edit.setVisibility(View.VISIBLE);

                edit.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {

                        final EditText input = new EditText(Privacy_Policy.this);
                        input.setText("    " + EditText.getText());

                        AlertDialog dialog = new AlertDialog.Builder(Privacy_Policy.this)
                                .setMessage("Enter Text")
                                .setView(input)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String text = input.getText().toString().trim();

                                        if (text.isEmpty()) {
                                            Toast.makeText(Privacy_Policy.this, "Empty Field", Toast.LENGTH_SHORT).show();
                                        } else {
                                            databaseReference.child("Details").child("privacyPolicy").setValue(text);
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create();
                        dialog.show();

                    }
                });
            }
        }catch (Exception e){

        }
    }
}