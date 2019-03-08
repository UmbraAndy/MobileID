/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.contentProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import io.kreolab.mobileid.datastore.IdDataContract;
import io.kreolab.mobileid.datastore.PersonDBHelper;
import timber.log.Timber;

public class MobileIdProvider extends ContentProvider {


    public static final int PERSON_ITEM = 101;
    public static final int PERSON_DIR = 100;
    // initialize uri matcher for matching the URis
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private PersonDBHelper mPersonDBHelper;

    public MobileIdProvider() {
        sUriMatcher.addURI(IdDataContract.PROVIDER_AUTHORITY, IdDataContract.PersonalData.TABLE_NAME, PERSON_DIR);
        sUriMatcher.addURI(IdDataContract.PROVIDER_AUTHORITY, IdDataContract.PersonalData.TABLE_NAME + "/#", PERSON_ITEM);
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mPersonDBHelper.getWritableDatabase();
        int updatedRows;
        switch (sUriMatcher.match(uri)) {
//            case PERSON_ITEM:
//                //get id
//                String id = uri.getPathSegments().get(1);
//                updatedRows = db.delete(IdDataContract.PersonalData.TABLE_NAME,
//                        IdDataContract.PersonalData.COLUMN_ID_NUMBER + "=?", new String[]{id});
//
//                break;
            case PERSON_DIR:
                updatedRows = db.delete(IdDataContract.PersonalData.TABLE_NAME, selection,selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(uri.toString() + " not supported");
        }

        if (updatedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRows;

    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = mPersonDBHelper.getWritableDatabase();
        Uri createdUri;
        long insertedRowId;
        switch (sUriMatcher.match(uri)) {
            case PERSON_DIR:
                insertedRowId = db.insert(IdDataContract.PersonalData.TABLE_NAME, null, values);

                break;
            default:
                throw new UnsupportedOperationException(uri.toString() + " not supported");

        }
        if (insertedRowId > 0) {
            createdUri = ContentUris.withAppendedId(IdDataContract.PersonalData.CONTENT_URI, insertedRowId);
        } else {
            throw new SQLException("Insert fail for " + uri);
        }
        Timber.d("XXXXXInsertID "+insertedRowId );
        return createdUri;
    }

    @Override
    public boolean onCreate() {
        // create database resource here
        mPersonDBHelper = new PersonDBHelper(getContext());
        return true;

    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = mPersonDBHelper.getReadableDatabase();
        switch (sUriMatcher.match(uri)) {
            case PERSON_ITEM://single item
                //get id number
                String id = uri.getPathSegments().get(1);
                cursor = db.query(IdDataContract.PersonalData.TABLE_NAME
                        , projection, IdDataContract.PersonalData.COLUMN_ID_NUMBER + "=?", new String[]{id}, null, null, sortOrder);
                break;
            case PERSON_DIR://match multiple items
                cursor = db.query(IdDataContract.PersonalData.TABLE_NAME
                        , projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException(uri.toString() + " not supported");

        }

        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mPersonDBHelper.getWritableDatabase();
        int updatedRows;
        switch (sUriMatcher.match(uri)) {
            case PERSON_ITEM:
                //get id number
                String id = uri.getPathSegments().get(1);
                updatedRows = db.update(IdDataContract.PersonalData.TABLE_NAME, values,
                        IdDataContract.PersonalData.COLUMN_ID_NUMBER + "=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException(uri.toString() + " not supported");
        }

        if (updatedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRows;
    }
}
