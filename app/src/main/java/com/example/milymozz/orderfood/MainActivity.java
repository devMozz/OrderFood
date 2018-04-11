package com.example.milymozz.orderfood;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.milymozz.orderfood.Common.Common;
import com.example.milymozz.orderfood.Model.User;
import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference users;

    private Button btnContinue;
    private TextView txtSlogan, txtMain;

    private static final int REQUEST_CODE = 7171;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountKit.initialize(this);
        setContentView(R.layout.activity_main);

        printKeyHash();

        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");

        btnContinue = (Button) findViewById(R.id.btn_continue);

        txtMain = (TextView) findViewById(R.id.txtMain);

        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Nabila.ttf");
        txtMain.setTypeface(face);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent signUp = new Intent(MainActivity.this, SignUp.class);
//                startActivity(signUp);
                startLoginSystem();
            }
        });

//        //Init Paper
//        Paper.init(this);
//
//        // Check remember
//        String user = Paper.book().read(Common.USER_KEY);
//        String pwd = Paper.book().read(Common.PWD_KEY);
//
//        if (user != null && pwd != null) {
//            if (!user.isEmpty() && !pwd.isEmpty()) {
//                login(user, pwd);
//            }
//        }

        //Check Session Facebook Account Kit
        if (AccountKit.getCurrentAccessToken() != null) {
            //Show Dialog
            final AlertDialog waitingDialog = new SpotsDialog(this);
            waitingDialog.show();
            waitingDialog.setMessage("잠시만 기다려 주세요");
            waitingDialog.setCancelable(false);

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    users.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User localUser = dataSnapshot.getValue(User.class);

                                    Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                    Common.currentUser = localUser;
                                    startActivity(homeIntent);

                                    //Dismiss dialog
                                    waitingDialog.dismiss();
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });

        }

    }

    private void startLoginSystem() {
        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent, REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError() != null) {
                Toast.makeText(this, "" + result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            } else if (result.wasCancelled()) {
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                return;
            } else {
                //Show Dialog
                final AlertDialog waitingDialog = new SpotsDialog(this);
                waitingDialog.show();
                waitingDialog.setMessage("잠시만 기다려 주세요");
                waitingDialog.setCancelable(false);

                //Get current Phone
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        final String userPhone = String.valueOf(account.getPhoneNumber());

                        users.orderByKey().equalTo(userPhone)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.child(userPhone).exists()) { // If not exists
                                            User newUser = new User();
                                            newUser.setPhone(userPhone);
                                            newUser.setName("");
                                            newUser.setBalance(String.valueOf(0.0));

                                            users.child(userPhone)
                                                    .setValue(newUser)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(MainActivity.this, "가입 성공 !", Toast.LENGTH_SHORT).show();

                                                                // Login
                                                                users.child(userPhone)
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                User localUser = dataSnapshot.getValue(User.class);

                                                                                Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                                                Common.currentUser = localUser;
                                                                                startActivity(homeIntent);

                                                                                //Dismiss dialog
                                                                                waitingDialog.dismiss();
                                                                                finish();
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });

                                        } else { // If exists
                                            // Just Login
                                            // Login
                                            users.child(userPhone)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            User localUser = dataSnapshot.getValue(User.class);

                                                            Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                            Common.currentUser = localUser;
                                                            startActivity(homeIntent);

                                                            //Dismiss dialog
                                                            waitingDialog.dismiss();
                                                            finish();
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(MainActivity.this, "" + accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }
    }

    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.milymozz.orderfood",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    private void login(final String phone, final String pwd) {
        //just copy login code from SignIn

        //Init Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        if (Common.isConnectedToInternet(getBaseContext())) {

            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Please waiting...");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Check if user not exist in database
                    if (dataSnapshot.child(phone).exists()) {
                        //Get User Information
                        mDialog.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        if (user != null) {
                            user.setPhone(phone); //Set Phone
                            Log.d("PHONE", "" + user.getPhone() + " " + user.getName() + " " + user.getPassword() + " " + user.getIsStaff());
                        }
                        if (user != null) {
                            if (user.getPassword().equals(pwd)) {
                                Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                Common.currentUser = user;
                                Log.d("PHONE", "" + user.getPhone() + " " + user.getName() + " " + user.getPassword() + " " + user.getIsStaff());
                                startActivity(homeIntent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Wrong Password!", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } else {
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this, "User not exist in Database", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
        }

    }
}
