package com.ciuc.andrii.firebase_1;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TasksActivityListV extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference DBRef;
    private List<String> Task_list = new ArrayList<>();
    private FirebaseUser mUser = mAuth.getInstance().getCurrentUser();
    private FirebaseListAdapter mFBAdapter;
    private EditText edit_new;
    private Button btn_new_task;
    private ListView listUserTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_listv);

        edit_new = findViewById(R.id.edit_new);
        btn_new_task = findViewById(R.id.btn_add_task);

        listUserTasks = findViewById(R.id.user_tasks);

        DBRef = FirebaseDatabase.getInstance().getReference();


        mFBAdapter = new FirebaseListAdapter<String>(this,String.class,android.R.layout.simple_list_item_1,DBRef.child(mUser.getUid()).child("Tasks")){
            @Override
            protected void populateView(@NonNull View v, @NonNull String str, int position) {
                TextView text = v.findViewById(android.R.id.text1);
                text.setText(str);
            }
        };
        listUserTasks.setAdapter(mFBAdapter);

        /*DBRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String,String>> gti = new GenericTypeIndicator<HashMap<String,String>>() {};
                HashMap<String ,String> res = dataSnapshot.child("Tasks").getValue(gti);
                Task_list.clear();
                Task_list.addAll(res.values());
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //якщо помилка

            }
        });*/

        btn_new_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBRef.child(mUser.getUid()).child("Tasks").push().setValue(edit_new.getText().toString());
                edit_new.setText("");
            }
        });
    }



    private void updateUI() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Task_list);
        listUserTasks.setAdapter(adapter);
    }
}
