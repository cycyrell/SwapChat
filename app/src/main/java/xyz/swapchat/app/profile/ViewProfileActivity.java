package xyz.teamcatalyst.breedr.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import xyz.teamcatalyst.breedr.GlideApp;
import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.api.FirebaseApi;
import xyz.teamcatalyst.breedr.chat.ChatActivity;
import xyz.teamcatalyst.breedr.data.Item;
import xyz.teamcatalyst.breedr.data.UserFeedback;
import xyz.teamcatalyst.breedr.data.UserProfile;
import xyz.teamcatalyst.breedr.databinding.ActivityViewProfileBinding;

public class ViewProfileActivity extends AppCompatActivity {

    public UserProfile userProfile;
    public boolean isMatched;
    private ProfileActivity.DogAdapter adapter;
    private List<Item> items = new ArrayList<>();
    private List<String> keys = new ArrayList<>();
    private boolean isSelectingDog;
    private Item selectedItem;
    private String selectedKey;
    private FastItemAdapter fastAdapter;
    String feedback = "";
    private double ratingDouble;
    private Set<String> myFeedbacksOnItemId = new HashSet<>();

    public static void start(Context context, UserProfile userProfile, boolean isMatched) {
        Intent starter = new Intent(context, ViewProfileActivity.class);
        starter.putExtra("userProfile", userProfile);
        starter.putExtra("isMatched", isMatched);
        context.startActivity(starter);
    }

    ActivityViewProfileBinding activityViewProfileBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userProfile = (UserProfile) getIntent().getSerializableExtra("userProfile");
        isMatched = getIntent().getBooleanExtra("isMatched", false);
        activityViewProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_profile);
        activityViewProfileBinding.setVm(this);

        fastAdapter = new FastItemAdapter();
        activityViewProfileBinding.feedbackList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        activityViewProfileBinding.feedbackList.setAdapter(fastAdapter);

        GlideApp.with(this).load(userProfile.getProfileUrl()).fitCenter().centerCrop()
                .into(activityViewProfileBinding.userProfileImage);

        loadDogs();
    }

    private void loadDogs() {
        DatabaseReference myDogs = FirebaseApi.getInstance().getDatabase().getReference("user_items").child(userProfile.getUserId());
        activityViewProfileBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new ProfileActivity.DogAdapter(items, keys, (item, key) -> {
            FirebaseUser me = FirebaseApi.getUser();
            DatabaseReference dogFeedback = FirebaseApi.getInstance().getDatabase().getReference("feedback").child(item.getOwnerId() + item.getName());
            selectedItem = item;
            selectedKey = key;

            dogFeedback.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    activityViewProfileBinding.viewDogProfile.setVisibility(View.VISIBLE);
                    if (!FirebaseApi.getUser().getUid().equals(userProfile.getUserId())) {
                        activityViewProfileBinding.matchAction.setVisibility(View.VISIBLE);
                    }
                    fastAdapter.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserFeedback userFeedback = snapshot.getValue(UserFeedback.class);
                        if (FirebaseApi.getUser().getUid().equals(userFeedback.getUserId())) {
                            myFeedbacksOnItemId.add(item.getName());
                        }
                        fastAdapter.add(new FeedbackItem(userFeedback));
                    }
                    if (isSelectingDog) {
                        if (myFeedbacksOnItemId.contains(item.getName())) {
                            AlertDialog dialog = new AlertDialog.Builder(ViewProfileActivity.this)
                                    .setTitle(R.string.feedback)
                                    .setMessage(R.string.feedback_already)
                                    .setOnDismissListener(dialog12 -> {
                                        isSelectingDog = false;
                                        activityViewProfileBinding.focusBackground.setVisibility(View.GONE);
                                    })
                                    .create();
                            dialog.show();
                        } else {
                            View view = getLayoutInflater().inflate(R.layout.dialog_feedback, null);

                            AlertDialog dialog = new AlertDialog.Builder(ViewProfileActivity.this)
                                    .setTitle(R.string.feedback)
                                    .setMessage(R.string.enter_comment)
                                    .setView(view)
                                    .setOnDismissListener(dialog12 -> {
                                        isSelectingDog = false;
                                        activityViewProfileBinding.focusBackground.setVisibility(View.GONE);
                                    })
                                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                        dialogInterface.dismiss();
                                    })
                                    .setPositiveButton(R.string.submit, (dialog1, which) -> {
                                        EditText feedbackEt = (EditText) view.findViewById(R.id.feedback);
                                        RatingBar rating = (RatingBar) view.findViewById(R.id.dialog_ratingbar);
                                        feedback = String.valueOf(feedbackEt.getText());
                                        if (feedback.toLowerCase().contains("fuck")) {
                                            feedbackEt.setError(getString(R.string.profanity));
                                        } else {
                                            ratingDouble = (double) rating.getRating();
                                            DatabaseReference myFeedback = dogFeedback.child(me.getUid());
                                            myFeedback.setValue(new UserFeedback(feedback, me.getUid(), me.getDisplayName(), ratingDouble, "")).addOnCompleteListener(task -> {
                                                dialog1.dismiss();
                                            });
                                        }

                                    })
                                    .create();
                            dialog.show();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        });
        activityViewProfileBinding.recyclerView.setAdapter(adapter);

        myDogs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();
                keys.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Item item = dataSnapshot1.getValue(Item.class);
                    keys.add(dataSnapshot1.getKey());
                    items.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onMatchActionClicked() {
        if (isMatched) {
            unmatch();
            if (FirebaseApi.getUser().getUid().equals(selectedItem.getOwnerId())) {
                activityViewProfileBinding.matchAction.setText("Like Back");
            }
        } else {
            if (FirebaseApi.getUser().getUid().equals(selectedItem.getOwnerId())) {
                likeBack();
            } else {

            }
        }
    }

    private void unmatch() {
        String ownerId = selectedItem.getOwnerId();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference history = database.getReference("history");

        // delete your like
        history.child(FirebaseApi.getUser().getUid()).child("youLike").child(ownerId).setValue(null); // delete my history
        history.child(ownerId).child("likesYou").child(FirebaseApi.getUser().getUid()).setValue(null); // delete likesYou of other person
        history.child(ownerId).child("youLike").child(FirebaseApi.getUser().getUid()).setValue(true); // return status to youLike of other person
        history.child(FirebaseApi.getUser().getUid()).child("likesYou").child(ownerId).setValue(true); // return status to likesYou of you

        // delete matched
        history.child(FirebaseApi.getUser().getUid()).child("matched").child(ownerId).setValue(null);
        history.child(ownerId).child("matched").child(FirebaseApi.getUser().getUid()).setValue(null);

        isMatched = false;
    }

    private void likeBack() {
        String ownerId = selectedItem.getOwnerId();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference history = database.getReference("history");
        history.child(FirebaseApi.getUser().getUid()).child("youLike").child(ownerId).setValue(true);
        history.child(ownerId).child("likesYou").child(FirebaseApi.getUser().getUid()).setValue(true);
        history.child(FirebaseApi.getUser().getUid()).child("likesYou").child(ownerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                history.child(FirebaseApi.getUser().getUid()).child("matched").child(ownerId).setValue(true);
                history.child(ownerId).child("matched").child(FirebaseApi.getUser().getUid()).setValue(true);
                history.child(ownerId).child("likesYou").child(FirebaseApi.getUser().getUid()).setValue(null);
                history.child(ownerId).child("youLike").child(FirebaseApi.getUser().getUid()).setValue(null);
                history.child(FirebaseApi.getUser().getUid()).child("likesYou").child(ownerId).setValue(null);
                history.child(FirebaseApi.getUser().getUid()).child("youLike").child(ownerId).setValue(null);
                activityViewProfileBinding.matchAction.setText("Unmatch");
                isMatched = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onFeedbackClicked() {
        activityViewProfileBinding.focusBackground.setVisibility(View.VISIBLE);
        isSelectingDog = true;
    }

    public void onViewDogProfileClicked() {
        ItemDetailsActivity.start(this, selectedItem, selectedKey, true);
    }

    public void onChatClicked() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("EXTRA_CHAT_USER_ID", userProfile.getUserId());
        startActivity(intent);
    }
}
