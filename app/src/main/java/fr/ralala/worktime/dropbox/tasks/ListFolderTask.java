package fr.ralala.worktime.dropbox.tasks;


import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.ListFolderResult;

import fr.ralala.worktime.dropbox.DropboxHelper;
import fr.ralala.worktime.dropbox.DropboxListener;
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
public class ListFolderTask extends TaskRunner<DropboxHelper, String, Void, ListFolderTask.Result> {
  public static class Result {
    private ListFolderResult file = null;
    private Exception exception = null;
  }

  private final DropboxListener mCallback;
  private final DropboxHelper mHelper;

  /**
   * Creates a task.
   *
   * @param helper   Dropbox client.
   * @param callback Dropbox callback.
   */
  public ListFolderTask(DropboxHelper helper, DropboxListener callback) {
    mHelper = helper;
    mCallback = callback;
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public DropboxHelper onPreExecute() {
    return mHelper;
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
  public Result doInBackground(DropboxHelper helper, String param) {
    Result res = new Result();
    try {
      if (helper == null)
        throw new NullPointerException("Null dropbox helper");
      res.file = helper.listFolder(param);
    } catch (DbxException | NullPointerException e) {
      res.exception = e;
    }
    return res;
  }
}
