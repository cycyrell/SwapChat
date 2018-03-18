package xyz.teamcatalyst.breedr.lovematch;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import xyz.teamcatalyst.breedr.GlideApp;
import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.api.FirebaseApi;
import xyz.teamcatalyst.breedr.data.Item;
import xyz.teamcatalyst.breedr.data.UserFeedback;
import xyz.teamcatalyst.breedr.data.UserProfile;
import xyz.teamcatalyst.breedr.databinding.ItemDetailBinding;
import xyz.teamcatalyst.breedr.profile.FeedbackItem;
import xyz.teamcatalyst.breedr.profile.ViewProfileActivity;

/**
 * A fragment representing a single Data detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    public Item mItem;
    private ItemDetailBinding dataBinding;
    private FastItemAdapter fastAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = (Item) getArguments().getSerializable(ARG_ITEM);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        dataBinding = DataBindingUtil.inflate(inflater, R.layout.item_detail, container, false);
        View rootView = dataBinding.getRoot();
        dataBinding.setVm(this);

        if (!TextUtils.isEmpty(mItem.getItemImage())) {
            DefaultSliderView mainImage = new DefaultSliderView(getContext());
            mainImage.image(mItem.getItemImage());
            mainImage.setScaleType(BaseSliderView.ScaleType.CenterCrop);
            dataBinding.image.addSlider(mainImage);
        }

        for (String image : mItem.getImageList()) {
            DefaultSliderView defaultSliderView = new DefaultSliderView(getContext());
            defaultSliderView.image(image);
            defaultSliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
            dataBinding.image.addSlider(defaultSliderView);
        }

        fastAdapter = new FastItemAdapter();
        dataBinding.feedbackList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        dataBinding.feedbackList.setAdapter(fastAdapter);

        FirebaseApi.getInstance().getDatabase().getReference("user_profile").child(mItem.getOwnerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                        if (userProfile == null) return;
                        if (userProfile.getProfileUrl() != null) {
                            GlideApp.with(getContext()).load(userProfile.getProfileUrl()).fitCenter().centerCrop()
                                    .into(dataBinding.userProfileImage);
                        }

                        userProfile.setUserId(mItem.getOwnerId());
                        dataBinding.userProfileImage.setOnClickListener(view -> ViewProfileActivity.start(getContext(), userProfile, false));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        FirebaseUser me = FirebaseApi.getUser();
        DatabaseReference dogFeedback = FirebaseApi.getInstance().getDatabase().getReference("feedback").child(mItem.getOwnerId() + mItem.getName());
        dogFeedback.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fastAdapter.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    fastAdapter.add(new FeedbackItem(snapshot.getValue(UserFeedback.class)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return rootView;
    }

    public void onLikeClicked() {
        getActivity().setResult(100);
        getActivity().finish();
    }
}
