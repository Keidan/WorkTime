package fr.ralala.worktime.dropbox;


import android.content.Context;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderResult;

import fr.ralala.worktime.tasks.TaskRunner;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Task to list items in a folder
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class ListFolderTask extends TaskRunner<DbxClientV2, String, Void, ListFolderTask.Result> {
  public static class Result {
    private ListFolderResult file = null;
    private Exception exception = null;
  }
  private final DropboxListener mCallback;
  private final DbxClientV2 mClient;

  /**
   * Creates a task.
   *
   * @param dbxClient Dropbox client.
   * @param callback  Dropbox callback.
   */
  ListFolderTask(DbxClientV2 dbxClient, DropboxListener callback) {
    mClient = dbxClient;
    mCallback = callback;
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public DbxClientV2 onPreExecute() {
    return mClient;
  }

  /**
   * Executed when the task is finished.
   *
   * @param result The task result.
   */
  @Override
  public void onPostExecute(Result result) {
    super.onPostExecute(result);

    if (result.exception != null) {
      mCallback.onDropboxListFolderError(result.exception);
    } else {
      mCallback.onDropboxListFolderDataLoaded(result.file);
    }
  }

  /**
   * Executed in background.
   *
   * @param param folder path
   * @return ListFolderResult
   */
  @Override
  public Result doInBackground(DbxClientV2 client, String param) {
    Result res = new Result();
    try {
      if (client == null)
        throw new NullPointerException("Null client");
      DbxUserFilesRequests files = client.files();
      if (files == null)
        throw new NullPointerException("Null client files");
      res.file = files.listFolder(param);
    } catch (DbxException | NullPointerException e) {
      res.exception = e;
    }
    return res;
  }
}
