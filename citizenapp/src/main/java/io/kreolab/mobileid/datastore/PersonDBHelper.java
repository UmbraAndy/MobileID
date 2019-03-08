/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.datastore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;

public class PersonDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mobileid.db";
    private static final int DATABASE_VERSION = 1;

    public PersonDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d("XXXDBCreated");
        final String CREATE_SQL = "CREATE TABLE " + IdDataContract.PersonalData.TABLE_NAME
                + " ("
                + IdDataContract.PersonalData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + IdDataContract.PersonalData.COLUMN_ID_NUMBER + " TEXT NOT NULL,"
                + IdDataContract.PersonalData.COLUMN_SURNAME + " TEXT NOT NULL, "
                + IdDataContract.PersonalData.COLUMN_FIRST_NAME + " TEXT NOT NULL, "
                + IdDataContract.PersonalData.COLUMN_OTHER_NAMES + " TEXT, "
                + IdDataContract.PersonalData.COLUMN_DATE_OF_BIRTH + " TIMESTAMP NOT NULL, "
                + IdDataContract.PersonalData.COLUMN_NATIONALITY + " TEXT NOT NULL, "
                + IdDataContract.PersonalData.COLUMN_DATE_OF_ISSUE + " TIMESTAMP NOT NULL, "
                + IdDataContract.PersonalData.COLUMN_DATE_OF_EXPIRY + " TIMESTAMP NOT NULL,"
                + IdDataContract.PersonalData.COLUMN_PHOTO + " BLOB NOT NULL,"
                + IdDataContract.PersonalData.COLUMN_PIN + " TEXT NOT NULL "
                + ")";
        db.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //handle db upgrade here. this is just fro test
        //db.execSQL("DROP TABLE "+ IdDataContract.PersonalData.TABLE_NAME);
        Timber.d("XXXDBUpdated");
    }
}
