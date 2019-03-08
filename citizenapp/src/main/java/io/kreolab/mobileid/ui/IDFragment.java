/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.kreolab.mobileid.R;
import io.kreolab.mobileid.model.IdData;
import io.kreolab.mobileid.utils.ImageUtils;

/**
 * Fragment that shows the ID data
 */
public class IDFragment extends Fragment {

    public static final String ID_DATA = "id_data";


    @BindView((R.id.idNumberTxt))
    TextView mIdNumberTxt;

    @BindView(R.id.surnameTxt)
    TextView mSurnameTxt;

    @BindView(R.id.firstNameTxt)
    TextView mFirstNameTxt;

    @BindView(R.id.otherNamesTxt)
    TextView mOthernamesTxt;

    @BindView(R.id.dateOfBirthTxt)
    TextView mDateOfBirthTxt;

    @BindView(R.id.nationalityTxt)
    TextView mNationalityTxt;

    @BindView(R.id.issueDateTxt)
    TextView mIssueDateTxt;

    @BindView(R.id.expiryDateTxt)
    TextView mExpiryDateTxt;

    @BindView(R.id.passportPhoto)
    ImageView mPassportPhoto;


    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        IdData idData = null;
        if(getArguments() != null && getArguments().getParcelable(ID_DATA) != null)
        {
            idData = getArguments().getParcelable(ID_DATA);
        }
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_id, container, false);
        ButterKnife.bind(this, fragmentView);
        if(idData != null) {
            mIdNumberTxt.setText(idData.getIdNumber());
            mSurnameTxt.setText(idData.getSurname());
            mFirstNameTxt.setText(idData.getFirstname());
            mOthernamesTxt.setText(idData.getOther_names());
            mDateOfBirthTxt.setText(mDateFormat.format(idData.getDate_of_birth()));
            mNationalityTxt.setText(idData.getNationality());
            mIssueDateTxt.setText(mDateFormat.format(idData.getDate_of_issue()));
            mExpiryDateTxt.setText(mDateFormat.format(idData.getDate_of_expiry()));
            mPassportPhoto.setImageBitmap(ImageUtils.convertBytesToBitmap(idData.getPhoto()));
        }

        return fragmentView;
    }
}
