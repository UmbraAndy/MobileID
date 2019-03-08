/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.datastore;

import android.net.Uri;
import android.provider.BaseColumns;

public class IdDataContract {
    public static String PROVIDER_AUTHORITY = "io.kreolab.mobileid.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + PROVIDER_AUTHORITY);


    public static class PersonalData implements BaseColumns {

        public static final String COLUMN_ID_NUMBER = "id_number";
        public static final String TABLE_NAME = "personal_data";
        public static final String COLUMN_SURNAME = "surname";
        public static final String COLUMN_FIRST_NAME = "firstname";
        public static final String COLUMN_OTHER_NAMES = "other_names";
        public static final String COLUMN_DATE_OF_BIRTH = "date_of_birth";
        public static final String COLUMN_NATIONALITY = "nationality";
        public static final String COLUMN_DATE_OF_ISSUE = "date_of_issue";
        public static final String COLUMN_DATE_OF_EXPIRY = "date_of_expiry";
        public static final String COLUMN_PHOTO = "photo";
        public static final String COLUMN_PIN = "pin";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
    }

}
