package io.kreolab.mobileid.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.kreolab.mobileid.R;
import io.kreolab.mobileid.datastore.IdDataContract;
import io.kreolab.mobileid.model.IdData;

/**
 * Activity for signing in to the application
 */
public class SignInActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FETCH_PERSON_LOADER_ID = 100;

    @BindView(R.id.signInBtn)
    Button mSignInBtn;

    @BindView(R.id.signUpLink)
    Button mSignUpLink;

    @BindView(R.id.pinTxt)
    EditText mPinTxt;


    @BindView(R.id.signInPbr)
    ProgressBar mSignInPbr;

    @BindView(R.id.signInLoadingTxt)
    TextView mSignInLoadingTxt;


    @BindView(android.R.id.content)
    View mContentViewForSnackBar;

    private IdData mIdData;

    public static final String ID_DATA = "id_data";
    private String infoMessage;

    private int mFragmentMenuToRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        initGui();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        if (intent != null && intent.getStringExtra(SignUpActivity.SING_UP_SUCCESS) != null) {
            infoMessage = "Sign up for Mobile ID successful. Please log in";
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (infoMessage != null) {
            Snackbar.make(mContentViewForSnackBar, infoMessage, Snackbar.LENGTH_LONG).show();
        }
    }

    private void init() {
        Intent startIntent = getIntent();
        //check if intent has selected fragment to show
        if (startIntent != null) {
            mFragmentMenuToRequest = startIntent.getIntExtra(ContentActivity.LAST_MENU_CLICKED, 0);
        }
    }


    private void initGui() {
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        //get intent used to start the activity
        Intent startIntent = getIntent();
        if (startIntent != null && startIntent.getStringExtra(SignUpActivity.SING_UP_SUCCESS) != null) {
            infoMessage = "Sign up for Mobile ID successful. Please log in";
        }

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        mSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignUp();
            }
        });
        displayLoading(false);
    }

    private void displayLoading(boolean display) {
        int visibility = View.INVISIBLE;

        if (display) {
            visibility = View.VISIBLE;
        }

        mSignInPbr.setVisibility(visibility);
        mSignInLoadingTxt.setVisibility(visibility);
    }

    private void enableInput(boolean enable) {
        mPinTxt.setEnabled(enable);
        mSignInBtn.setEnabled(enable);
        mSignUpLink.setEnabled(enable);
    }

    private void signInSuccessHandleGui() {
        launchMainScreen();
    }

    private void signInFailureHandleGui(String errorMessage) {
        displayLoading(false);
        enableInput(true);
        showSignInFailedMessage(errorMessage);
    }


    //perform sign in by calling async task to call server.
    private void signIn() {
        //validate user input
        if (TextUtils.isEmpty(mPinTxt.getText())) {
            mPinTxt.setError("PIN must be provided.");
            return;
        }

        displayLoading(true);
        enableInput(false);

        //initialize cursor loader to
        //get user from the db
        getSupportLoaderManager().initLoader(FETCH_PERSON_LOADER_ID,null,this);
    }

    //launch the main activity
    private void launchMainScreen() {
        Intent contentActivityIntent = new Intent(this, ContentActivity.class);
        //set fragment menu to select
        contentActivityIntent.putExtra(ContentActivity.LAST_MENU_CLICKED, mFragmentMenuToRequest);
        contentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        contentActivityIntent.putExtra(ID_DATA, mIdData);
        startActivity(contentActivityIntent);

    }

    //launch the sign up screen
    private void launchSignUp() {
        Intent signUpActivityIntent = new Intent(this, SignUpActivity.class);
        startActivity(signUpActivityIntent);
    }


    private void showSignInFailedMessage(String errorMessage) {
        Snackbar.make(mContentViewForSnackBar, errorMessage, Snackbar.LENGTH_LONG).show();

    }


//    @Override
//    public void fetchIdDataSuccessful(IdData idData) {
//        mIdData = idData;
//        //authenticateUser();
//        signInSuccessHandleGui();
//    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader cursorLoader = null;

        String pin = mPinTxt.getText().toString();
        if(TextUtils.isEmpty(pin)){
            mPinTxt.setError("PIN cannot be empty");
            return  cursorLoader;
        }
        switch (id) {
            case FETCH_PERSON_LOADER_ID:
                String whereClause = IdDataContract.PersonalData.COLUMN_PIN + "=?";
                cursorLoader = new CursorLoader(this, IdDataContract.PersonalData.CONTENT_URI,
                        new String[]{"*"}, whereClause, new String[]{pin}, null);
                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(data.getCount() > 0)
        {
            data.moveToFirst();
            String idNumber = data.getString(data.getColumnIndex(IdDataContract.PersonalData.COLUMN_ID_NUMBER));
            String surname = data.getString(data.getColumnIndex(IdDataContract.PersonalData.COLUMN_SURNAME));
            String firstname = data.getString(data.getColumnIndex(IdDataContract.PersonalData.COLUMN_FIRST_NAME));
            String otherNames = data.getString(data.getColumnIndex(IdDataContract.PersonalData.COLUMN_OTHER_NAMES));
            long dateOfBirth = data.getLong(data.getColumnIndex(IdDataContract.PersonalData.COLUMN_DATE_OF_BIRTH));
            String nationality = data.getString(data.getColumnIndex(IdDataContract.PersonalData.COLUMN_NATIONALITY));
            long dateOfIssue = data.getLong(data.getColumnIndex(IdDataContract.PersonalData.COLUMN_DATE_OF_ISSUE));
            long dateOfExpiry = data.getLong(data.getColumnIndex(IdDataContract.PersonalData.COLUMN_DATE_OF_EXPIRY));
            byte[] photo = data.getBlob(data.getColumnIndex(IdDataContract.PersonalData.COLUMN_PHOTO));
            mIdData = new IdData();
            mIdData.setIdNumber(idNumber);
            mIdData.setSurname(surname);
            mIdData.setFirstname(firstname);
            mIdData.setOther_names(otherNames);
            mIdData.setDate_of_birth(dateOfBirth);
            mIdData.setNationality(nationality);
            mIdData.setDate_of_issue(dateOfIssue);
            mIdData.setDate_of_expiry(dateOfExpiry);
            mIdData.setPhoto(photo);
            signInSuccessHandleGui();
        }
        else{
            signInFailureHandleGui("Failed to find ID data on this device");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

//    @Override
//    public void fetchIdDataFailed(String errorMessage) {
//        signInFailureHandleGui(errorMessage);
//    }
}
