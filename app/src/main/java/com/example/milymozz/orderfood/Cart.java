package com.example.milymozz.orderfood;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.milymozz.orderfood.Common.Common;
import com.example.milymozz.orderfood.Database.Database;
import com.example.milymozz.orderfood.Helper.RecyclerItemTouchHelper;
import com.example.milymozz.orderfood.Interface.RecyclerItemTouchHelperListener;
import com.example.milymozz.orderfood.Model.DataMessage;
import com.example.milymozz.orderfood.Model.MyResponse;
import com.example.milymozz.orderfood.Model.Order;
import com.example.milymozz.orderfood.Model.Request;
import com.example.milymozz.orderfood.Model.Token;
import com.example.milymozz.orderfood.Model.User;
import com.example.milymozz.orderfood.Remote.APIService;
import com.example.milymozz.orderfood.Remote.IGoogleService;
import com.example.milymozz.orderfood.ViewHolder.CartAdapter;
import com.example.milymozz.orderfood.ViewHolder.CartViewHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, RecyclerItemTouchHelperListener {
    private FirebaseDatabase database;
    private DatabaseReference requests;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    public TextView txtTotalPlace;
    private String address, comment;
    private FButton btnPlace;

    private RelativeLayout rootLayout;

    private List<Order> cart = new ArrayList<>();

    private CartAdapter adapter;

    private APIService mService;

    private Place shippingAddress;

    //Declare google Map Api Retrofit
    private IGoogleService mGoogleService;

    //Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;

    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICE_REQUEST = 9997;

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

        setContentView(R.layout.activity_cart);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]
                    {
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_REQUEST_CODE);

        } else {
            if (checkPlayService()) {

                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        //Init
        mGoogleService = Common.getGoogleMapApi();

        //Init Service
        mService = Common.getFCMService();

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Swipe to Delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPlace = (TextView) findViewById(R.id.total);
        btnPlace = (FButton) findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create new Request
                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "카트가 비었어요 !", Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayService()) { // If have play service on Device

                        buildGoogleApiClient();
                        createLocationRequest();

                    }
                }
                break;
        }
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }

        return true;
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("주소를 입력하세요 : ");


        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);

//        final MaterialEditText edtAddress = (MaterialEditText) order_address_comment.findViewById(R.id.edtAddress);

        final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //Hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //Set hide for Autocomplete edit text
        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("주소를 입력하세요");
        //Set Text Size
        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);

        //Get address from place Autocomplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.e("ERROR", status.getStatusMessage());
            }
        });

        final MaterialEditText edtComment = (MaterialEditText) order_address_comment.findViewById(R.id.edtComment);

        //Radio
        final RadioButton rdiShipToAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiHomeAddress);

        final RadioButton rdiCod = (RadioButton) order_address_comment.findViewById(R.id.rdiCode);
        final RadioButton rdiPaypal = (RadioButton) order_address_comment.findViewById(R.id.rdiPaypal);
        final RadioButton rdiOrderBalance = (RadioButton) order_address_comment.findViewById(R.id.rdiOrderBalance);

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Common.currentUser.getHomeAddress() != null || !TextUtils.isEmpty(Common.currentUser.getHomeAddress())) {
                        address = Common.currentUser.getHomeAddress();
                        //Set this address to edtAddress
                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);
                    } else {
                        Toast.makeText(Cart.this, "집 주소를 업로드 해주세요", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Event Radio
        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()

        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mGoogleService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //If fetch API ok
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().toString());
                                        JSONArray resultArray = jsonObject.getJSONArray("results");
                                        JSONObject firstObject = resultArray.getJSONObject(0);
                                        address = firstObject.getString("formatted_address");

                                        //Set this address to edtAddress
                                        ((EditText) edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }
        });

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                address = edtAddress.getText().toString();
//                comment = edtComment.getText().toString();

                // Add check condition here
                // If user select address from place fragment, just use it
                // If user select Ship To This Address, get Address from location and use it
                // If user select Home Address, get HomeAddress from Profile and use it
                if (!rdiShipToAddress.isChecked() && !rdiHomeAddress.isChecked()) {
                    // If both Radio is not selected ->
                    if (shippingAddress != null) {
                        address = shippingAddress.getAddress().toString();
                    } else {
                        Toast.makeText(Cart.this, "주소를 입력하거나 혹은 선택해 주세요", Toast.LENGTH_SHORT).show();

                        //Fix crash fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();

                        return;
                    }
                }

                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(Cart.this, "주소를 입력하세요", Toast.LENGTH_SHORT).show();

                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();

                    return;
                }

                if (!rdiCod.isChecked() && !rdiPaypal.isChecked() && !rdiOrderBalance.isChecked()) {
                    Toast.makeText(Cart.this, "Paypal Option", Toast.LENGTH_SHORT).show();

                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();

                    return;

                } else if (rdiPaypal.isChecked()) {
                    // I will add Paypal SDK later..

                } else if (rdiCod.isChecked()) {
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            txtTotalPlace.getText().toString(),
                            "0",
                            edtComment.getText().toString(),
                            "COD",
                            String.format("%s, %s", mLastLocation.getLatitude(), mLastLocation.getLongitude()), // Coordinates 유저가 주문을 할 때
                            cart);

                    // System.CurrentMilli 를 키로 사용
                    String order_number = String.valueOf(System.currentTimeMillis());
                    requests.child(order_number)
                            .setValue(request);

                    //Delete 카트
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                    sendNotification(order_number);

                    Toast.makeText(Cart.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                    finish();

                } else if (rdiOrderBalance.isChecked()) {
                    double amount = 0;
                    // First, 전체 가격을 받아온다
                    try {
                        amount = Common.formatCurrency(txtTotalPlace.getText().toString(), Locale.US).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // 이 주문의 총 가격을 받은 후 사용자 Balance 완료
                    if (Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount) {
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPlace.getText().toString(),
                                "0",
                                edtComment.getText().toString(),
                                "Order Balance",
                                String.format("%s, %s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                                cart);

                        final String order_number = String.valueOf(System.currentTimeMillis());
                        requests.child(order_number)
                                .setValue(request);

                        //업데이트 balance
                        double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
                        Map<String, Object> update_balance = new HashMap<>();
                        update_balance.put("balance", balance);

                        FirebaseDatabase.getInstance()
                                .getReference("User")
                                .child(Common.currentUser.getPhone())
                                .updateChildren(update_balance)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //Refresh User
                                            FirebaseDatabase.getInstance()
                                                    .getReference("User")
                                                    .child(Common.currentUser.getPhone())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                                            //Send Order to Server
                                                            sendNotification(order_number);

                                                            //Delete cart
                                                            new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                                                            finish();
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                });

                    } else {
                        Toast.makeText(Cart.this, "Balance가 부족합니다, 다른 결제 방법을 선택하세요", Toast.LENGTH_SHORT).show();
                    }

                }


                //Remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();

            }
        });

        alertDialog.show();
    }

    private void sendNotification(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("serverToken").equalTo(true); // get all node with isServerToken is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Token serverToken = null;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    serverToken = postSnapshot.getValue(Token.class);
                }

                //Create raw payload to send
//                    Notification notification = new Notification("MOZZ DEV", "You have new order" + order_number);
//                    Sender content = new Sender(serverToken.getToken(), notification);

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "MozzDev");
                dataSend.put("message", "주문이 들어왔어요! " + order_number);
                DataMessage dataMessage = new DataMessage(serverToken != null ? serverToken.getToken() : null, dataSend);

                String test = new Gson().toJson(dataMessage);
                Log.d("Content", test);

                mService.sendNotification(dataMessage)
                        .enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                //Only run when get result
                                if (response.code() == 200) {
                                    if (response.body().success == 1) {

                                    } else {
                                        Toast.makeText(Cart.this, "Failed !!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Log.e("ERROR", t.getMessage());
                            }
                        });
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total = 0;
        for (Order order : cart)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));

        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPlace.setText(fmt.format(total));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());

        return true;
    }

    private void deleteCart(int position) {
        //remove item at List<Order> by position
        cart.remove(position);

        //after that, delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());

        //after final, update new data from List<Order> to SQLite // 응 돌아가 카트로..
        for (Order item : cart)
            new Database(this).addToCart(item);

        //refresh
        loadListFood();

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("LOCATION", "나의 위치 : " + mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());


        } else {
//                Toast.makeText(this, "Couldn't get the location", Toast.LENGTH_SHORT).show();
            Log.d("LOCATION", "Location을 얻을 수 없음");

        }
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder) {
            String name = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()); // item
            final int deleteIndex = viewHolder.getAdapterPosition(); // position

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            //update
            //Update txtTotal
            //Calculate total price
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));

            Locale locale = new Locale("en", "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

            txtTotalPlace.setText(fmt.format(total));

            //SnackBar
            Snackbar snackbar = Snackbar.make(rootLayout, name + " 카트에서 삭제", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem); // 다시 리스트에 넣기
                    //Update txtTotal
                    //Calculate total price
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item : orders)
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));

                    Locale locale = new Locale("en", "US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                    txtTotalPlace.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
