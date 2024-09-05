package com.c3labs.retroftfiledownloader;

import android.os.Environment;

import java.io.File;

public class Method {
    public static File createOrGetDirectory() {
        File myDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NAMAL");

        if (!myDirectory.exists()) {
            myDirectory.mkdirs();
        }
        return myDirectory;
    }
}
