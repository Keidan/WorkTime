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
public class DropboxHelper {
  private static final String LABEL = "dropbox-sample";
  private static final String KEY = "access-token";
  private DbxClientV2 sDbxClient;
  private static DropboxHelper helper = null;

  private DropboxHelper() { }

  public static DropboxHelper helper() {
    if(helper == null) helper = new DropboxHelper();
    return helper;
  }

  public boolean connect(final Context c, final String appkey) {
    helper.loadToken(c);
    if(!helper.hasToken(c)) {
      Auth.startOAuth2Authentication(c, appkey);
      helper.loadToken(c);
      return helper.hasToken(c);
    }
    return true;
  }

  public boolean hasToken(final Context c) {
    SharedPreferences prefs = c.getSharedPreferences(LABEL, Context.MODE_PRIVATE);
    String accessToken = prefs.getString(KEY, null);
    return accessToken != null;
  }

  private DropboxHelper loadToken(final Context c) {
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
    return this;
  }

  private void initDropBox(String accessToken) {
    if (sDbxClient == null) {
      DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder(getClass().getPackage().getName())
        .withHttpRequestor(OkHttp3Requestor.INSTANCE)
        .build();
      sDbxClient = new DbxClientV2(requestConfig, accessToken);
    }
  }

  public DbxClientV2 getClient() {
    if (sDbxClient == null) {
      throw new IllegalStateException("Client not initialized.");
    }
    return sDbxClient;
  }
}
