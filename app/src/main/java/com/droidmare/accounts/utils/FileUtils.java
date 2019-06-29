package com.droidmare.accounts.utils;

import com.droidmare.accounts.models.Multimedia;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

//Utils form managing files declaration
//@author Eduardo on 11/06/2019.
public class FileUtils {

    public static final int FOLDER_TYPE = 0;
    public static final int IMAGE_TYPE = 1;

    //Method that returns the the list of files contained within the path passed as parameter:
    public static ArrayList<Multimedia> getDirectoryFiles(String directoryPath) {

        ArrayList<String> imageFormats = new ArrayList<>(Arrays.asList("png","jpg","jpeg"));

        ArrayList<Multimedia> multimediaList = new ArrayList<>();

        if (directoryPath != null) for (File file: new File(directoryPath).listFiles()) {

            String fileName = file.getName();

            int fileExtensionStartIndex = fileName.lastIndexOf(".");

            String fileExtension = fileName.substring(fileExtensionStartIndex + 1).toLowerCase();

            fileName = fileName.substring(0, fileExtensionStartIndex > -1 ? fileExtensionStartIndex : fileName.length());

            Multimedia multimedia = new Multimedia(fileName, file.getPath());

            if (file.isDirectory()) {
                for (File temp: file.listFiles()) {
                    if (temp.getName().equals("icon_folder.jpg")) {
                        multimedia.setIcon(temp);
                        break;
                    }
                }

                multimedia.setType(FOLDER_TYPE);
            }

            else if (imageFormats.contains(fileExtension))
                multimedia.setType(IMAGE_TYPE);

            if (multimedia.getType() != -1) multimediaList.add(multimedia);
        }

        return SortUtils.sortMultimediaList(multimediaList);
    }
}