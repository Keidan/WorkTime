package fr.ralala.worktime.dropbox.tasks;

import android.content.Context;
import android.media.MediaScannerConnection;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.IOException;

import fr.ralala.worktime.dropbox.DropboxHelper;
import fr.ralala.worktime.dropbox.DropboxListener;
import fr.ralala.worktime.tasks.TaskRunner;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Task to download a file from Dropbox and put it in the Downloads folder
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class DownloadFileTask extends TaskRunner<DownloadFileTask.Config, FileMetadata, Void, DownloadFileTask.Result> {
  public static class Result {
    private File file = null;
    private Exception exception = null;
  }

  public static class Config {
    private Context context = null;
    private DropboxHelper helper = null;
  }

  private final DropboxListener mCallback;
  private final Config mConfig = new Config();

  /**
   * Creates the task.
   *
   * @param context  The Android context.
   * @param helper   The dropbox helper.
   * @param callback The dropbox listener.
   */
  public DownloadFileTask(Context context, DropboxHelper helper, DropboxListener callback) {
    mConfig.context = context;
    mConfig.helper = helper;
    mCallback = callback;
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public Config onPreExecute() {
    return mConfig;
  }

  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  @Override
  public void onPostExecute(Result result) {
    super.onPostExecute(result);
    if (result.exception != null) {
      mCallback.onDropboxDownloadError(result.exception);
    } else {
      mCallback.onDropboxDownloadComplete(result.file);
    }
  }

  /**
   * Executed in background.
   *
   * @param metadata FileMetadata
   * @return File
   */
  @Override
  public Result doInBackground(Config cfg, FileMetadata metadata) {
    Result res = new Result();
    try {

      File path = AndroidHelper.getAppPath(cfg.context);
      if (metadata == null)
        throw new NullPointerException("Null metadata");

      File file = new File(path, metadata.getName());

      // Make sure the Downloads directory exists.
      if (!path.exists()) {
        if (!path.mkdirs()) {
          res.exception = new RuntimeException("Unable to create directory: " + path);
        }
      } else if (!path.isDirectory()) {
        res.exception = new IllegalStateException("Download path is not a directory: " + path);
        return null;
      }

      cfg.helper.download(file, metadata);

      // Tell android about the file
      MediaScannerConnection.scanFile(cfg.context,
        new String[]{file.toString()}, null, null);

      res.file = file;
    } catch (DbxException | IOException | NullPointerException e) {
      res.exception = e;
    }
    return res;
  }
}
