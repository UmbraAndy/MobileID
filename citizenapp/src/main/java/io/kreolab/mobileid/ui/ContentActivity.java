/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.ui;

import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.kreolab.mobileid.R;
import io.kreolab.mobileid.datastore.IdDataContract;
import io.kreolab.mobileid.datastore.PersonDBHelper;
import io.kreolab.mobileid.model.IdData;
import io.kreolab.mobileid.model.RegistrationCenter;
import io.kreolab.mobileid.model.RegistrationState;
import io.kreolab.mobileid.utils.ImageUtils;
import timber.log.Timber;

/**
 * Activity that servers as a container for the fragments of the apps screen
 */
public class ContentActivity extends AppCompatActivity
        implements RequestModificationFragment.OnRequestSubmitListener,
        ShareIDDevicesFragment.OnDeviceSelectedListener,
        SelectLocationFragment.OnItemSelectedListener,
        RegistrationCenterLocationFragment.OnLaunchLocationListener,
        ShowIdQrCodeFragment.QRCodeGeneration {


    @BindView(R.id.idLayout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.navBar)
    NavigationView mNavigationView;


    private CircleImageView mProfileImg;

    private TextView mNavHeaderIdNumberTxt;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private FragmentManager mFragmentManager;


    private int mClickedNavBarItemId = 0;

    private View mContentViewForSnackBar;

    private Map<String, List> mStatesRegistrationCenter;

    private FirebaseDatabase mFirestoreDatabase;
    private SQLiteDatabase mLocalSqLiteDatabase;

    private IdData mIdData;

    private int mLastSelectedMenu = R.id.showMobileIdMnu;
    public static final String LAST_MENU_CLICKED = "last_menu_clicked";
    public static final String FIREBASE_PERSON_COLLECTION = "persons";
    public static final String FIREBASE_REGISTRATION_CENTER_COLLECTION = "registrationPlaces";


    //fragments tags
    private static final String ID_FRAGMENT_TAG = "IdFragment";
    private static final String QR_CODE_FRAGMENT_TAG = "QrCodeFragment";
    private static final String MODIFICATION_FRAGMENT_TAG = "ModificationFragment";
    private static final String REGISTRATION_CENTER_FRAGMENT_TAG = "RegistrationFragment";
    private static final String SELECT_DEVICE_FRAGMENT_TAG = "SelectDeviceFragment";
    private static final String SELECT_LOCATION_FRAGMENT_TAG = "SelectLocationFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirestoreDatabase = FirebaseDatabase.getInstance();
        //PersonDBHelper personDBHelper = new PersonDBHelper(this);
        //mLocalSqLiteDatabase = personDBHelper.getWritableDatabase();
        setUpRegistrationCenters();
        setContentView(R.layout.activity_content);
        setSupportActionBar(mToolbar);
        ButterKnife.bind(this);

        //set up content for snack bar
        mContentViewForSnackBar = findViewById(android.R.id.content);

        //set up actionbar
        setUpActionbar();

        setUpNavBar();
        getIdDataFromIntent();

        //get last menu from saved instance
        getLastSelectedMenu(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();

    }

    private void getLastSelectedMenu(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mLastSelectedMenu = savedInstanceState.getInt(ContentActivity.LAST_MENU_CLICKED);
        }
        if (mLastSelectedMenu == 0) {
            mLastSelectedMenu = R.id.showMobileIdMnu;
        }
    }

    private void setUpRegistrationCenters() {
        mStatesRegistrationCenter = new HashMap<>();
        //TODO use firebase
        DatabaseReference registrationCentersReference =  mFirestoreDatabase.getReference(FIREBASE_REGISTRATION_CENTER_COLLECTION);
        registrationCentersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Timber.d("XXXXRegCenters " + dataSnapshot);
                    for (DataSnapshot statePositionDocument : dataSnapshot.getChildren()) {//for each states position
                        Timber.d("XXXXStates " +statePositionDocument.getKey());
                        for(DataSnapshot stateDocument: statePositionDocument.getChildren()) {
                            RegistrationState stateDetails = new RegistrationState();
                            stateDetails.setName(stateDocument.getKey());
                            for(DataSnapshot centerDocument : stateDocument.getChildren()) {
                                RegistrationCenter registrationCenter = centerDocument.getValue(RegistrationCenter.class);
                                stateDetails.getCenters().add(registrationCenter);
                                Timber.d("XXXXCenter  " + centerDocument.getKey()
                                        + " => " + registrationCenter);
                            }
                            mStatesRegistrationCenter.put(stateDetails.getName(), stateDetails.getCenters());
                        }
                    }

                }
              else {
                    Timber.d("XXXXXError getting registration centers documents: " );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.d("XXXXXRegFetchFail: " + databaseError.getMessage());
            }
        });
//        CollectionReference registrationCentersCollectionReference = mFirestoreDatabase.collection(FIREBASE_REGISTRATION_CENTER_COLLECTION);
//        registrationCentersCollectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        RegistrationState stateDetails = document.toObject(RegistrationState.class);
//                        Timber.d("XXXXReg: " + document.getId() + " => " + stateDetails);
//                        mStatesRegistrationCenter.put(stateDetails.getName(), stateDetails.getCenters());
//                        //for(r)
//                    }
//                } else {
//                    Timber.d("XXXXXError getting documents: " + task.getException().getMessage());
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Timber.d("XXXXXRegFetchFail: " + e.getMessage());
//            }
//        });
    }

    private void setUpActionbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    private void getIdDataFromIntent() {
        Intent startIntent = getIntent();
        //get IdData from intent if available
        if (startIntent != null && startIntent.getParcelableExtra(SignInActivity.ID_DATA) != null) ;
        {
            mIdData = startIntent.getParcelableExtra(SignInActivity.ID_DATA);
            //set navbar content
            View headerView = mNavigationView.getHeaderView(0);
            mProfileImg = headerView.findViewById(R.id.profileImg);
            mProfileImg.setImageBitmap(ImageUtils.convertBytesToBitmap(mIdData.getPhoto()));
            mNavHeaderIdNumberTxt.setText(mIdData.getIdNumber());
        }
    }

    private void setUpNavBar() {
        mNavigationView.setItemIconTintList(null);
        DrawerLayout.DrawerListener drawerListener = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.accessibility_open_nav, R.string.accessibility_open_nav) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (mClickedNavBarItemId != 0) {
                    handleNavItemClick(mClickedNavBarItemId);
                }
            }
        };
        mDrawerLayout.addDrawerListener(drawerListener);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //set the clicked item
                mClickedNavBarItemId = item.getItemId();
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        View headerView = mNavigationView.getHeaderView(0);
        mProfileImg = headerView.findViewById(R.id.profileImg);
        mNavHeaderIdNumberTxt = headerView.findViewById(R.id.navHeaderIdNumberTxt);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleNavItemClick(mLastSelectedMenu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_MENU_CLICKED, mLastSelectedMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void requestSubmit(final IdData idData) {
        //save photo  byte for later because it's not serializable to firebase firestore
        final byte[] photoByte = idData.getPhoto();
        idData.setPhoto(null);
        //TODO use firbase
//        DocumentReference personDocumentReference = mFirestoreDatabase.collection(FIREBASE_PERSON_COLLECTION).document(mIdData.getIdNumber());
//        personDocumentReference.set(idData, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    if (updateIdDataToLocalDB(idData) > 0) {
//                        mIdData = idData;
//                        mIdData.setPhoto(photoByte);
//                        handleNavItemClick(R.id.showMobileIdMnu);
//                    } else {
//                        Timber.d("Could not save data to local db");
//                    }
//
//                } else {
//                    //show error message
//                    Snackbar.make(mContentViewForSnackBar, "Update was not successful", Snackbar.LENGTH_SHORT).show();
//                }
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Snackbar.make(mContentViewForSnackBar, e.getMessage(), Snackbar.LENGTH_SHORT).show();
//            }
//        });

    }

    @Override
    public void notifyValidation(String validationMessage) {
        Snackbar.make(mContentViewForSnackBar, validationMessage, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void deviceSelected(BluetoothDevice device) {
        //This feature is supposed to work with the verification app
        // for the Mobile so I did'nt bother implementing it for this submission
        Snackbar.make(mContentViewForSnackBar, "Device selected" + device.getName(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void serviceValidation(String message) {
        Snackbar.make(mContentViewForSnackBar,message,Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void locationSelected(String location) {
        Snackbar.make(mContentViewForSnackBar, location + " was selected.", Snackbar.LENGTH_SHORT).show();
        RegistrationCenterLocationFragment registrationCenterLocationFragment = (RegistrationCenterLocationFragment) getSupportFragmentManager().findFragmentById(R.id.contentFragmentContainer);
        //fetch state center location
        registrationCenterLocationFragment.addMarkersAndMoveToLocation(mStatesRegistrationCenter.get(location));

    }

    @Override
    public void launchSelection() {
        DialogFragment selectLocationFragment = new SelectLocationFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray(SelectLocationFragment.STATE_KEY_ARRAY, mStatesRegistrationCenter.keySet().toArray(new String[0]));
        selectLocationFragment.setArguments(bundle);
        selectLocationFragment.show(getSupportFragmentManager(), SELECT_LOCATION_FRAGMENT_TAG);
    }

    @Override
    public void generationFailed() {
        Snackbar.make(mContentViewForSnackBar, "QR code could not be generated", Snackbar.LENGTH_SHORT).show();
    }

    private void handleNavItemClick(int itemId) {
        mLastSelectedMenu = itemId;
        switch (itemId) {
            case R.id.showMobileIdMnu:
                launchShowId();
                break;
            case R.id.registrationCentersMnu:
                launchRegistrationCenter();
                break;
            case R.id.requestForModificationMnu:
                launchModification();
                break;
            case R.id.displayQrCodeMnu:
                launchQrCode();
                break;
            case R.id.sendIdToDeviceMnu:
                launchChooseDevice();
                break;
            case R.id.signOutMnu:
                //finish activity
                finish();
                break;

        }
    }


    private void launchChooseDevice() {
        if (mFragmentManager.findFragmentByTag(SELECT_DEVICE_FRAGMENT_TAG) != null) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.contentFragmentContainer, new ShareIDDevicesFragment(), SELECT_DEVICE_FRAGMENT_TAG);
        transaction.commit();
        setActionBarTitle(R.string.shareIdTitle);
    }


    private void launchShowId() {
        if (mFragmentManager.findFragmentByTag(ID_FRAGMENT_TAG) != null) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        IDFragment idFragment = new IDFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(IDFragment.ID_DATA, mIdData);
        idFragment.setArguments(bundle);
        transaction.replace(R.id.contentFragmentContainer, idFragment, ID_FRAGMENT_TAG);
        transaction.commit();
        setActionBarTitle(R.string.mobileIdTitle);
    }

    private void launchQrCode() {
        if (mFragmentManager.findFragmentByTag(QR_CODE_FRAGMENT_TAG) != null) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        ShowIdQrCodeFragment qrCodeFragment = new ShowIdQrCodeFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ShowIdQrCodeFragment.ID_DATA, mIdData);
        qrCodeFragment.setArguments(bundle);
        transaction.replace(R.id.contentFragmentContainer, qrCodeFragment, QR_CODE_FRAGMENT_TAG);
        transaction.commit();
        setActionBarTitle(R.string.qrCodeTitle);
    }

    private void setActionBarTitle(int qrCodeTitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(qrCodeTitle);
        }
    }

    private void launchModification() {
        if (mFragmentManager.findFragmentByTag(MODIFICATION_FRAGMENT_TAG) != null) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putParcelable(RequestModificationFragment.ID_DATA, mIdData);
        RequestModificationFragment fragment = new RequestModificationFragment();
        fragment.setArguments(bundle);
        transaction.replace(R.id.contentFragmentContainer, fragment, MODIFICATION_FRAGMENT_TAG);

        transaction.commit();
        setActionBarTitle(R.string.modificationRequestTitle);
    }

    private void launchRegistrationCenter() {
        if (mFragmentManager.findFragmentByTag(REGISTRATION_CENTER_FRAGMENT_TAG) != null) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.contentFragmentContainer, new RegistrationCenterLocationFragment(), REGISTRATION_CENTER_FRAGMENT_TAG);
        transaction.commit();
        setActionBarTitle(R.string.registrationCenterTitle);
    }
}
