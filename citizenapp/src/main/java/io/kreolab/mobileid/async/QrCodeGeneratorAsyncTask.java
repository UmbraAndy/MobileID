/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.async;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.lang.ref.WeakReference;
import java.util.Date;

import io.kreolab.mobileid.model.IdData;
import timber.log.Timber;

public class QrCodeGeneratorAsyncTask extends AsyncTask<IdData,Void,Bitmap> {

    private final WeakReference<TaskStatus> mTaskStatusWeakReference;
    private final MultiFormatWriter mMultiFormatWriter = new MultiFormatWriter();
    private Throwable mError;

    private final String sNewLine = System.getProperty("line.separator");
    

    public QrCodeGeneratorAsyncTask(TaskStatus mTaskStatus) {
        Timber.d("XXXXQRTask: "+ mTaskStatus);
        this.mTaskStatusWeakReference =  new WeakReference<>(mTaskStatus);
    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {

        Timber.d("XXXXQRPostExe");
        if(bitmap ==  null)
        {
            if(mTaskStatusWeakReference != null && mTaskStatusWeakReference.get() != null) {
                mTaskStatusWeakReference.get().qrCodeGenerationFailed(mError.getMessage());
            }
            return;
        }

        Timber.d("mTaskStatusWeakReference != null :" +(mTaskStatusWeakReference != null) + " mTaskStatusWeakReference.get() != null :" + (mTaskStatusWeakReference.get() != null) );
        if(mTaskStatusWeakReference != null && mTaskStatusWeakReference.get() != null)
        {
            mTaskStatusWeakReference.get().qrCodeGenerationSuccessful(bitmap);
        }
    }

    @Override
    protected Bitmap doInBackground(IdData... idDataArray) {
        Timber.d("XXXXQRDoInBG");
        Bitmap qrBitmap = null;
        IdData idData = idDataArray[0];

        //Build string to put in QR code
        
        StringBuilder idDataStringBuilder  = new StringBuilder();
        idDataStringBuilder.append("ID Number: ").append(idData.getIdNumber()).append(sNewLine);
        if(!TextUtils.isEmpty(idData.getSurname())) {
            idDataStringBuilder.append("Surname: ").append(idData.getSurname()).append(sNewLine);
        }
        if(!TextUtils.isEmpty(idData.getSurname())) {
            idDataStringBuilder.append("First name: ").append(idData.getFirstname()).append(sNewLine);
        }
        if(!TextUtils.isEmpty(idData.getSurname())) {
            idDataStringBuilder.append("Other names: ").append(idData.getOther_names()).append(sNewLine);
        }
        if(idData.getDate_of_birth() >= 0) {
            idDataStringBuilder.append("Date of birth: ").append(new Date(idData.getDate_of_birth())).append(sNewLine);
        }
        if(TextUtils.isEmpty(idData.getNationality())) {
            idDataStringBuilder.append("Nationality: ").append(idData.getNationality()).append(sNewLine);
        }
        if(idData.getDate_of_issue() >= 0) {
            idDataStringBuilder.append("Date of issue: ").append(new Date(idData.getDate_of_issue())).append(sNewLine);
        }
        if(idData.getDate_of_expiry() >= 0) {
            idDataStringBuilder.append("Date of expiry: ").append(new Date(idData.getDate_of_expiry())).append(sNewLine);
        }

        try {
            //generate the bitmatrix for the format you want
            BitMatrix bitMatrix = mMultiFormatWriter.encode(idDataStringBuilder.toString(), BarcodeFormat.QR_CODE,200,200);

            //encoder to encode your format to bitmap
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrBitmap = barcodeEncoder.createBitmap(bitMatrix);

        } catch (WriterException e) {
            Timber.e(e);
            mError = e;

        }
        return qrBitmap;
    }

    public interface TaskStatus{
        void qrCodeGenerationSuccessful(Bitmap bitmap);
        void qrCodeGenerationFailed(String errorMessage);
    }

}
