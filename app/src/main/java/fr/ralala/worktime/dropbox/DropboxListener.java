package fr.ralala.worktime.dropbox;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;

import java.io.File;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Main listener for the Dropbox helper class
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public interface DropboxListener {

  /**
   * Called when the upload on dropbox is complete.
   * @param result The upload result.
   */
  void onDropboxUploadComplete(FileMetadata result);

  /**
   * Called when the upload on dropbox is finished on error.
   * @param e Error exception.
   */
  void onDropboxUploadError(Exception e);

  /**
   * Called when the download on dropbox is complete.
   * @param result The upload result.
   */
  void onDroptboxDownloadComplete(File result);

  /**
   * Called when the download on dropbox is finished on error.
   * @param e Error exception.
   */
  void onDroptboxDownloadError(Exception e);

  /**
   * Called when the recovery of the file list of the dropbox is complete.
   * @param result The listing result.
   */
  void onDroptboxListFoderDataLoaded(ListFolderResult result);

  /**
   * Called when the recovery of the file list of the dropbox is finished on error.
   * @param e Error exception.
   */
  void onDroptboxListFoderError(Exception e);
}
