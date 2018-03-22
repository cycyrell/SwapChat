package xyz.teamcatalyst.breedr.lovematch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;
import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.api.FirebaseApi;
import xyz.teamcatalyst.breedr.data.History;
import xyz.teamcatalyst.breedr.data.Item;
import xyz.teamcatalyst.breedr.data.Profile;
import xyz.teamcatalyst.breedr.notifications.NotificationItem;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    @BindView(R.id.itemStack)
    SwipeFlingAdapterView mItemStack;
    @BindView(R.id.filter_gender)
    Spinner mFilterGender;
    @BindView(R.id.filter_age_number)
    Spinner mFilterAgeNumber;
    @BindView(R.id.filter_age_unit)
    Spinner mFilterAgeUnit;
    @BindView(R.id.seek_distance)
    SeekBar seekDistance;
    @BindView(R.id.distance_value)
    TextView distanceValue;
    @BindView(R.id.filter_breed)
    Spinner filterBreed;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private String currentUserId;
    private DatabaseReference myItems;
    private DatabaseReference history;
    private ArrayList<Item> items;
    private CardsDataAdapter cardsDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myItems = database.getReference("user_items");
        history = database.getReference("history");
        seekDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                distanceValue.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekDistance.setProgress(12);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mFilterGender.setSelection(0);
        loadHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadHistory() {
        DatabaseReference historyDbRef = history.child(FirebaseAuth.getInstance().getUid());
        historyDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                History history = dataSnapshot.getValue(History.class);
                if (history == null) {
                    setupFlingAdapterView(new HashSet<>());
                    return;
                }
                Set<String> youLike = history.youLike != null ? history.youLike.keySet() : new HashSet<>();
                setupFlingAdapterView(youLike);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                setupFlingAdapterView(new HashSet<>());
            }
        });
    }

    private void setupFlingAdapterView(Set<String> yourLikes) {

        items = new ArrayList<>();
        cardsDataAdapter = new CardsDataAdapter(this, items);
        mItemStack.setAdapter(cardsDataAdapter);
        cardsDataAdapter.notifyDataSetChanged();
        mItemStack.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                items.remove(0);
                cardsDataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
//                Toast.makeText(ItemListActivity.this, "Skipped!", Toast.LENGTH_SHORT).show();
                dislike((Item) dataObject);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                likeItem((Item) dataObject);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
//                al.add("XML ".concat(String.valueOf(i)));
//                arrayAdapter.notifyDataSetChanged();
//                Log.d("LIST", "notified");
//                i++;
            }

            @Override
            public void onScroll(float v) {

            }
        });

        // Optionally add an OnItemClickListener
        mItemStack.setOnItemClickListener((itemPosition, dataObject) -> {
            Intent intent = new Intent(this, ItemDetailActivity.class);
            intent.putExtra(ItemDetailFragment.ARG_ITEM, (Item) dataObject);
            startActivityForResult(intent, 101);
        });

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.show();
        myItems.addValueEventListener(new ValueEventListener() {
            public Set<String> withinVicinity = new HashSet<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();
                for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                    if (snapshot1.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        continue;
                    for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                        Item item = snapshot2.getValue(Item.class);
                        if (item == null) continue;
                        item.setItemId(snapshot2.getKey());
                        if (yourLikes.contains(item.getOwnerId())) {
                            continue;
                        }

                        if (!filterBreed.getSelectedItem().toString().equals("*")) {
                            if (!filterBreed.getSelectedItem().toString().equals(item.getCategory())) {
                                continue;
                            }
                        }

                        Location location = FirebaseApi.getInstance().getLocation();
                        if (location != null) {
                            GeoQuery geoQuery = FirebaseApi.getInstance().getGeofire().queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), seekDistance.getProgress());
                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    withinVicinity.add(key);
                                }

                                @Override
                                public void onKeyExited(String key) {

                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {

                                }

                                @Override
                                public void onGeoQueryReady() {
                                    geoQuery.removeAllListeners();
                                    if (seekDistance.getProgress() == seekDistance.getMax() || withinVicinity.contains(item.getOwnerId())) {
                                        if (!Prefs.getBoolean("dislike" + item.getItemId(), false)) {
                                            items.add(item);
                                            cardsDataAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {

                                }
                            });
                        } else {
                            if (seekDistance.getProgress() == seekDistance.getMax() || withinVicinity.contains(item.getOwnerId())) {
                                if (!Prefs.getBoolean("dislike" + item.getItemId(), false)) {
                                    items.add(item);
                                    cardsDataAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
                cardsDataAdapter.notifyDataSetChanged();
                progressDialog.dismiss();

                showTutorial();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                cardsDataAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showTutorial() {
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(false)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.MINIMUM)
                .dismissOnTouch(true)
                .setTargetPadding(30)
                .setDelayMillis(100)
                .enableFadeAnimation(true)
                .performClick(false)
                .setIdempotent(false)
                .setInfoText(getString(R.string.swipe_tutorial))
                .setShape(ShapeType.CIRCLE)
                .setTarget(mItemStack)
                .setUsageId(UUID.randomUUID().toString())
                .show();

    }

    private void likeItem(Item dataObject) {
        if (dataObject instanceof Item) {
            String ownerId = ((Item) dataObject).getOwnerId();
//            Toast.makeText(ItemListActivity.this, "Liked!", Toast.LENGTH_SHORT).show();
            history.child(currentUserId).child("youLike").child(ownerId).setValue(System.currentTimeMillis());
            history.child(ownerId).child("likesYou").child(currentUserId).setValue(System.currentTimeMillis());
            history.child("itemsYouLike").child(FirebaseApi.getUser().getUid()).child(dataObject.getItemId()).setValue(dataObject);
            history.child(currentUserId).child("likesYou").child(ownerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null) return;
                    history.child(FirebaseApi.getUser().getUid()).child("matched").child(ownerId).setValue(System.currentTimeMillis());
                    history.child(ownerId).child("matched").child(FirebaseApi.getUser().getUid()).setValue(System.currentTimeMillis());
                    history.child(ownerId).child("likesYou").child(FirebaseApi.getUser().getUid()).setValue(null);
                    history.child(ownerId).child("youLike").child(FirebaseApi.getUser().getUid()).setValue(null);
                    history.child(FirebaseApi.getUser().getUid()).child("likesYou").child(ownerId).setValue(null);
                    history.child(FirebaseApi.getUser().getUid()).child("youLike").child(ownerId).setValue(null);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void dislike(Item item) {
        Prefs.putBoolean("dislike" + item.getItemId(), true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == 100) {
                likeItem(items.get(0));
                items.remove(0);
                cardsDataAdapter.notifyDataSetChanged();
            }
        }
    }

    @OnClick(R.id.apply_filter)
    public void onApplyFilterClicked() {
        loadHistory();
    }
}
