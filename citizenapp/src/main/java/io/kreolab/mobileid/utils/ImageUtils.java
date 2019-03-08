/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils {

    public static Bitmap convertBytesToBitmap(byte[] imageBytes){
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
        return bitmap;
    }
}
