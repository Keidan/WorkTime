package fr.ralala.worktime.dropbox;

import android.content.Context;
import android.content.SharedPreferences;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;

import fr.ralala.worktime.R;

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
  private static final String LABEL = "dropbox-sample";
  private static final String KEY = "accessToken";
  private DbxClientV2 mDbxClient;
  private static DropboxHelper mHelper = null;
  private String mDevAccessToken = null;

  private DropboxHelper() {
  }

  private String getDevAccessToken(Context ctx) {
    if(mDevAccessToken == null) {
      mDevAccessToken = ctx.getString(R.string.dev_access_token);
    }
    return mDevAccessToken;
  }

  /**
   * Returns the helper instance.
   *
   * @return DropboxHelper
   */
  static DropboxHelper helper() {
    if (mHelper == null) mHelper = new DropboxHelper();
    return mHelper;
  }

  /**
   * Established the connection with dropbox
   *
   * @param ctx    The Android context.
   * @return boolean
   */
  boolean connect(final Context ctx) {
    Context c = ctx.getApplicationContext();
    mHelper.loadToken(c);
    if (!mHelper.hasToken(c)) {
      Auth.startOAuth2Authentication(c, c.getString(R.string.app_key));
      mHelper.loadToken(c);
      return mHelper.hasToken(c);
    }
    return true;
  }

  /**
   * Tests if the configuration has the required token.
   *
   * @param ctx The Android context.
   * @return boolean
   */
  private boolean hasToken(final Context ctx) {
    Context c = ctx.getApplicationContext();
    SharedPreferences prefs = c.getSharedPreferences(LABEL, Context.MODE_PRIVATE);
    String accessToken = prefs.getString(KEY, getDevAccessToken(ctx));
    return accessToken != null;
  }

  /**
   * Loads the dropbox token.
   *
   * @param ctx The Android context.
   */
  private void loadToken(final Context ctx) {
    Context c = ctx.getApplicationContext();
    SharedPreferences prefs = c.getSharedPreferences(LABEL, Context.MODE_PRIVATE);
    String accessToken = prefs.getString(KEY, getDevAccessToken(ctx));
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
   *
   * @param accessToken The access token to use.
   */
  private void initDropBox(String accessToken) {
    final Package p = getClass().getPackage();
    if (mDbxClient == null && p != null) {
      DbxRequestConfig.Builder b = DbxRequestConfig.newBuilder(p.getName());
      DbxRequestConfig requestConfig = b.build();
      mDbxClient = new DbxClientV2(requestConfig, accessToken);
    }
  }

  /**
   * Returns the instance of the dropbox client.
   *
   * @return DbxClientV2
   */
  DbxClientV2 getClient() {
    if (mDbxClient == null) {
      throw new IllegalStateException("Client not initialized.");
    }
    return mDbxClient;
  }
}
