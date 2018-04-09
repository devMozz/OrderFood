package com.example.milymozz.orderfood;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.milymozz.orderfood.Common.Common;
import com.example.milymozz.orderfood.Database.Database;
import com.example.milymozz.orderfood.Interface.ItemClickListener;
import com.example.milymozz.orderfood.Model.Banner;
import com.example.milymozz.orderfood.Model.Category;
import com.example.milymozz.orderfood.Model.Token;
import com.example.milymozz.orderfood.ViewHolder.MenuViewHolder;
import com.facebook.accountkit.AccountKit;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseDatabase database;
    private DatabaseReference category;

    private FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    private TextView txtFullName;
    private RecyclerView recycler_menu;

    private RecyclerView.LayoutManager layoutManager;

    private SwipeRefreshLayout swipeRefreshLayout;

    private CounterFab fab;

    //Slider
    HashMap<String, String> image_list;
    SliderLayout mSliderLayout;

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

        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(getBaseContext(), "Please check your connection !!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(getBaseContext(), "Please check your connection !!", Toast.LENGTH_SHORT).show();

                }
            }
        });

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        //Make sure you move this function after database is getInstance
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item, parent, false);

                return new MenuViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category category) {
                viewHolder.txtMenuName.setText(category.getName());
                Picasso.with(getBaseContext())
                        .load(category.getImage())
                        .into(viewHolder.imageView);
                final Category clickItem = category;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(Home.this, "" + clickItem.getName(), Toast.LENGTH_SHORT).show();
                        //Get CategoryId and send to new Activity
                        Intent foodListIntent = new Intent(Home.this, FoodList.class);
                        //Category is key, so we just gt key of this item
                        foodListIntent.putExtra("CategoryId", adapter.getRef(position).getKey());
                        Log.d("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodListIntent);

                    }
                });
            }
        };

        Paper.init(this);

        fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent cartIntent = new Intent(Home.this, Cart.class);
                startActivity(cartIntent);
            }
        });

        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set Name for user
        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView) headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //Load menu
        recycler_menu = (RecyclerView) findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);

        //LinearLayout Manager
//        layoutManager = new LinearLayoutManager(this);
//        recycler_menu.setLayoutManager(layoutManager);

        recycler_menu.setLayoutManager(new GridLayoutManager(this, 2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);

        updateToken(FirebaseInstanceId.getInstance().getToken());


        //Setup Slider
        setUpSlider();

        //Register Service
//        Intent service = new Intent(Home.this, ListenOrder.class);
//        startService(service);

    }

    private void setUpSlider() {
        mSliderLayout = (SliderLayout) findViewById(R.id.slider);
        image_list = new HashMap<>();

        final DatabaseReference banner = database.getReference("Banner");

        banner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Banner banner = postSnapshot.getValue(Banner.class);
                    //We will concat string name and id like
                    //PIZZA 01 -> And we will use PIZZA for show description, 01 for foodId to click

                    assert banner != null;
                    image_list.put(banner.getName() + "@@@" + banner.getId(), banner.getImage());

                }
                for (String key : image_list.keySet()) {
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    //Create Slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this, FoodDetail.class);
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });

                    //Add extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString(Common.INTENT_FOOD_ID, idOfFood);

                    mSliderLayout.addSlider(textSliderView);

                    //Remove event after finish
                    banner.removeEventListener(this);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSliderLayout.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSliderLayout.setCustomAnimation(new DescriptionAnimation());
        mSliderLayout.setDuration(4000);

    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token, false); // false because this token send from Client app
        tokens.child(Common.currentUser.getPhone()).setValue(data);

    }

    private void loadMenu() {
        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //Animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSliderLayout.stopAutoCycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        mSliderLayout.startAutoCycle();
        if (adapter != null)
            adapter.startListening();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search)
            startActivity(new Intent(Home.this, SearchActivity.class));

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent(Home.this, Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {
            Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_log_out) {
//            //Delete Remember user & password
//            Paper.book().destroy();
            AccountKit.logOut();

            //Logout
            Intent signIn = new Intent(Home.this, MainActivity.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(signIn);

//        } else if (id == R.id.nav_change_pwd) {
//            showChangePasswordDialog();
        } else if (id == R.id.nav_Update_name) {
            showUpdateNameDialog();

        } else if (id == R.id.nav_setting) {
            showSettingDialog();

        } else if (id == R.id.nav_home_address) {
            showHomeAddressDialog();
        } else if (id == R.id.nav_favorites) {
            startActivity(new Intent(Home.this, FavoritesActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSettingDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("SETTINGS");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_setting = inflater.inflate(R.layout.setting_layout, null);

        final CheckBox ckb_news = (CheckBox) layout_setting.findViewById(R.id.ckb_news);
        //Remember state of checkbox
        Paper.init(this);
        String isSubscribe = Paper.book().read("sub_news");
        if (isSubscribe == null || TextUtils.isEmpty(isSubscribe) || isSubscribe.equals("false"))
            ckb_news.setChecked(false);
        else
            ckb_news.setChecked(true);

        alertDialog.setView(layout_setting);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (ckb_news.isChecked()) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.TOPICNAME);
                    //Write Value
                    Paper.book().write("sub_news", "true");
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.TOPICNAME);
                    //Write Value
                    Paper.book().write("sub_news", "false");
                }

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

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Home Address");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_home = inflater.inflate(R.layout.home_address_layout, null);

        final MaterialEditText edtHomeAddress = (MaterialEditText) layout_home.findViewById(R.id.edtHomeAddress);

        alertDialog.setView(layout_home);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Set new Home Address

                // Update Home Address
                Map<String, Object> updateHome = new HashMap<>();
                updateHome.put("homeAddress", edtHomeAddress.getText().toString());

                //Set new Home Address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                FirebaseDatabase.getInstance()
                        .getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(updateHome)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Update Your Home Address", Toast.LENGTH_SHORT).show();
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

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Change Password");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.change_password_layout, null);

        final MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        //Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Change Password

                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                //Check old password
                if (edtPassword.getText().toString().equals(Common.currentUser.getPassword())) {
                    //Check new password and repeat password
                    if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())) {
                        Map<String, Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password", edtNewPassword.getText().toString());

                        //Make update
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        Toast.makeText(Home.this, "Password was Updated!", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        waitingDialog.dismiss();
                        Toast.makeText(Home.this, "New Password doesn't match", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this, "Wrong old password", Toast.LENGTH_SHORT).show();
                }

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

    private void showUpdateNameDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Name");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_name = inflater.inflate(R.layout.update_name_layout, null);

        final MaterialEditText edtName = (MaterialEditText) layout_name.findViewById(R.id.edtName);

        alertDialog.setView(layout_name);

        //Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Change Password

                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                // Update Name
                Map<String, Object> update_name = new HashMap<>();
                update_name.put("name", edtName.getText().toString());

                FirebaseDatabase.getInstance()
                        .getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(update_name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Dismiss Dialog
                                waitingDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(Home.this, "Name was updated!", Toast.LENGTH_SHORT).show();
                                }

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
