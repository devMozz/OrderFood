package com.example.milymozz.orderfood;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.milymozz.orderfood.Common.Common;
import com.example.milymozz.orderfood.Database.Database;
import com.example.milymozz.orderfood.Model.Food;
import com.example.milymozz.orderfood.Model.Order;
import com.example.milymozz.orderfood.Model.Rating;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {
    private TextView food_name, food_price, food_description;
    private ImageView food_image;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton btnRating;
    private CounterFab btnCart;
    private ElegantNumberButton numberButton;
    private RatingBar ratingBar;

    private String foodId = "";

    private FirebaseDatabase database;
    private DatabaseReference foods;
    private DatabaseReference ratingTbl;

    private Food currentFood;

    private FButton btnShowComment;

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

        setContentView(R.layout.activity_food_detail);

        btnShowComment = (FButton) findViewById(R.id.btnShowComment);
        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodDetail.this, ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID, foodId);
                startActivity(intent);
            }
        });

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        ratingTbl = database.getReference("Rating");

        //Init View
        numberButton = (ElegantNumberButton) findViewById(R.id.number_button);
        btnCart = (CounterFab) findViewById(R.id.btnCart);
        btnRating = (FloatingActionButton) findViewById(R.id.btnRating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        Common.currentUser.getPhone(),
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()
                ));

                Toast.makeText(FoodDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        food_description = (TextView) findViewById(R.id.food_description);
        food_name = (TextView) findViewById(R.id.food_name);
        food_price = (TextView) findViewById(R.id.food_price);
        food_image = (ImageView) findViewById(R.id.img_food);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //Get Intent here
        if (getIntent() != null)
            foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
        if (!foodId.isEmpty() && foodId != null) {

            if (Common.isConnectedToInternet(this)) {
                getDetailFood(foodId);
                getRatingFood(foodId);

            } else {
                Toast.makeText(this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getRatingFood(String foodId) {
        Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Rating item = postSnapshot.getValue(Rating.class);
                    if (item != null) {
                        sum += Integer.parseInt(item.getRateValue());
                    }
                    count++;

                }

                if (count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showRatingDialog() {
        if (!FoodDetail.this.isFinishing()) {
            new AppRatingDialog.Builder()
                    .setPositiveButtonText("Submit")
                    .setNegativeButtonText("Cancel")
                    .setNoteDescriptions(Arrays.asList("Very Bad", "Not Good", "Quite Ok", "Very Good", "Excellent"))
                    .setDefaultRating(1)
                    .setTitle("Rate this food")
                    .setDescription("Please select some stars and give you feedback")
                    .setTitleTextColor(R.color.colorPrimary)
                    .setDescriptionTextColor(R.color.colorPrimary)
                    .setHint("Please write your comment here...")
                    .setHintTextColor(R.color.colorAccent)
                    .setCommentTextColor(android.R.color.white)
                    .setCommentBackgroundColor(R.color.colorPrimaryDark)
                    .setWindowAnimation(R.style.RatingDialogFadeAnim)
                    .create(FoodDetail.this)
                    .show();
        }

    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                //Set Image
                Picasso.with(getBaseContext())
                        .load(currentFood.getImage())
                        .into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());

                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {

        //Get Rating and upload to Firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments);

        //Fix user can rate multiple time
        ratingTbl.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this, "Thank you for submit rating !!", Toast.LENGTH_SHORT).show();
                    }
                });

        /*
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()) {
                    //Remove old value (you can delete or let it be - useless function :D)
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();

                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);


                } else {
                    //Update new value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);

                }
                Toast.makeText(FoodDetail.this, "Thank you for submit rating !!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        */

    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
