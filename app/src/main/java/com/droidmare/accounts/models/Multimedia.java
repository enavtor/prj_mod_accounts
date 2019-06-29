package com.droidmare.accounts.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class Multimedia implements Parcelable {

    private String name;

    private String path;

    private File icon;
    private Bitmap bitmapIcon;

    private int type;

    public Multimedia(String name, String path) {
        this.name = name;
        this.path = path;
        this.icon = null;
        this.bitmapIcon = null;
        this.type = -1;
    }

    public String getName() { return name; }

    public String getPath() { return path; }

    public void setIcon(File icon) { this.icon = icon;}

    public File getIcon() { return icon; }

    public void setBitmapIcon(Bitmap icon) { this.bitmapIcon = icon;}

    public Bitmap getBitmapIcon() { return bitmapIcon; }

    public void setType(int type) { this.type = type; }

    public int getType() { return type; }

    private Multimedia(Parcel in) {
        name = in.readString();
        path = in.readString();
        type = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeInt(type);
    }

    @SuppressWarnings("unused")
    public static final Creator<Multimedia> CREATOR = new Creator<Multimedia>() {
        @Override
        public Multimedia createFromParcel(Parcel in) {
            return new Multimedia(in);
        }

        @Override
        public Multimedia[] newArray(int size) {
            return new Multimedia[size];
        }
    };
}
