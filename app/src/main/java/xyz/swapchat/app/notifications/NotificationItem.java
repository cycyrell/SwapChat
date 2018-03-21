package xyz.teamcatalyst.breedr.notifications;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.api.FirebaseApi;
import xyz.teamcatalyst.breedr.data.Item;
import xyz.teamcatalyst.breedr.data.Profile;
import xyz.teamcatalyst.breedr.profile.ItemDetailsActivity;
import xyz.teamcatalyst.breedr.profile.ProfileActivity;

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 07/08/2017
 */
public class NotificationItem extends AbstractItem<NotificationItem, NotificationItem.ViewHolder> {

    public final Profile profile;
    public final boolean isMatched;
    public final boolean isLikeBackVisible;
    public final String key;

    private List<Item> items = new ArrayList<>();
    private List<String> keys = new ArrayList<>();

    public NotificationItem(String key, Profile profile, boolean isLikeBackVisible, boolean isMatched) {
        this.profile = profile;
        this.isMatched = isMatched;
        this.isLikeBackVisible = isLikeBackVisible;
        this.key = key;
    }

    @Override
    public int getType() {
        return R.id.fastadapter_image_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_notification;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.mDisplayPicture.setImageDrawable(null);
    }
    /**
     * binds the data of this item onto the viewHolder
     *
     * @param viewHolder the viewHolder of this item
     */
    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);

        //get the context
        Context ctx = viewHolder.itemView.getContext();

        //define our data for the view
        viewHolder.mName.setText(profile.getDisplayName());


        viewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));
        ProfileActivity.DogAdapter adapter = new ProfileActivity.DogAdapter(items, keys, R.layout.item_small_list_content, (dog, key) -> {
            ItemDetailsActivity.start(ctx, dog, key, true);
        });

        viewHolder.recyclerView.setVisibility(View.VISIBLE);
        viewHolder.recyclerView.setAdapter(adapter);
        DatabaseReference userItems = FirebaseApi.getInstance().getDatabase().getReference("user_items").child(key);
        userItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userItemsSnapshot) {
                items.clear();
                keys.clear();

                DatabaseReference history = FirebaseApi.getInstance().getDatabase().getReference("history");

                history.child("itemsYouLike").child(FirebaseApi.getUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot itemsYouLIkeSnapshot) {
                                List<String> itemsYouLike = new ArrayList<>();
                                for (DataSnapshot itemYouLike : itemsYouLIkeSnapshot.getChildren()) {
                                    itemsYouLike.add(itemYouLike.getKey());
                                }

                                for (DataSnapshot dataSnapshot1 : userItemsSnapshot.getChildren()) {
                                    Item dog = dataSnapshot1.getValue(Item.class);
                                    keys.add(dataSnapshot1.getKey());
                                    if (itemsYouLike.contains(dataSnapshot1.getKey())) {
                                        items.add(dog);
                                    }
                                }
                                if (items.isEmpty()) {
                                    viewHolder.recyclerView.setVisibility(View.GONE);
                                }
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (isMatched) {
            viewHolder.mContactNumber.setText(profile.getContactNumber());
            viewHolder.mEmail.setText(profile.getEmail());
        } else {
            viewHolder.mContactNumber.setText("Contact Number Hidden");
            viewHolder.mEmail.setText("Email Hidden");
        }

        if (isLikeBackVisible) {
            viewHolder.likeBack.setVisibility(View.VISIBLE);
        } else {
            viewHolder.likeBack.setVisibility(View.GONE);
        }

        viewHolder.mDisplayPicture.setImageBitmap(null);
//        viewHolder.recyclerView.setVisibility(View.GONE);

        //load glide
        userItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Glide.with(ctx).load(profile.getProfileUrl()).into(viewHolder.mDisplayPicture);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        protected View view;
        @BindView(R.id.display_picture) ImageView mDisplayPicture;
        @BindView(R.id.name) TextView mName;
        @BindView(R.id.contact_number) TextView mContactNumber;
        @BindView(R.id.email) TextView mEmail;
        @BindView(R.id.likeBack) ImageView likeBack;
        @BindView(R.id.recyclerView) RecyclerView recyclerView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }
    }
}
