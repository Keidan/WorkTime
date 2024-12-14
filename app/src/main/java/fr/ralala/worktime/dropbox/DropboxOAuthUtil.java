package fr.ralala.worktime.dropbox;

import android.content.Context;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.oauth.DbxRefreshResult;
import com.dropbox.core.v2.DbxClientV2;

import java.util.Arrays;
import java.util.List;

import fr.ralala.worktime.ApplicationCtx;
import fr.ralala.worktime.R;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * OAuth util functions
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class DropboxOAuthUtil {
  private final DropboxCredentialUtil mDropboxCredentialUtil;
  private boolean mIsAwaitingResult = false;

  public DropboxOAuthUtil(DropboxCredentialUtil credentialUtil) {
    mDropboxCredentialUtil = credentialUtil;
  }

  public DbxRequestConfig getDbxRequestConfig(Context context) {
    return new DbxRequestConfig("db-" + context.getString(R.string.app_key));
  }

  public void refreshToken(Context context, DbxClientV2 client, DbxCredential credential) {
    new Thread(() -> {
      try {
        DbxRefreshResult result = client.refreshAccessToken();
        DbxCredential authDbxCredential = new DbxCredential(
          result.getAccessToken(),
          result.getExpiresAt(),
          credential.getRefreshToken(),
          context.getString(R.string.app_key));
        mDropboxCredentialUtil.storeCredentialLocally(authDbxCredential);
        mIsAwaitingResult = true;
      } catch (DbxException e) {
        String text = "Exception: " + e.getMessage();
        ApplicationCtx.addLog(context, "refreshToken", text);
        Log.e(getClass().getSimpleName(), text, e);
      }
    }).start();
  }

  /**
   * Starts the Dropbox OAuth process by launching the Dropbox official app or web
   * browser if dropbox official app is not available. In browser flow, normally user needs to
   * sign in.
   * Because mobile apps need to keep Dropbox secrets in their binaries we need to use PKCE.
   * Read more about this here: <a href="https://dropbox.tech/developers/pkce--what-and-why-">...</a>
   **/
  public void startDropboxAuthorization2PKCE(Context context) {
    DbxRequestConfig requestConfig = getDbxRequestConfig(context);

    // The scope's your app will need from Dropbox
    // Read more about Scopes here: https://developers.dropbox.com/oauth-guide#dropbox-api-permissions
    List<String> scopes = Arrays.asList(
      "account_info.read",
      "files.content.write",
      "files.content.read",
      "sharing.read"
    );
    Auth.startOAuth2PKCE(context, context.getString(R.string.app_key), requestConfig, scopes);
    mIsAwaitingResult = true;
  }

  /**
   * Starts the Dropbox OAuth process by launching the Dropbox official app or web
   * browser if dropbox official app is not available. In browser flow, normally user needs to
   * sign in.
   * Because mobile apps need to keep Dropbox secrets in their binaries we need to use PKCE.
   * Read more about this here: <a href="https://dropbox.tech/developers/pkce--what-and-why-">...</a>
   **/
  public void startDropboxAuthorizationOAuth2(Context context) {
    Auth.startOAuth2Authentication(context, context.getString(R.string.app_key));
    mIsAwaitingResult = true;
  }

  /**
   * Call this from onResume() in the activity you are awaiting an Auth Result
   */
  public void onResume() {
    if (mIsAwaitingResult) {
      DbxCredential authDbxCredential = Auth.getDbxCredential();
      mIsAwaitingResult = false;
      if (authDbxCredential != null) {
        mDropboxCredentialUtil.storeCredentialLocally(authDbxCredential);
      }
    }
  }
}
