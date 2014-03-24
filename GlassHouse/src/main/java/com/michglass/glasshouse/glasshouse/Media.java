package com.michglass.glasshouse.glasshouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rodly on 3/23/14.
 */
public class Media {

    private ArrayList<Bitmap> media;
    private ArrayList<Uri> uris;

    public Media() {}

    public Uri getImageUri(Context inContext, Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), image, "Title", null);
        return Uri.parse(path);
    }

    public void addMedia(Bitmap image, Uri imageUri) {
        media.add(image);
        uris.add(imageUri);
    }

    public ArrayList<Bitmap> getMedia() {
        return media;
    }

    public ArrayList<Uri> getUris() {
        return uris;
    }
}
