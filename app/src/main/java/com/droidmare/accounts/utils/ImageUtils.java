package com.droidmare.accounts.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;

import java.io.ByteArrayOutputStream;

//Utils for managing bitmap images
//@author Eduardo on 27/05/2019.
public class ImageUtils {

    private static Bitmap scaleBitmap (Bitmap bitmapImage) {

        Bitmap scaledBitmap = bitmapImage;

        boolean mustBeScaled = false;

        int maxScaledSize = 300;

        int scaledWidth = maxScaledSize;
        int scaledHeight = maxScaledSize;

        int bitmapWidth = bitmapImage.getWidth();
        int bitmapHeight = bitmapImage.getHeight();

        Log.d("API_TEST", "Size: " + bitmapWidth + "x" + bitmapHeight);

        if (bitmapWidth > scaledWidth) {
            mustBeScaled = true;
            scaledHeight = scaledWidth * bitmapHeight / bitmapWidth;
            if (scaledHeight > maxScaledSize) {
                scaledHeight = maxScaledSize;
                scaledWidth = scaledHeight * bitmapWidth / bitmapHeight;
            }
        }

        else if (bitmapHeight > scaledHeight) {
            mustBeScaled = true;
            scaledWidth = scaledHeight * bitmapWidth / bitmapHeight;
            if (scaledWidth > maxScaledSize) {
                scaledWidth = maxScaledSize;
                scaledHeight = scaledWidth * bitmapHeight / bitmapWidth;
            }
        }

        if (mustBeScaled) scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, scaledWidth, scaledHeight, false);

        Log.d("API_TEST", "Compressed size: " + scaledBitmap.getWidth() + "x" + scaledBitmap.getHeight());

        return scaledBitmap;
    }

    public static String encodeBitmapImage(Bitmap bitmap) {

        if (bitmap == null) return null;

        bitmap = scaleBitmap(bitmap);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Log.d("API_TEST", "Density: " + bitmap.getByteCount());

        bitmap.compress(Bitmap.CompressFormat.WEBP, 80, byteArrayOutputStream);

        byte[] bitmapByteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bitmapByteArray, Base64.DEFAULT);
    }

    public static Bitmap decodeBitmapString(String encodedString) {

        byte[] encodedBitmapByteArray = Base64.decode(encodedString, Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(encodedBitmapByteArray, 0, encodedBitmapByteArray.length);
    }

    //Method that transforms a dp value into pixels:
    static int transformDipToPix (Context context, int dpValue) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, r.getDisplayMetrics());
    }
}