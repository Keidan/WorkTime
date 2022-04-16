package fr.ralala.worktime.dropbox;

import android.content.Context;
import android.content.SharedPreferences;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Helper functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
@SuppressWarnings("WeakerAccess")
public class DropboxHelper {
  private static final String LABEL = "dropbox-sample";
  private static final String KEY = "access-token";
  private DbxClientV2 mDbxClient;
  private static DropboxHelper mHelper = null;

  private DropboxHelper() { }

  /**
   * Returns the helper instance.
   * @return DropboxHelper
   */
  static DropboxHelper helper() {
    if(mHelper == null) mHelper = new DropboxHelper();
    return mHelper;
  }

  /**
   * Established the connection with dropbox
   * @param ctx The Android context.
   * @param appkey The application key.
   * @return boolean
   */
  boolean connect(final Context ctx, final String appkey) {
    Context c = ctx.getApplicationContext();
    mHelper.loadToken(c);
    if(!mHelper.hasToken(c)) {
      Auth.startOAuth2Authentication(c, appkey);
      mHelper.loadToken(c);
      return mHelper.hasToken(c);
    }
    return true;
  }

  /**
   * Tests if the configuration has the required token.
   * @param ctx The Android context.
   * @return boolean
   */
  private boolean hasToken(final Context ctx) {
    Context c = ctx.getApplicationContext();
    SharedPreferences prefs = c.getSharedPreferences(LABEL, Context.MODE_PRIVATE);
    String accessToken = prefs.getString(KEY, null);
    return accessToken != null;
  }

  /**
   * Loads the dropbox token.
   * @param ctx The Android context.
   */
  private void loadToken(final Context ctx) {
    Context c = ctx.getApplicationContext();
    SharedPreferences prefs = c.getSharedPreferences(LABEL, Context.MODE_PRIVATE);
    String accessToken = prefs.getString(KEY, null);
    if (accessToken == null) {
      accessToken = Auth.getOAuth2Token();
      if (accessToken != null) {
        prefs.edit().putString(KEY, accessToken).apply();
        initDropBox(accessToken);
      }
    } else {
      initDropBox(accessToken);
    }
  }

  /**
   * Initializes the dropbox contexts.
   * @param accessToken The access token to use.
   */
  private void initDropBox(String accessToken) {
    if (mDbxClient == null) {
      DbxRequestConfig.Builder b = DbxRequestConfig.newBuilder(getClass().getPackage().getName());
      DbxRequestConfig requestConfig = b.build();
      mDbxClient = new DbxClientV2(requestConfig, accessToken);
    }
  }

  /**
   * Returns the instance of the dropbox client.
   * @return DbxClientV2
   */
  DbxClientV2 getClient() {
    if (mDbxClient == null) {
      throw new IllegalStateException("Client not initialized.");
    }
    return mDbxClient;
  }
}
