package xyz.teamcatalyst.breedr.profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import xyz.teamcatalyst.breedr.GlideApp;
import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.auth.ActivitySignin;
import xyz.teamcatalyst.breedr.data.Item;
import xyz.teamcatalyst.breedr.data.Profile;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    public RecyclerView mRecyclerView;
    @BindView(R.id.et_user_fullname)
    public TextInputEditText mProfileFullName;
    @BindView(R.id.et_user_contact)
    public TextInputEditText mEtUserContact;
    @BindView(R.id.et_user_email)
    public TextInputEditText mEtUserEmail;
    @BindView(R.id.iv_profile_image)
    ImageView mIvProfileImage;

    private DogAdapter adapter;
    private List<Item> items = new ArrayList<>();
    private List<String> keys = new ArrayList<>();
    private DatabaseReference myProfile;
    private StorageReference mStorageRef;
    private String downloadUrl = "";
    private Boolean isForRegistration = false;

    public static void start(Context context) {
        Intent starter = new Intent(context, ProfileActivity.class);
        context.startActivity(starter);
    }

    public static Intent getRegistrationIntent(Context context) {
        Intent starter = new Intent(context, ProfileActivity.class);
        starter.putExtra("is_register", true);
        return starter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        isForRegistration = getIntent().getBooleanExtra("is_register", false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myDogs = database.getReference("user_items").child(currentUser.getUid());
        myProfile = database.getReference("user_profile").child(currentUser.getUid());

        mEtUserEmail.setText(Prefs.getString("email", currentUser.getEmail()));
        mProfileFullName.setText(Prefs.getString("fullname", currentUser.getDisplayName()));

        myProfile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile profile = dataSnapshot.getValue(Profile.class);
                if (profile == null) return;
                mProfileFullName.setText(profile.getDisplayName());
                downloadUrl = profile.getProfileUrl();
                mEtUserEmail.setText(profile.getEmail());
                mEtUserContact.setText(profile.getContactNumber());
                if (!TextUtils.isEmpty(profile.getProfileUrl())) {
                    GlideApp.with(ProfileActivity.this).load(profile.getProfileUrl()).centerCrop().into(mIvProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new DogAdapter(items, keys, (dog, key) -> {
            ItemDetailsActivity.start(this, dog, key, false);
        });
        mRecyclerView.setAdapter(adapter);

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

    @OnClick(R.id.save)
    public void onSaveClicked() {
        if (isForRegistration && TextUtils.isEmpty(downloadUrl)) {
            new AlertDialog.Builder(this)
                    .setMessage("Please take a selfie (this will be used for login)")
                    .setPositiveButton("Ok", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .show();
            return;
        }
        myProfile.setValue(new Profile(mProfileFullName.getText().toString(),
                downloadUrl,
                mEtUserContact.getText().toString(),
                mEtUserEmail.getText().toString())).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.profile_saved)
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            if (isForRegistration) {
                                setResult(RESULT_OK);
                                finish();
                                startActivity(new Intent(ProfileActivity.this, ActivitySignin.class));
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Go back to previous tutorial
        super.onBackPressed();
    }

    @OnClick(R.id.btn_add_dog)
    public void onAddDogClicked() {
        Intent intent = new Intent(this, ItemDetailsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.iv_profile_image)
    public void onProfileImageClicked() {
        EasyImage.openChooserWithGallery(this, "Upload profile picture", 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File file, EasyImage.ImageSource imageSource, int type) {
                Uri uri = Uri.fromFile(file);
                StorageReference riversRef = mStorageRef.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/profile.jpg");

                riversRef.putFile(uri)
                        .addOnSuccessListener(taskSnapshot -> {
                            downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            GlideApp.with(ProfileActivity.this).load(downloadUrl).centerCrop().into(mIvProfileImage);
                        });
            }
        });
    }


    public static class DogAdapter extends RecyclerView.Adapter<DogAdapter.ViewHolder> {

        private final List<Item> mValues;
        private final DogListListener mListener;
        private final List<String> mKeys;
        private int layout;

        public DogAdapter(List<Item> items, List<String> keys, DogListListener dogListListener) {
            mValues = items;
            mKeys = keys;
            mListener = dogListListener;
            layout = R.layout.item_list_content;
        }

        public DogAdapter(List<Item> items, List<String> keys, int layout, DogListListener dogListListener) {
            mValues = items;
            mKeys = keys;
            mListener = dogListListener;
            this.layout = layout;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mName.setText(mValues.get(position).getName());
            GlideApp.with(holder.mView.getContext()).load(holder.mItem.getItemImage()).centerCrop().into(holder.mImage);
            holder.mView.setOnClickListener(view -> {
                mListener.onDogClicked(holder.mItem, mKeys.get(position));
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mName;
            public final ImageView mImage;
            public Item mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mName = (TextView) view.findViewById(R.id.name);
                mImage = (ImageView) view.findViewById(R.id.image);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mName.getText() + "'";
            }
        }
    }

    public interface DogListListener {
        void onDogClicked(Item item, String key);
    }
}
