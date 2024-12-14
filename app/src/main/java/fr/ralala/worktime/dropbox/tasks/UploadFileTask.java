package fr.ralala.worktime.dropbox.tasks;

import android.content.Context;
import android.net.Uri;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.dropbox.DropboxHelper;
import fr.ralala.worktime.dropbox.DropboxListener;
import fr.ralala.worktime.tasks.TaskRunner;
import fr.ralala.worktime.utils.UriHelpers;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Task to to upload a file to a directory
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class UploadFileTask extends TaskRunner<UploadFileTask.Config, List<String>, Void, UploadFileTask.Result> {
  public static class Result {
    private FileMetadata file = null;
    private Exception exception = null;
  }

  public static class Config {
    private Context context = null;
    private DropboxHelper helper = null;
  }

  private final DropboxListener mCallback;
  private final Config mConfig = new Config();


  /**
   * Creates a task.
   *
   * @param context  The Android context.
   * @param helper   Dropbox helper.
   * @param callback Dropbox client.
   */
  public UploadFileTask(Context context, DropboxHelper helper, DropboxListener callback) {
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
   * Executed when the task is finished.
   *
   * @param result The task result.
   */
  @Override
  public void onPostExecute(Result result) {
    super.onPostExecute(result);
    if (result.exception != null) {
      mCallback.onDropboxUploadError(result.exception);
    } else {
      mCallback.onDropboxUploadComplete(result.file);
    }
  }

  /**
   * Executed in background.
   *
   * @param params [0]=localUri, [1]=remote folder path
   * @return FileMetadata
   */
  @Override
  public Result doInBackground(Config cfg, List<String> params) {
    Result res = new Result();
    InputStream inputStream = null;
    try {
      if (params == null)
        throw new NullPointerException("Null params");
      if (params.size() != 2)
        throw new IllegalArgumentException("Empty params");
      String localUri = params.get(0);
      File localFile = UriHelpers.getFileForUri(cfg.context, Uri.parse(localUri));

      if (localFile != null) {
        String remoteFolderPath = params.get(1);

        // Note - this is not ensuring the name is a valid dropbox file name
        String remoteFileName = localFile.getName();
        MainApplication.addLog(cfg.context, "UploadFileTask", "remoteFolderPath: '" + remoteFolderPath + "'");
        MainApplication.addLog(cfg.context, "UploadFileTask", "localFile: '" + localFile + "'");

        if (cfg.helper == null)
          throw new NullPointerException("Null dropbox helper");
        inputStream = Files.newInputStream(localFile.toPath());

        res.file = cfg.helper.uploadFile(inputStream, remoteFolderPath, remoteFileName);
      }
    } catch (DbxException | IOException | NullPointerException | IllegalArgumentException e) {
      res.exception = e;
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          res.exception = e;
        }
      }
    }
    return res;
  }
}