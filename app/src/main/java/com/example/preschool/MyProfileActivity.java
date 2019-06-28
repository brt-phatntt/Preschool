package com.example.preschool;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userClass, userParentof, userBirthDay;
    private CircleImageView userProfileImage;

    private DatabaseReference UsersRef;
    private FirebaseUser firebaseUser;
    private FirebaseAuth mAuth;

    private String current_user_id, visitUserId, idClass, idTeacher, className;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addControlls();

        mAuth = FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        current_user_id = firebaseUser.getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myBirthday = dataSnapshot.child("birthday").getValue().toString();
                    String myClass = dataSnapshot.child("classname").getValue().toString();
                    String myParentOf = dataSnapshot.child("parentof").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.ic_person_black_50dp).into(userProfileImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userBirthDay.setText("Sinh nhật: " + myBirthday);
                    userClass.setText("Lớp: " + myClass);
                    userParentof.setText("Phụ huynh của bé: " + myParentOf);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_edit_profile:
            {
                Intent intent=new Intent(MyProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.action_change_pass:
                changePassWord();
                break;
        }
        return true;
    }

    private void changePassWord() {
        final Dialog dialogChangePassWord=new Dialog(MyProfileActivity.this);
        dialogChangePassWord.setContentView(R.layout.dialog_change_password);
//        dialogChangePassWord.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogChangePassWord.setCancelable(true);
        dialogChangePassWord.show();

        // Controlls
        final EditText oldPass=dialogChangePassWord.findViewById(R.id.old_pass);
        final EditText newPass=dialogChangePassWord.findViewById(R.id.new_pass);
        final EditText reNewPass=dialogChangePassWord.findViewById(R.id.renew_pass);
        Button btnChangePass=dialogChangePassWord.findViewById(R.id.btn_change_pass);


        // Verify Password
        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(passWordVerify(oldPass.getText().toString(),newPass.getText().toString(),reNewPass.getText().toString())){
                    mAuth.getCurrentUser().updatePassword(newPass.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
//                                        Log.d(TAG, "User password updated.");
                                        dialogChangePassWord.dismiss();
                                        Toast.makeText(MyProfileActivity.this,"Đổi mật khẩu thành công",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                }
                dialogChangePassWord.dismiss();
            }

            private boolean passWordVerify(String oldPass, String newPass, String reNewPass) {
                if(oldPass.length()>=6){
                    // Test login to check right Password
                    if(loginTest(oldPass)){
                        // Check new pass
                        if(newPass.length()>=6){
                            if(newPass.equals(reNewPass)){
                                return true;
                            }
                        }
                    }
                    else
                    Toast.makeText(MyProfileActivity.this,"Sai mật khẩu",Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

    }
    private String getMyEmail(){
        return firebaseUser.getEmail();
    }
    private Boolean loginTest(String pass){
//        mAuth.signInWithEmailAndPassword(getMyEmail(),pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//
//            }
//        });
        return false;
    }

    private void addControlls() {
        userProfileImage = findViewById(R.id.person_profile_pic);
        userName = findViewById(R.id.person_username);
        userProfName = findViewById(R.id.person_full_name);
        userParentof = findViewById(R.id.relationship_with_children);
        userBirthDay = findViewById(R.id.person_birthday);
        userClass = findViewById(R.id.person_class);
    }

}
