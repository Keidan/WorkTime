package fr.ralala.worktime.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;

/**
 * Utility functions to support Uri conversion and processing.
 * Source <a href="https://github.com/dropbox/dropbox-sdk-java/blob/master/examples/android/src/main/java/com/dropbox/core/examples/android/UriHelpers.java">...</a>
 */
public final class UriHelpers {

  private UriHelpers() {
  }

  /**
   * Get the file path for a uri. This is a convoluted way to get the path for an Uri created using the
   * StorageAccessFramework. This in no way is the official way to do this but there does not seem to be a better
   * way to do this at this point. It is taken from <a href="https://github.com/iPaulPro/aFileChooser">...</a>.
   *
   * @param context The context of the application
   * @param uri     The uri of the saved file
   * @return The file with path pointing to the saved file. It can return null if we can't resolve the uri properly.
   */
  public static File getFileForUri(final Context context, final Uri uri) {
    String path = null;
    // DocumentProvider
    if (DocumentsContract.isDocumentUri(context, uri)) {
      // ExternalStorageProvider
      if (isExternalStorageDocument(uri)) {
        path = processExternalStorageDocument(uri);
      } else if (isDownloadsDocument(uri)) {
        path = processDownloadsDocument(context, uri);
      } else if (isMediaDocument(uri)) {
        path = processMediaDocument(context, uri);
      }
    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
      // MediaStore (and general)
      path = getDataColumn(context, uri, null, null);
    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
      // File
      path = uri.getPath();
    }
    return path != null ? new File(path) : null;
  }

  private static String processExternalStorageDocument(final Uri uri) {
    final String docId = DocumentsContract.getDocumentId(uri);
    final String[] split = docId.split(":");
    final String type = split[0];
    String path = null;

    if ("primary".equalsIgnoreCase(type)) {
      path = Environment.getExternalStorageDirectory() + File.separator + split[1];
    }
    return path;
  }

  private static String processDownloadsDocument(final Context context, final Uri uri) {
    // DownloadsProvider
    final String id = DocumentsContract.getDocumentId(uri);
    final Uri contentUri = ContentUris
      .withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

    return getDataColumn(context, contentUri, null, null);
  }

  private static String processMediaDocument(final Context context, final Uri uri) {
// MediaProvider
    final String docId = DocumentsContract.getDocumentId(uri);
    final String[] split = docId.split(":");
    final String type = split[0];

    Uri contentUri = null;
    if ("image".equals(type)) {
      contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    } else if ("video".equals(type)) {
      contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    } else if ("audio".equals(type)) {
      contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    final String selection = "_id=?";
    final String[] selectionArgs = new String[]{
      split[1]
    };

    return getDataColumn(context, contentUri, selection, selectionArgs);
  }

  /**
   * Returns the data column.
   *
   * @param context       The Android context.
   * @param uri           The input Uri.
   * @param selection     The selection value.
   * @param selectionArgs The selection arguments.
   * @return String
   */
  private static String getDataColumn(Context context, Uri uri, String selection,
                                      String[] selectionArgs) {

    Cursor cursor = null;
    final String column = "_data";
    final String[] projection = {
      column
    };

    try {
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
        null);
      if (cursor != null && cursor.moveToFirst()) {
        final int column_index = cursor.getColumnIndexOrThrow(column);
        return cursor.getString(column_index);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }


  /**
   * Test if the Uri authority match with an external storage document.
   *
   * @param uri The input Uri
   * @return boolean
   */
  private static boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  /**
   * Test if the Uri authority match with a downloads document.
   *
   * @param uri The input Uri
   * @return boolean
   */
  private static boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  /**
   * Test if the Uri authority match with an media document.
   *
   * @param uri The input Uri
   * @return boolean
   */
  private static boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }
}