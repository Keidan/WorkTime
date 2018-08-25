package fr.ralala.worktime.dropbox;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import fr.ralala.worktime.utils.UriHelpers;

/**
 * Async task to upload a file to a directory
 * Source https://github.com/dropbox/dropbox-sdk-java/blob/master/examples/android/src/main/java/com/dropbox/core/examples/android/UploadFileTask.java
 */
public class UploadFileTask extends AsyncTask<String, Void, FileMetadata> {

  private final DbxClientV2 mDbxClient;
  private final DropboxListener mCallback;
  private Exception mException;
  private final WeakReference<Context> mWeakContext;


  /**
   * Creates a task.
   * @param context The Android contexte.
   * @param dbxClient Dropbox client.
   * @param callback Dropbox client.
   */
  UploadFileTask(Context context, DbxClientV2 dbxClient, DropboxListener callback) {
    mWeakContext = new WeakReference<>(context);
    mDbxClient = dbxClient;
    mCallback = callback;
  }

  /**
   * Executed when the task is finished.
   * @param result The task result.
   */
  @Override
  protected void onPostExecute(FileMetadata result) {
    super.onPostExecute(result);
    if (mException != null) {
      mCallback.onDropboxUploadError(mException);
    } else if (result == null) {
      mCallback.onDropboxUploadError(null);
    } else {
      mCallback.onDropboxUploadComplete(result);
    }
  }

  /**
   * Executed in background.
   * @param params [0]=localUri, [1]=remote folder path
   * @return FileMetadata
   */
  @Override
  protected FileMetadata doInBackground(String... params) {
    Context context = mWeakContext.get();
    String localUri = params[0];
    File localFile = UriHelpers.getFileForUri(context, Uri.parse(localUri));

    if (localFile != null) {
      String remoteFolderPath = params[1];

      // Note - this is not ensuring the name is a valid dropbox file name
      String remoteFileName = localFile.getName();
      Log.d(getClass().getSimpleName(), "remoteFolderPath: '" + remoteFolderPath + "'");
      Log.d(getClass().getSimpleName(), "localFile: '" + localFile + "'");
      try (InputStream inputStream = new FileInputStream(localFile)) {
        return mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName)
          .withMode(WriteMode.OVERWRITE)
          .uploadAndFinish(inputStream);
      } catch (DbxException | IOException e) {
        mException = e;
      }
    }

    return null;
  }
}