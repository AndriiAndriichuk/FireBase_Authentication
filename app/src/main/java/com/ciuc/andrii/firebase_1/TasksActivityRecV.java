package com.ciuc.andrii.firebase_1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TasksActivityRecV extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference DBRef;
    private FirebaseUser mUser = mAuth.getInstance().getCurrentUser();
    private EditText edit_new;
    private Button btn_new_task;
    RecyclerView recyclerView;

    private static class TaskViewHolder extends RecyclerView.ViewHolder{
        TextView text_title;
        Button btn_del_task;
        public TaskViewHolder(View itemView) {
            super(itemView);
            text_title = itemView.findViewById(R.id.text_title);
            btn_del_task = itemView.findViewById(R.id.btn_del);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_recv);

        edit_new = findViewById(R.id.edit_new_task);
        btn_new_task = findViewById(R.id.btn_add_ntask);

        DBRef = FirebaseDatabase.getInstance().getReference();

        btn_new_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBRef.child(mUser.getUid()).child("Tasks").push().setValue(edit_new.getText().toString());
                edit_new.setText("");
            }
        });

        recyclerView = findViewById(R.id.recv_tasks);

        FirebaseRecyclerAdapter<String,TaskViewHolder> adapter;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);


        adapter = new FirebaseRecyclerAdapter<String, TaskViewHolder>(String.class,R.layout.list_item,TaskViewHolder.class,DBRef.child(mUser.getUid()).child("Tasks")) {
            @Override
            protected void populateViewHolder(TaskViewHolder viewHolder, String title, final int position) {
                viewHolder.text_title.setText(title);
                viewHolder.btn_del_task.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference itemRef = getRef(position);
                        itemRef.removeValue();
                    }
                });

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(new Intent(TasksActivityRecV.this,Storage.class).putExtra("Reference",getRef(position).getKey().toString()));
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);

    }


}
