package com.samulitfirstproject.speedbazar;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Comments extends AppCompatActivity {

    private String comment_post_key,current_user_id;
    private EditText writeComment;
    private ImageView sendComent,backspace;
    private RecyclerView crecyclerView;
    private FirebaseAuth mfirebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference Comment,UserRef;
    private ArrayList<CommentInfo> cList;
    private CommentAdapter cAdapter;
    private SwipeRefreshLayout swipe;
    private static int REFRESH = 700;
    private TextView emptytext;
    private ImageView emptyimg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_comments);

        writeComment = findViewById(R.id.write_comment);
        sendComent = findViewById(R.id.send_comment);
        crecyclerView = findViewById(R.id.comment_recycler);

        emptytext = findViewById(R.id.empty_comment);
        emptyimg = findViewById(R.id.empty_img);

        backspace = findViewById(R.id.backpress_cmnt);
        swipe =findViewById(R.id.cswipe);

        crecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        //linearLayoutManager.setStackFromEnd(true);
        crecyclerView.setLayoutManager(linearLayoutManager);

        cList = new ArrayList<>();

        cAdapter = new CommentAdapter(this, cList);

        crecyclerView.setAdapter(cAdapter);

        mfirebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        current_user_id = mfirebaseAuth.getCurrentUser().getUid();

        comment_post_key = getIntent().getExtras().get("comment_key").toString();

        Comment = firebaseDatabase.getReference().child("Comment").child(comment_post_key);
        UserRef = firebaseDatabase.getReference().child("UsersData");

        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Comment.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()){

                                    cList.clear();
                                    cAdapter.notifyDataSetChanged();

                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                        CommentInfo posts = postSnapshot.getValue(CommentInfo.class);
                                        posts.setKey(postSnapshot.getKey());
                                        cList.add(posts);
                                    }
                                    cAdapter.notifyDataSetChanged();

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(Comments.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                            }

                        });
                        swipe.setRefreshing(false);
                    }
                },REFRESH);
            }
        });

        sendComent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(writeComment.getWindowToken(), 0);

                emptyimg.setVisibility(View.GONE);
                emptytext.setVisibility(View.GONE);

                UserRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            String name = dataSnapshot.child("userName").getValue(String.class);

                            setComment(name);
                            writeComment.setText("");


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });



        Comment.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    cList.clear();
                    cAdapter.notifyDataSetChanged();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        CommentInfo posts = postSnapshot.getValue(CommentInfo.class);
                        posts.setKey(postSnapshot.getKey());
                        cList.add(posts);
                    }
                    cAdapter.notifyDataSetChanged();

                }
                else {
                    emptyimg.setVisibility(View.VISIBLE);
                    emptytext.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Comments.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }

        });




    }

    private void setComment(String name) {

        String commentText = writeComment.getText().toString().trim();

        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "write comment first!", Toast.LENGTH_SHORT).show();
        }
        else {

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("d MMMM yyyy, hh:mm aa");
            String date = currentDate.format(calFordDate.getTime());

            String saveCurrentDate = String.valueOf(getDateInMillis(date));

            HashMap comment = new HashMap();
            comment.put("name",name);
            comment.put("comment",commentText);
            comment.put("date",saveCurrentDate);
            comment.put("uid",current_user_id);
            comment.put("commentPostKey",comment_post_key);

            final String commentKey = Comment.push().getKey();

            Comment.child(commentKey).updateChildren(comment)
                    .addOnCompleteListener(new OnCompleteListener() {

                        @Override
                        public void onComplete(@NonNull Task task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(Comments.this, "Comment Added", Toast.LENGTH_SHORT).show();
                                }
                            else {
                                    Toast.makeText(Comments.this, "something went wrong. try again!", Toast.LENGTH_SHORT).show();
                                }


                        }});

                    }


    }


    public static long getDateInMillis(String srcDate) {
        SimpleDateFormat desiredFormat = new SimpleDateFormat(
                "d MMMM yyyy, hh:mm aa");

        long dateInMillis = 0;
        try {
            Date date = desiredFormat.parse(srcDate);
            dateInMillis = date.getTime();
            return dateInMillis;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
