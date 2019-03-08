/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.kreolab.mobileid.R;
import io.kreolab.mobileid.async.QrCodeGeneratorAsyncTask;
import io.kreolab.mobileid.model.IdData;
import timber.log.Timber;


public class ShowIdQrCodeFragment extends Fragment implements QrCodeGeneratorAsyncTask.TaskStatus {

    public static final String ID_DATA = "id_data";

    private QRCodeGeneration mQrCodeGeneration;

    @BindView(R.id.qrCodeImg)
    ImageView mQrCodeImg;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mQrCodeGeneration = (QRCodeGeneration) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement QRCodeGeneration");
        }
        IdData idData = getArguments().getParcelable(ID_DATA);
        QrCodeGeneratorAsyncTask mQrCodeGeneratorAsyncTask = new QrCodeGeneratorAsyncTask(this);
        mQrCodeGeneratorAsyncTask.execute(idData);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_idqrcode, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void qrCodeGenerationSuccessful(Bitmap bitmap) {
        mQrCodeImg.setImageBitmap(bitmap);
    }

    @Override
    public void qrCodeGenerationFailed(String errorMessage) {
        Timber.d("XXXXQRcodeGen: " + errorMessage);
        mQrCodeGeneration.generationFailed();
    }


    public interface QRCodeGeneration{
        void generationFailed();
    }
}
