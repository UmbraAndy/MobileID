/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid;

import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import java.io.File;

import timber.log.Timber;

public class MobileIdApplication extends MultiDexApplication {
    public static  final String FILE_PROVIDER = "io.kreolab.mobileid.fileprovider";

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        //clear pictures in app storage to avoid taking unnecessary space
        clearInternalStorage();
    }

    private void clearInternalStorage() {
        File pictureInternalDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Timber.d("XXXFileTODelete: " +pictureInternalDir.getAbsolutePath());

        //get list of files in folder
        String[] files = pictureInternalDir.list();
        for (String file : files) {
            new File(pictureInternalDir, file).delete();
        }

    }

}
