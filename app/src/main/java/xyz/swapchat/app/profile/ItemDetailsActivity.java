package xyz.teamcatalyst.breedr.profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import xyz.teamcatalyst.breedr.GlideApp;
import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.data.Item;

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 02/07/2017
 */
public class ItemDetailsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.et_item_name) EditText mEtDogName;
    @BindView(R.id.et_item_category) Spinner mEtItemCategory;
    @BindView(R.id.et_item_gender) Spinner mSpinnerGender;
    @BindView(R.id.et_item_age) EditText mEtDogAge;
    @BindView(R.id.et_item_city) EditText mEtDogCity;
    @BindView(R.id.et_item_about) EditText mEtDogAbout;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.btn_item_delete) Button mBtnDeleteDog;
    @BindView(R.id.iv_item_image) ImageView mIvDogImage;
    @BindView(R.id.rv_item_image_list) RecyclerView mRvItemImageList;
    @BindView(R.id.add_image) ImageView addDogButton;
    private Item item;
    private String key;
    private DatabaseReference myItems;
    private StorageReference mStorageRef;
    private String downloadUrl;
    private List<String> itemImageList = new ArrayList<>();
    private ItemImageAdapter itemImageAdapter;
    private boolean isViewOnly;

    public static void start(Context context, Item item, String key, boolean isViewOnly) {
        Intent starter = new Intent(context, ItemDetailsActivity.class);
        starter.putExtra("item", item);
        starter.putExtra("key", key);
        starter.putExtra("isViewOnly", isViewOnly);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        isViewOnly = getIntent().getBooleanExtra("isViewOnly", false);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_gender, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinnerGender.setAdapter(adapter);
        mSpinnerGender.setSelection(0);

        ArrayAdapter<CharSequence> itemCategoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_category, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEtItemCategory.setAdapter(itemCategoryAdapter);
        mEtItemCategory.setSelection(0);

        mRvItemImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        itemImageAdapter = new ItemImageAdapter(itemImageList, url -> {
            if (isViewOnly) return;
            new AlertDialog.Builder(ItemDetailsActivity.this)
                    .setMessage("Delete image?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        itemImageList.remove(url);
                        itemImageAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> {
                        dialogInterface.dismiss();

                    })
                    .show();
        });
        mRvItemImageList.setAdapter(itemImageAdapter);

        if (getIntent().hasExtra("item")) {
            item = (Item) getIntent().getExtras().getSerializable("item");
            key = getIntent().getStringExtra("key");
            mEtDogName.setText(item.getName());
            downloadUrl = item.getItemImage();
            if (downloadUrl == null) downloadUrl = "";
            int dogBreedPos = adapter.getPosition(item.getCategory());
            if (dogBreedPos != -1) {
                mEtItemCategory.setSelection(dogBreedPos);
            }

            String[] breeds = getResources().getStringArray(R.array.spinner_category);
            for (int i = 0; i < breeds.length; i++) {
                if (breeds[i].equals(item.getCategory())) {
                    mEtItemCategory.setSelection(i);
                }
            }

            String[] gender = getResources().getStringArray(R.array.spinner_gender);
            for (int i = 0; i < gender.length; i++) {
                if (gender[i].equals(item.getGender())) {
                    mSpinnerGender.setSelection(i);
                }
            }

            if (!TextUtils.isEmpty(item.getAge())) {
                String age = item.getAge().replace("Year(s)", "").replace("Month(s)", "").trim();
                if (TextUtils.isEmpty(age)) {
                    age = "1";
                }
                mEtDogAge.setText(age);
            }
            mEtDogCity.setText(item.getCity());
            mEtDogAbout.setText(item.getAbout());
            mBtnDeleteDog.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(item.getItemImage())) {
                GlideApp.with(ItemDetailsActivity.this).load(item.getItemImage()).centerCrop().into(mIvDogImage);
            }

            itemImageList.addAll(item.getImageList());
            itemImageAdapter.notifyDataSetChanged();


            if(isViewOnly) {
                mEtDogName.setEnabled(false);
                mEtItemCategory.setEnabled(false);
                mSpinnerGender.setEnabled(false);
                mEtDogAge.setEnabled(false);
//                mDogAgeYear.setEnabled(false);
                mEtDogCity.setEnabled(false);
                mEtDogAbout.setEnabled(false);
                addDogButton.setVisibility(View.GONE);
                mBtnDeleteDog.setVisibility(View.GONE);
                mFab.setVisibility(View.GONE);
            }
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myItems = database.getReference("user_items").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mFab.setOnClickListener(view -> {
            if (TextUtils.isEmpty(downloadUrl)) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.add_an_image_for_item)
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .show();
                return;
            }
            if (key == null) {
                key = myItems.push().getKey();
            }

            String age = mEtDogAge.getText().toString().replace("Year(s)", "").replace("Month(s)", "").trim();
            if (TextUtils.isEmpty(age)) {
                age = "1";
            }

            item = new Item(
                    mEtDogName.getText().toString(),
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    downloadUrl,
                    mEtItemCategory.getSelectedItem().toString(),
                    mSpinnerGender.getSelectedItem().toString(),
                    "",
                    mEtDogCity.getText().toString(),
                    mEtDogAbout.getText().toString(),
                    key,
                    itemImageList
            );
            myItems.child(key).setValue(item).addOnCompleteListener(task -> Snackbar.make(view, R.string.item_added, Snackbar.LENGTH_LONG).setAction("OK", view1 -> finish()).show());
        });
    }

    @OnClick(R.id.btn_item_delete)
    public void onDeleteClicked() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    myItems.child(key).removeValue((databaseError, databaseReference) -> {
                        finish();
                    });
                })
                .setNegativeButton("No", ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }))
                .show();

    }

    @OnClick(R.id.iv_item_image)
    public void onDogImageClicked() {
        if (isViewOnly) return;
        EasyImage.openChooserWithGallery(this, "Upload item picture", 1);
    }

    @OnClick(R.id.add_image)
    public void onAddImageClicked() {
        EasyImage.openChooserWithGallery(this, "Upload new image", 2);
    }

    @OnClick(R.id.add_vaccine_proof_image)
    public void onAddVaccineProofImageClicked() {
        EasyImage.openChooserWithGallery(this, "Upload vaccine proof", 3);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @SuppressWarnings({"VisibleForTests", "ConstantConditions"})
            @Override
            public void onImagePicked(File file, EasyImage.ImageSource imageSource, int type) {
                Uri uri = Uri.fromFile(file);
                StorageReference riversRef = mStorageRef.child("dogs/profile_image/" + UUID.randomUUID() + ".jpg");

                riversRef.putFile(uri)
                        .addOnSuccessListener(taskSnapshot -> {
                            if (type == 1) {
                                downloadUrl = taskSnapshot.getDownloadUrl().toString();
                                GlideApp.with(ItemDetailsActivity.this).load(downloadUrl).centerCrop().into(mIvDogImage);
                            } else if (type == 2) {
                                itemImageList.add(taskSnapshot.getDownloadUrl().toString());
                                itemImageAdapter.notifyDataSetChanged();
                            } else if (type == 3) {
                            }
                        });
            }
        });
    }

    public interface OnDogImageClickListener {
        void onDogImageClicked(String url);
    }

    public class ItemImageAdapter extends RecyclerView.Adapter<ItemImageAdapter.ViewHolder> {

        private final List<String> mValues;
        private final OnDogImageClickListener onDogImageClickListener;

        public ItemImageAdapter(List<String> items, OnDogImageClickListener onDogImageClickListener) {
            mValues = items;
            this.onDogImageClickListener = onDogImageClickListener;
        }

        @Override
        public ItemImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ItemImageAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mName.setText("");
            holder.mView.setOnClickListener(view -> onDogImageClickListener.onDogImageClicked(holder.mItem));
            GlideApp.with(holder.mView.getContext()).load(mValues.get(position)).fitCenter().centerCrop().into(holder.mImage);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mName;
            public final ImageView mImage;
            public String mItem;

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
}
