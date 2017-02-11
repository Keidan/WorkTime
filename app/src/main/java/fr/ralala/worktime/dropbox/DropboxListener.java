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

  void onDropboxUploadComplete(FileMetadata result);
  void onDropboxUploadError(Exception e);

  void onDroptboxDownloadComplete(File result);
  void onDroptboxDownloadError(Exception e);


  void onDroptboxListFoderDataLoaded(ListFolderResult result);
  void onDroptboxListFoderError(Exception e);
}
