package com.droidmare.accounts.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

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

        return scaledBitmap;
    }

    public static String encodeBitmapImage(Bitmap bitmap) {

        if (bitmap == null) return null;

        bitmap = scaleBitmap(bitmap);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        byte[] bitmapByteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bitmapByteArray, Base64.DEFAULT);
    }

    public static Bitmap decodeBitmapString(String encodedString) {

        byte[] encodedBitmapByteArray = Base64.decode(encodedString, Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(encodedBitmapByteArray, 0, encodedBitmapByteArray.length);
    }
}