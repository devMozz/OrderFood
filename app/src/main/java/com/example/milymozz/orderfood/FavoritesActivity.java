package com.example.milymozz.orderfood;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.milymozz.orderfood.Common.Common;
import com.example.milymozz.orderfood.Database.Database;
import com.example.milymozz.orderfood.Helper.RecyclerItemTouchHelper;
import com.example.milymozz.orderfood.Interface.RecyclerItemTouchHelperListener;
import com.example.milymozz.orderfood.Model.Favorites;
import com.example.milymozz.orderfood.ViewHolder.FavoritesAdapter;
import com.example.milymozz.orderfood.ViewHolder.FavoritesViewHolder;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private FavoritesAdapter adapter;
    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Swipe to Delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        loadList();
    }

    private void loadList() {
        adapter = new FavoritesAdapter(this, new Database(this).getAllFavorites(Common.currentUser.getPhone()));
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoritesViewHolder) {
            String name = ((FavoritesAdapter) recyclerView.getAdapter()).getItem(position).getFoodName();

            final Favorites deleteItem = ((FavoritesAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(viewHolder.getAdapterPosition());
            new Database(getBaseContext()).removeFromFavorites(deleteItem.getFoodId(), Common.currentUser.getPhone());

            Snackbar snackbar = Snackbar.make(rootLayout, name + " 카트에서 삭제", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToFavorites(deleteItem); // 다시 리스트에 넣기

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }

    }
}
