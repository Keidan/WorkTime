package fr.ralala.worktime.dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;

/**
 * Async task to list items in a folder
 * Source https://github.com/dropbox/dropbox-sdk-java/blob/master/examples/android/src/main/java/com/dropbox/core/examples/android/ListFolderTask.java
 */
public class ListFolderTask extends AsyncTask<String, Void, ListFolderResult> {

  private final DbxClientV2 mDbxClient;
  private final DropboxListener mCallback;
  private Exception mException;

  /**
   * Creates a task.
   * @param dbxClient Dropbox client.
   * @param callback Dropbox client.
   */
  ListFolderTask(DbxClientV2 dbxClient, DropboxListener callback) {
    mDbxClient = dbxClient;
    mCallback = callback;
  }

  /**
   * Executed when the task is finished.
   * @param result The task result.
   */
  @Override
  protected void onPostExecute(ListFolderResult result) {
    super.onPostExecute(result);

    if (mException != null) {
      mCallback.onDroptboxListFoderError(mException);
    } else {
      mCallback.onDroptboxListFoderDataLoaded(result);
    }
  }

  /**
   * Executed in background.
   * @param params [0]=folder path
   * @return ListFolderResult
   */
  @Override
  protected ListFolderResult doInBackground(String... params) {
    try {
      return mDbxClient.files().listFolder(params[0]);
    } catch (DbxException e) {
      mException = e;
    }

    return null;
  }
}
