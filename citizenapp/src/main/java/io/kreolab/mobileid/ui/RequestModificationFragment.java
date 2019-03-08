/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.kreolab.mobileid.MobileIdApplication;
import io.kreolab.mobileid.R;
import io.kreolab.mobileid.model.IdData;
import io.kreolab.mobileid.recyclerviewAdapter.DocumentCapturedAdapter;
import io.kreolab.mobileid.recyclerviewAdapter.FingerprintCapturedAdapter;
import timber.log.Timber;

public class RequestModificationFragment extends Fragment
        implements FingerprintCapturedAdapter.FingerItemRemoveClickedListener,
        DocumentCapturedAdapter.DocumentItemRemoveClickedListener {

    private static final int IMAGE_CAPTURE_REQUEST = 100;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static  final String FILE_EXTENSION = ".jpg";
    public static  final String ID_DATA ="id_data";

    @BindView(R.id.surnameTxt)
    EditText mSurnameTxt;

    @BindView(R.id.firstNameTxt)
    EditText mFirstNameTxt;

    @BindView(R.id.otherNamesTxt)
    EditText mOtherNamesTxt;

    @BindView(R.id.captureFingerprintBtn)
    ImageButton mCaptureFingerBtn;

    @BindView(R.id.captureDocumentBtn)
    ImageButton mCaptureDocumentBtn;

    @BindView(R.id.fingerprintSpn)
    Spinner mFingerprintSpn;

    @BindView(R.id.capturedFingerprintRcv)
    RecyclerView mFingerprintCapturedRcv;

    @BindView(R.id.documentCapturedRcv)
    RecyclerView mDocumentCapturedRcv;

    @BindView(R.id.allowedDocumentsSpn)
    Spinner mAllowedDocumentsSpn;

    @BindView(R.id.clearBtn)
    Button mClearBtn;

    @BindView(R.id.submitBtn)
    Button mSubmitBtn;

    //the file name for the image to save
    private String mImageFileName;

    private  IdData mIdData;


    private OnRequestSubmitListener mOnRequestSubmitListener;

    private FingerprintCapturedAdapter mFingerprintCapturedAdapter;

    private DocumentCapturedAdapter mDocumentCapturedAdapter;

//    //holds absolute path to captured  fingerprint images
//    private List<String> mFingerImagesFileList = new ArrayList<>();
//
//    //holds absolute path to captured  document images
//    private List<String> mDocumentImagesFileList = new ArrayList<>();

    private ImageType mCurrentImageType;



    enum ImageType {
        FINGER_PRINT, DOCUMENT
    }


    public RequestModificationFragment( ) {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_modification,
                container, false
        );
        ButterKnife.bind(this, view);


        //set up finger spinner and capture btn
        setupFingerSpinner();

        //set up recycler for fingerprints captured
        setupFingerCaptureRecyclerView();


        //set up document spinner
        setupDocumentSpinner();

        //set up document captured recycler
        setupDocumentCapturedRecycler();

        // get passed in IdData

        if (getArguments() != null) {
            mIdData = getArguments().getParcelable(ID_DATA);
        }

        //if passed in data is not noll, populate UI
        initializeFieldsWithCurrentData();


        mCaptureFingerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set the image type being captured
                mCurrentImageType = ImageType.FINGER_PRINT;
                //get value of spinner, start camera
                String fingerSelected = mFingerprintSpn.getSelectedItem().toString();
                mImageFileName = fingerSelected.replaceAll(" ", "");
                //mFingerprintCapturedAdapter.addFingerprintPosition(fingerSelected);
                takePicture();
            }
        });

        mCaptureDocumentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set the image type being captured
                mCurrentImageType = ImageType.DOCUMENT;
                //get value of spinner, start camera
                String documentSelected = mAllowedDocumentsSpn.getSelectedItem().toString();
                mImageFileName = documentSelected.replaceAll(" ", "");
                //mDocumentCapturedAdapter.addDocument(documentSelected);
                takePicture();
            }
        });

        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reinitialize fields
                initializeFieldsWithCurrentData();

                //clear document related fields
                mDocumentCapturedAdapter.clear();
                mAllowedDocumentsSpn.setSelection(0);

                //clear fingerprint related field
                mFingerprintCapturedAdapter.clear();
                mFingerprintSpn.setSelection(0);

            }
        });


        mSubmitBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //validate that modification was made
                String surname = mSurnameTxt.getText().toString();
                String firstname = mFirstNameTxt.getText().toString();
                String otherNames = mOtherNamesTxt.getText().toString();
                if(TextUtils.isEmpty(surname) && TextUtils.isEmpty(firstname) && TextUtils.isEmpty(otherNames))
                {
                    mSurnameTxt.setError("Should not be empty");
                    mFirstNameTxt.setError("Should not be empty");
                    mOtherNamesTxt.setError("Should not be empty");
                    return;
                }


                if(mFingerprintCapturedAdapter.getItemCount() == 0)
                {
                    mOnRequestSubmitListener.notifyValidation("No valid finger position selected ");
                    return;
                }


                if(mDocumentCapturedAdapter.getItemCount() == 0){
                    mOnRequestSubmitListener.notifyValidation("No valid document selected ");
                    return;
                }


                //if surname is not empty, set it
                if(!TextUtils.isEmpty(surname)){
                    mIdData.setSurname(surname);
                }

                //if firstname is not empty, set it
                if(!TextUtils.isEmpty(firstname)){
                    mIdData.setFirstname(firstname);
                }

                //if other names is not empty, set it
                if(!TextUtils.isEmpty(otherNames)){
                    mIdData.setOther_names(otherNames);
                }
                mOnRequestSubmitListener.requestSubmit(mIdData);
            }
        });

        return view;
    }

    private void initializeFieldsWithCurrentData() {
        if(mIdData != null){
            mSurnameTxt.setText(mIdData.getSurname());
            mFirstNameTxt.setText(mIdData.getFirstname());
            mOtherNamesTxt.setText(mIdData.getOther_names());
        }
    }

    private void setupDocumentSpinner() {
        ArrayAdapter<CharSequence> documentAdapter = ArrayAdapter.createFromResource(getContext()
                , R.array.allowedLegalDocuments, android.R.layout.simple_spinner_item);
        documentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAllowedDocumentsSpn.setAdapter(documentAdapter);
    }

    private void setupFingerCaptureRecyclerView() {
        mFingerprintCapturedAdapter = new FingerprintCapturedAdapter(this);
        mFingerprintCapturedRcv.setAdapter(mFingerprintCapturedAdapter);
        mFingerprintCapturedRcv.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    private void setupDocumentCapturedRecycler() {
        mDocumentCapturedAdapter = new DocumentCapturedAdapter(this);
        mDocumentCapturedRcv.setAdapter(mDocumentCapturedAdapter);
        mDocumentCapturedRcv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupFingerSpinner() {
        ArrayAdapter<CharSequence> fingerPositionAdapter = ArrayAdapter.createFromResource(getContext()
                , R.array.fingerprintPositions, android.R.layout.simple_spinner_item);
        fingerPositionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFingerprintSpn.setAdapter(fingerPositionAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnRequestSubmitListener) {
            mOnRequestSubmitListener = (OnRequestSubmitListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRequestSubmitListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == Activity.RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //save status to appropriate  GUI
            switch (mCurrentImageType) {
                case FINGER_PRINT:
                    //add fingerprint to gui
                    String fingerSelected = mFingerprintSpn.getSelectedItem().toString();
                    mFingerprintCapturedAdapter.addFingerprintPosition(fingerSelected);
                    break;
                case DOCUMENT:
                    //add document to gui
                    String documentSelected = mAllowedDocumentsSpn.getSelectedItem().toString();
                    mDocumentCapturedAdapter.addDocument(documentSelected);
                    break;

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                }
                break;
        }
    }

    private void takePicture() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted so request for permission
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {//permission already granted
            startCamera();
        }

    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check if application exist for picture capture
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoImageFile =
                    new File(getContext()
                            .getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            TextUtils.concat(selectionToFileName(mImageFileName), FILE_EXTENSION).toString());
            //check android version to avoid FileUriExposedException
            //Idea gotten from medium
            // https://medium.com/@ali.muzaffar/what-is-android-os-fileuriexposedexception-and-what-you-can-do-about-it-70b9eb17c6d0
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoImageFile));
            } else {

                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        MobileIdApplication.FILE_PROVIDER,
                        photoImageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_REQUEST);
        }
    }


    @NonNull
    private String selectionToFileName(String selectionName) {
        String filename;
        filename = selectionName.replaceAll(" ","_");
        return filename.toLowerCase();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnRequestSubmitListener = null;
    }

    @Override
    public void removeFingerItem(String fingerPosition) {
        Timber.d("XXXXRemoveFinger");
        //remove item from list
        mFingerprintCapturedAdapter.removeFingerprintPosition(fingerPosition);
        //delete file
        deleteFileFrom(selectionToFileName(fingerPosition));


    }

    @Override
    public  void removeDocumentItem(String documentName){
        Timber.d("XXXXRemoveDocument");
        //remove item from list
        mDocumentCapturedAdapter.removeDocument(documentName);
        //delete file
        deleteFileFrom(selectionToFileName(documentName));

    }


    private void deleteFileFrom(String fileName) {
        File fileToRemove = new File(getContext()
                .getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                TextUtils.concat(fileName, FILE_EXTENSION).toString());
        Timber.d("XXXFileTODelete: " +fileToRemove.getAbsolutePath());
        //delete file
        fileToRemove.delete();
    }


    public interface OnRequestSubmitListener {
         void requestSubmit(IdData modifiedIdData);
         void notifyValidation(String validationMessage);
    }
}
