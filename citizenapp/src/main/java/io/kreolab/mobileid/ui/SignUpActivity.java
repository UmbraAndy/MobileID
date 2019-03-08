package io.kreolab.mobileid.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.kreolab.mobileid.R;
import io.kreolab.mobileid.datastore.IdDataContract;
import io.kreolab.mobileid.model.IdData;
import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity {


    private static final String IMAGE_FILE_EXTENSION = ".jpg";
    private  static  final String PERSONS_REFERENCE ="persons";

    @BindView(R.id.signUpBtn)
    Button mSignUpBtn;

    @BindView(R.id.signUpLink)
    TextView mSignInLink;

    @BindView(R.id.idNumberTxt)
    EditText mIdTxt;

    @BindView(R.id.fetchPbr)
    ProgressBar mFetchPbr;

    @BindView(R.id.fetchLoadingTxt)
    TextView mFetchLoadingTxt;

    @BindView(android.R.id.content)
    View mContentViewForSnackBar;

    private FirebaseDatabase mFirestoreDatabase;
    private FirebaseStorage mFirebaseStorage;

    public static final String SING_UP_SUCCESS = "sign_up_successful";
    private static final String SUCCESS = "success";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        initGui();
    }

    private void initGui() {
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        //Hide loading
        displayLoading(false);
        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();

            }
        });
        mSignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignIn();
            }
        });
    }

    private void displayLoading(boolean display) {

        int visibility = View.INVISIBLE;
        if (display) {
            visibility = View.VISIBLE;
        }

        mFetchPbr.setVisibility(visibility);
        mFetchLoadingTxt.setVisibility(visibility);
    }

    private void enableInput(boolean enable) {
        mIdTxt.setEnabled(enable);
        mSignInLink.setEnabled(enable);
        mSignUpBtn.setEnabled(enable);
    }

    private void init() {
        mFirestoreDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
    }


    private void signUp() {
        //validate user input
        if (TextUtils.isEmpty(mIdTxt.getText())) {
            mIdTxt.setError("User ID must be provided.");
            return;
        }

        //disable all inputs
        enableInput(false);

        //show progress bar
        displayLoading(true);

        //get user ID
        String userId = mIdTxt.getText().toString();

        //fetch data from firebase backend
        fetchIdFromBackend(userId);
    }

    private void fetchIdFromBackend(final String userId) {
        DatabaseReference personsReference = mFirestoreDatabase.getReference(PERSONS_REFERENCE)
                .child(userId);
        personsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot personDocumentSnapshot) {
                    if (personDocumentSnapshot.exists()) {// document exist, save in mFirestoreDatabase
                        final IdData personIdData = personDocumentSnapshot.getValue(IdData.class);
                        //fetch passport image
                        if (personIdData != null) {
                            mFirebaseStorage.getReference().child(personIdData.getIdNumber() + IMAGE_FILE_EXTENSION)
                                    .getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    //Save data to local mFirestoreDatabase
                                    //personIdData.setIdNumber(personDocumentSnapshot.getId());
                                    personIdData.setPhoto(bytes);
                                    long id = savePersonDataToDB(personIdData);

                                    if (id > 0) {//insert success

                                        //delete all old id records
                                        deleteOldRecords(id);
                                        fetchSuccessHandleGui();
                                    } else {//insert failed

                                        fetchFailedHandleGui("Could not save ID data. Please try again");
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    fetchFailedHandleGui("Error fetching ID photo");

                                }
                            });
                        }

                    } else {//
                        Timber.w("XXXNotFetched:" + userId);
                        //hide progress bar
                        fetchFailedHandleGui("ID data not found for " +userId);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Timber.d("XXXFetchError "+ databaseError);
                fetchFailedHandleGui("ID data not found for " + userId);

            }
        });
    }

    private int deleteOldRecords(long id) {
        int numberOfRowsDeleted = getContentResolver().delete(IdDataContract.PersonalData.CONTENT_URI, IdDataContract.PersonalData._ID + " != ?", new String[]{String.valueOf(id)});
        Timber.d("XXXXNumber of old records deleted " + numberOfRowsDeleted);
        return numberOfRowsDeleted;
    }

    private void fetchFailedHandleGui(String errorMessage) {
        //hide progress bar
        displayLoading(false);
        //enable input
        enableInput(true);
        Snackbar.make(mContentViewForSnackBar, errorMessage, Snackbar.LENGTH_LONG).show();
    }

    private void fetchSuccessHandleGui() {
        Intent signIntent = new Intent(this, SignInActivity.class);
        signIntent.putExtra(SING_UP_SUCCESS, SUCCESS);
        signIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(signIntent);
    }

    private long savePersonDataToDB(IdData personIdData) {
        long savedId = 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put(IdDataContract.PersonalData.COLUMN_ID_NUMBER, personIdData.getIdNumber());
        contentValues.put(IdDataContract.PersonalData.COLUMN_SURNAME, personIdData.getSurname());
        contentValues.put(IdDataContract.PersonalData.COLUMN_FIRST_NAME, personIdData.getFirstname());
        contentValues.put(IdDataContract.PersonalData.COLUMN_OTHER_NAMES, personIdData.getOther_names());
        contentValues.put(IdDataContract.PersonalData.COLUMN_DATE_OF_BIRTH, personIdData.getDate_of_birth());
        contentValues.put(IdDataContract.PersonalData.COLUMN_NATIONALITY, personIdData.getNationality());
        contentValues.put(IdDataContract.PersonalData.COLUMN_DATE_OF_ISSUE, personIdData.getDate_of_issue());
        contentValues.put(IdDataContract.PersonalData.COLUMN_DATE_OF_EXPIRY, personIdData.getDate_of_expiry());
        contentValues.put(IdDataContract.PersonalData.COLUMN_PHOTO, personIdData.getPhoto());
        contentValues.put(IdDataContract.PersonalData.COLUMN_PIN, personIdData.getPin());

        Uri personInsertUri = getContentResolver().insert(IdDataContract.PersonalData.CONTENT_URI, contentValues);
        if (personInsertUri != null) {
            Timber.d("XXXXInsertUri :" + personInsertUri);
            savedId = Long.parseLong(personInsertUri.getPathSegments().get(1));
        }
        return savedId;
    }


    private void launchSignIn() {
        Intent signIntent = new Intent(this, SignInActivity.class);
        startActivity(signIntent);

    }
}
