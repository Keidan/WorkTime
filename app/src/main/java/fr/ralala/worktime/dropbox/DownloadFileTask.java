package fr.ralala.worktime.dropbox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 * Source https://github.com/dropbox/dropbox-sdk-java/blob/master/examples/android/src/main/java/com/dropbox/core/examples/android/DownloadFileTask.java
 */
public class DownloadFileTask extends AsyncTask<FileMetadata, Void, File> {

  private final DbxClientV2 mDbxClient;
  private final DropboxListener mCallback;
  private Exception mException;
  private final WeakReference<Context> mWeakContext;

  /**
   * Creates the task.
   * @param context The Android context.
   * @param dbxClient The dropbox client.
   * @param callback The dropbox listener.
   */
  DownloadFileTask(Context context, DbxClientV2 dbxClient, DropboxListener callback) {
    mWeakContext = new WeakReference<>(context);
    mDbxClient = dbxClient;
    mCallback = callback;
  }

  /**
   * Executed when the task is finished.
   * @param result The task result.
   */
  @Override
  protected void onPostExecute(File result) {
    super.onPostExecute(result);
    if (mException != null) {
      mCallback.onDroptboxDownloadError(mException);
    } else {
      mCallback.onDroptboxDownloadComplete(result);
    }
  }

  /**
   * Executed in background.
   * @param params [0]=FileMetadata
   * @return File
   */
  @Override
  protected File doInBackground(FileMetadata... params) {
    FileMetadata metadata = params[0];
    try {
      File path = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS);
      File file = new File(path, metadata.getName());

      // Make sure the Downloads directory exists.
      if (!path.exists()) {
        if (!path.mkdirs()) {
          mException = new RuntimeException("Unable to create directory: " + path);
        }
      } else if (!path.isDirectory()) {
        mException = new IllegalStateException("Download path is not a directory: " + path);
        return null;
      }

      // Download the file.
      try (OutputStream outputStream = new FileOutputStream(file)) {
        mDbxClient.files().download(metadata.getPathLower(), metadata.getRev())
          .download(outputStream);
      }

      // Tell android about the file
      Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
      intent.setData(Uri.fromFile(file));
      mWeakContext.get().sendBroadcast(intent);

      return file;
    } catch (DbxException | IOException e) {
      mException = e;
    }

    return null;
  }
}
