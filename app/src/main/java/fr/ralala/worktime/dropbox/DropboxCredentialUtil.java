package fr.ralala.worktime.dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.oauth.DbxCredential;

import fr.ralala.worktime.ApplicationCtx;

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
  private static final String KEY_EXPIRES = "credential-expires";
  private final Context mContext;
  private final SharedPreferences mPref;

  public DropboxCredentialUtil(Context context) {
    mContext = context;
    mPref = mContext.getSharedPreferences(LABEL, Context.MODE_PRIVATE);
  }

  public boolean isExpired() {
    DbxCredential credential = readCredentialLocally();
    if (credential == null)
      return true;
    long expired = mPref.getLong(KEY_EXPIRES, 0);
    long expiresAt = credential.getExpiresAt();
    String text = "Expired: " + expired + ", expires at: " + expiresAt;
    ApplicationCtx.addLog(mContext, "isExpired", text);
    Log.d(getClass().getSimpleName(), text);
    return (credential.getExpiresAt() >= expired);
  }

  public DbxCredential readCredentialLocally() {
    String serializedCredentialJson = mPref.getString(KEY_CREDENTIAL, null);
    String text = "Local Credential Value from Shared Preferences: " + serializedCredentialJson;
    ApplicationCtx.addLog(mContext, "readCredentialLocally", text);
    Log.d(getClass().getSimpleName(), text);
    if (serializedCredentialJson == null)
      return null;
    try {
      return DbxCredential.Reader.readFully(serializedCredentialJson);
    } catch (JsonReadException e) {
      text = "Something went wrong parsing the credential, clearing it: " + e.getMessage();
      ApplicationCtx.addLog(mContext, "readCredentialLocally", text);
      Log.e(getClass().getSimpleName(), text, e);
      removeCredentialLocally();
      return null;
    }
  }

  //serialize the credential and store in SharedPreferences
  public void storeCredentialLocally(DbxCredential dbxCredential) {
    Long expiresAt = dbxCredential.getExpiresAt();
    String text = "Storing credential in Shared Preferences expires at " + expiresAt;
    ApplicationCtx.addLog(mContext, "storeCredentialLocally", text);
    Log.d(getClass().getSimpleName(), text);
    SharedPreferences.Editor edit = mPref.edit();
    edit.putString(KEY_CREDENTIAL, DbxCredential.Writer.writeToString(dbxCredential));
    edit.putLong(KEY_EXPIRES, expiresAt);
    edit.apply();
  }

  public void removeCredentialLocally() {
    String text = "Clearing credential from Shared Preferences";
    ApplicationCtx.addLog(mContext, "removeCredentialLocally", text);
    Log.d(getClass().getSimpleName(), text);
    SharedPreferences.Editor edit = mPref.edit();
    edit.remove(KEY_CREDENTIAL);
    edit.remove(KEY_EXPIRES);
    edit.apply();
  }

  public boolean isAuthenticated() {
    return readCredentialLocally() != null;
  }
}
