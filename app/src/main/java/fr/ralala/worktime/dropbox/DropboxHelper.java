package fr.ralala.worktime.dropbox;

import android.content.Context;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Helper functions
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
@SuppressWarnings("WeakerAccess")
public class DropboxHelper {
  private DbxClientV2 mDbxClient;
  private final DropboxCredentialUtil mCredentialUtil;
  private final DropboxOAuthUtil mOAuthUtil;
  private final Context mContext;

  public DropboxHelper(Context context) {
    mContext = context;
    mCredentialUtil = new DropboxCredentialUtil(context);
    mOAuthUtil = new DropboxOAuthUtil(mCredentialUtil);
  }

  public void invalidate() {
    mCredentialUtil.removeCredentialLocally();
  }

  public void startDropboxAuthorization() {
    if (mCredentialUtil.isAuthenticated()) {
      if (mCredentialUtil.isExpired())
        mOAuthUtil.refreshToken(mContext, getClient(), mCredentialUtil.readCredentialLocally(true));
      mDbxClient = null;
    } else
      mOAuthUtil.startDropboxAuthorization2PKCE(mContext);
  }

  private void createClient() {
    mOAuthUtil.onResume();
    mDbxClient = new DbxClientV2(
      mOAuthUtil.getDbxRequestConfig(mContext),
      mCredentialUtil.readCredentialLocally(true)
    );
  }

  /**
   * Returns the instance of the dropbox client.
   *
   * @return DbxClientV2
   */
  DbxClientV2 getClient() {
    if (mDbxClient == null) {
      createClient();
    }
    return mDbxClient;
  }

  public ListFolderResult listFolder(String path) throws DbxException, NullPointerException {
    DbxUserFilesRequests files = getClient().files();
    if (files == null)
      throw new NullPointerException("Null client files");
    return files.listFolder(path);
  }

  public FileMetadata uploadFile(InputStream inputStream, String remoteFolderPath, String remoteFileName) throws IOException, DbxException, NullPointerException {
    DbxUserFilesRequests files = getClient().files();
    if (files == null)
      throw new NullPointerException("Null client files");

    return files.uploadBuilder(remoteFolderPath + "/" + remoteFileName)
      .withMode(WriteMode.OVERWRITE)
      .uploadAndFinish(inputStream);
  }

  public void download(File file, FileMetadata metadata) throws IOException, DbxException {
    // Download the file.
    try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
      DbxUserFilesRequests files = getClient().files();
      DbxDownloader<FileMetadata> downloader = files.download(metadata.getPathLower(), metadata.getRev());
      downloader.download(outputStream);
      downloader.close();
    }
  }
}
