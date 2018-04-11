package com.example.milymozz.orderfood;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.milymozz.orderfood.Common.Common;
import com.example.milymozz.orderfood.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    private EditText edtPassword, edtPhone;
    private Button btnSignIn;
    private com.rey.material.widget.CheckBox ckbRemember;
    private TextView txtForgotPwd;

    private FirebaseDatabase database;
    private DatabaseReference table_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtPhone = (EditText) findViewById(R.id.edtPhone);

        txtForgotPwd = (TextView) findViewById(R.id.txtForgotPwd);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);

        ckbRemember = (com.rey.material.widget.CheckBox) findViewById(R.id.ckbRemember);

        //Init Paper
        Paper.init(this);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

        txtForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPwdDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    //Save user & password
                    if (ckbRemember.isChecked()) {
                        Paper.book().write(Common.USER_KEY, edtPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY, edtPassword.getText().toString());
                    }

                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("잠시만 기다려 주세요...");
                    mDialog.show();

                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Check if user not exist in database
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                //Get User Information
                                mDialog.dismiss();
                                User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                if (user != null) {
                                    user.setPhone(edtPhone.getText().toString()); //Set Phone
                                }
                                if (user != null) {
                                    if (user.getPassword().equals(edtPassword.getText().toString())) {
                                        Intent homeIntent = new Intent(SignIn.this, Home.class);
                                        Common.currentUser = user;
                                        startActivity(homeIntent);
                                        finish();

                                        table_user.removeEventListener(this);

                                    } else {
                                        Toast.makeText(SignIn.this, "다른 비밀번호!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "유저가 존재하지 않아요", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(SignIn.this, "인터넷 연결을 확인하세요 !", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void showForgotPwdDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignIn.this);
        alertDialog.setTitle("비밀번호를 모르시나요?");
        alertDialog.setMessage("Secure Code를 입력하세요");

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_view = inflater.inflate(R.layout.forgot_password_layout, null);

        alertDialog.setView(forgot_view);
        alertDialog.setIcon(R.drawable.ic_security_black_24dp);

        final MaterialEditText edtPhone = (MaterialEditText) forgot_view.findViewById(R.id.edtPhone);
        final MaterialEditText edtSecureCode = (MaterialEditText) forgot_view.findViewById(R.id.edtSecureCode);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Check 만약 유저가 있을 시
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);

                        if (user != null) {
                            if (user.getSecureCode().equals(edtSecureCode.getText().toString()))
                                Toast.makeText(SignIn.this, "비밀번호는 " + user.getPassword(), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(SignIn.this, "Secure code가 달라요 !", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }
}
