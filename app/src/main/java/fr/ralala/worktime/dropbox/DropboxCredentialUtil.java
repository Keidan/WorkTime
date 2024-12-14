package fr.ralala.worktime.dropbox;

import android.content.Context;
import android.content.SharedPreferences;

import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.oauth.DbxCredential;

import fr.ralala.worktime.ApplicationCtx;
import fr.ralala.worktime.utils.Log;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Credential util
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class DropboxCredentialUtil {
  private static final String LABEL = "dropbox-app";
  private static final String KEY_CREDENTIAL = "credential-json";
  private static final String KEY_EXPIRES_AT = "credential-expires-at";
  private final Context mContext;
  private final SharedPreferences mPref;

  public DropboxCredentialUtil(Context context) {
    mContext = context;
    mPref = mContext.getSharedPreferences(LABEL, Context.MODE_PRIVATE);
  }

  public boolean isExpired() {
    long expiresAt = mPref.getLong(KEY_EXPIRES_AT, 0);
    long time = System.currentTimeMillis();
    Log.info(mContext, "isExpired", "Time: " + time + ", expires at: " + expiresAt);
    boolean expired = (time >= expiresAt);
    Log.info(mContext, "isExpired", "Expired: " + (expired ? "YES" : "NO"));
    return expired;
  }

  public DbxCredential readCredentialLocally(boolean log) {
    String serializedCredentialJson = mPref.getString(KEY_CREDENTIAL, null);
    if (log) {
      Log.info(mContext, "readCredentialLocally", "Local Credential Value from Shared Preferences: " + serializedCredentialJson);
    }
    if (serializedCredentialJson == null)
      return null;
    try {
      return DbxCredential.Reader.readFully(serializedCredentialJson);
    } catch (JsonReadException e) {
      Log.error(mContext, "readCredentialLocally", "Something went wrong parsing the credential, clearing it: " + e.getMessage(), e);
      removeCredentialLocally();
      return null;
    }
  }

  //serialize the credential and store in SharedPreferences
  public void storeCredentialLocally(DbxCredential dbxCredential) {
    Long expiresAt = dbxCredential.getExpiresAt();
    Log.info(mContext, "storeCredentialLocally", "Storing credential in Shared Preferences expires at " + expiresAt);
    SharedPreferences.Editor edit = mPref.edit();
    edit.putString(KEY_CREDENTIAL, DbxCredential.Writer.writeToString(dbxCredential));
    edit.putLong(KEY_EXPIRES_AT, expiresAt);
    edit.apply();
  }

  public void removeCredentialLocally() {
    Log.info(mContext, "removeCredentialLocally", "Clearing credential from Shared Preferences");
    SharedPreferences.Editor edit = mPref.edit();
    edit.remove(KEY_CREDENTIAL);
    edit.remove(KEY_EXPIRES_AT);
    edit.apply();
  }

  public boolean isAuthenticated() {
    return readCredentialLocally(false) != null;
  }
}
