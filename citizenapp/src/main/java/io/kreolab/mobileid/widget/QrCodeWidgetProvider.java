/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import io.kreolab.mobileid.R;
import io.kreolab.mobileid.async.QrCodeGeneratorAsyncTask;
import io.kreolab.mobileid.datastore.IdDataContract;
import io.kreolab.mobileid.model.IdData;
import io.kreolab.mobileid.ui.ContentActivity;
import io.kreolab.mobileid.ui.SignInActivity;
import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class QrCodeWidgetProvider extends AppWidgetProvider implements QrCodeGeneratorAsyncTask.TaskStatus {
    private static    Context sContext;
    private static String sViewIdNumber;
    private static Bitmap sViewQrCodeImage;

    private static QrCodeGeneratorAsyncTask.TaskStatus sTaskStatus = new QrCodeGeneratorAsyncTask.TaskStatus() {
        @Override
        public void qrCodeGenerationSuccessful(Bitmap bitmap) {
            Timber.d("XXXXXQR-code S");
            sViewQrCodeImage = bitmap;
            AppWidgetManager appWidgetManager  = AppWidgetManager.getInstance(sContext);
            int [] appWigetIds = appWidgetManager.getAppWidgetIds(new ComponentName(sContext,QrCodeWidgetProvider.class));
            for(int appWidgetId : appWigetIds) {
                updateAppWidget(sContext, AppWidgetManager.getInstance(sContext), appWidgetId);
            }
        }

        @Override
        public void qrCodeGenerationFailed(String errorMessage) {
            Timber.d("XXXXXStaticHolderError "+ errorMessage );
        }
    };


    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        Timber.d("XXXXStaticUpdateCalled");
        Timber.d("XXXXBitmap: "+ sViewQrCodeImage);
        Timber.d("XXXXText: "+ sViewIdNumber);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.qr_code_widget_provider);
        Intent intent = new Intent(context, SignInActivity.class);
        //set this to set the fragment to launch
        intent.putExtra(ContentActivity.LAST_MENU_CLICKED, R.id.displayQrCodeMnu);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_image, pendingIntent);
        views.setTextViewText(R.id.appwidget_text, sViewIdNumber);
        views.setImageViewBitmap(R.id.appwidget_image, sViewQrCodeImage);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Timber.d("XXXXWidgetEnabled");
        sContext = context;
        FetchIdFromDB fetchIdFromDBAsyncTask = new FetchIdFromDB();
        fetchIdFromDBAsyncTask.execute();
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

    }

    @Override
    public void qrCodeGenerationSuccessful(Bitmap bitmap) {
             Timber.d("XXXXXQR-code S");
             sViewQrCodeImage = bitmap;
             AppWidgetManager appWidgetManager  = AppWidgetManager.getInstance(sContext);
             int [] appWigetIds = appWidgetManager.getAppWidgetIds(new ComponentName(sContext,QrCodeWidgetProvider.class));
             for(int appWidgetId : appWigetIds) {
                 updateAppWidget(sContext, AppWidgetManager.getInstance(sContext), appWidgetId);
             }

    }

    @Override
    public void qrCodeGenerationFailed(String errorMessage) {
            Timber.d("XXXXQRErr "+errorMessage);
    }


    private class FetchIdFromDB extends AsyncTask<Void, Void, IdData> {

        @Override
        protected IdData doInBackground(Void... voids) {
            Timber.d("XXXXFetchDoInBG");
            Cursor cursor = sContext.getContentResolver()
                    .query(IdDataContract.PersonalData.CONTENT_URI, null,
                            null, null, null);
            IdData idData = null;
            if (cursor.getCount() > 0) {
                idData = new IdData();
                cursor.moveToFirst();
                String idNumber = cursor.getString(cursor.getColumnIndex(IdDataContract.PersonalData.COLUMN_ID_NUMBER));
                idData.setIdNumber(idNumber);
            }
            return idData;
        }

        @Override
        protected void onPostExecute(IdData idData) {
            super.onPostExecute(idData);
            Timber.d("XXXXFetchPostExe "+ idData);
            sViewIdNumber = idData.getIdNumber();
            QrCodeGeneratorAsyncTask qrCodeGeneratorAsyncTask = new QrCodeGeneratorAsyncTask(sTaskStatus);
            qrCodeGeneratorAsyncTask.execute(idData);
        }
    }
}

