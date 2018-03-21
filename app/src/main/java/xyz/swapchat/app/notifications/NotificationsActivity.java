package xyz.teamcatalyst.breedr.notifications;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.HashSet;
import java.util.Set;

import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.api.FirebaseApi;
import xyz.teamcatalyst.breedr.data.History;
import xyz.teamcatalyst.breedr.data.Profile;
import xyz.teamcatalyst.breedr.data.UserProfile;
import xyz.teamcatalyst.breedr.profile.ViewProfileActivity;

public class NotificationsActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public static SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public static ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(0);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private FastItemAdapter fastAdapter;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
            fastAdapter = new FastItemAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(fastAdapter);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            DatabaseReference historyDbRef = database.getReference("history").child(currentUser.getUid());

            fastAdapter.withOnClickListener(new FastAdapter.OnClickListener() {
                @Override
                public boolean onClick(View v, IAdapter adapter, IItem item, int position) {
                    UserProfile userProfile = new UserProfile();
                    Profile profile = ((NotificationItem) item).profile;
                    userProfile.setDisplayName(profile.getDisplayName());
                    userProfile.setContactNumber(profile.getContactNumber());
                    userProfile.setEmail(profile.getEmail());
                    userProfile.setUserId(((NotificationItem) item).key);

                    if (((NotificationItem)item).isMatched) {
                        getActivity().finish();
                        ViewProfileActivity.start(getContext(), userProfile, true);
                    } else {
                        getActivity().finish();
                        ViewProfileActivity.start(getContext(), userProfile, false);
                    }
                    return true;
                }
            });

            loadHistory(historyDbRef);

            return rootView;
        }

        private void likeBack(NotificationItem notificationItem, int position) {
            String ownerId = notificationItem.key;
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference history = database.getReference("history");
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference historyDbRef = database.getReference("history").child(currentUser.getUid());
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
                    mViewPager.setAdapter(mSectionsPagerAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void loadHistory(DatabaseReference historyDbRef) {
            fastAdapter.clear();
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.show();
            historyDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    History history = dataSnapshot.getValue(History.class);
                    if (history == null) {
                        progressDialog.dismiss();
                        return;
                    }
                    Set<String> historySet;
                    boolean isMatched = false;
                    boolean canLikeBack = false;
                    Set<String> youLike = history.youLike != null ? history.youLike.keySet() : new HashSet<>();
                    Set<String> likesYou = history.likesYou != null ? history.likesYou.keySet() : new HashSet<>();
                    switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                        case 1:
                            historySet = youLike;
                            break;
                        case 2:
                            historySet = likesYou;
                            canLikeBack = true;
                            break;
                        case 3:
                            historySet = history.matched != null ? history.matched.keySet() : new HashSet<>();
                            isMatched = true;
                            break;
                        default: historySet = new HashSet<>();
                    }

                    progressDialog.dismiss();
                    for (String s : historySet) {
                        boolean finalIsMatched = isMatched;
                        boolean finalCanLikeBack = canLikeBack;
                        FirebaseApi.getInstance().getDatabase().getReference("user_profile").child(s)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot != null) {
                                            Profile profile = dataSnapshot.getValue(Profile.class);
                                            if (profile == null) return;
                                            fastAdapter.add(new NotificationItem(s, profile, /*!youLike.contains(s) && */finalCanLikeBack, finalIsMatched));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressDialog.dismiss();
                }
            });
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.you_liked);
                case 1:
                    return getString(R.string.liked_yours);
                case 2:
                    return getString(R.string.matched);
            }
            return null;
        }
    }
}
