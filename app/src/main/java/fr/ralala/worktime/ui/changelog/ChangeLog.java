package fr.ralala.worktime.ui.changelog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.util.Log;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Copyright (C) 2011-2013, Karsten Priegnitz
 * Permission to use, copy, modify, and distribute this piece of software
 * for any purpose with or without fee is hereby granted, provided that
 * the above copyright notice and this permission notice appear in the
 * source code of all copies.
 * It would be appreciated if you mention the author in your change log,
 * contributors list or the like.
 * author: Karsten Priegnitz
 * see: <a href="http://code.google.com/p/android-change-log/">...</a>
 */
public class ChangeLog {
  private ChangeLogIds mIds;
  private final Context mContext;
  private final String mLastVersion;
  private String mThisVersion;

  // this is the key for storing the version name in SharedPreferences
  private static final String VERSION_KEY = "PREFS_VERSION_KEY";
  private static final String NO_VERSION = "";

  /**
   * Constructor
   * <p>
   * Retrieves the version names and stores the new version name in
   * SharedPreferences
   */
  public ChangeLog(final ChangeLogIds ids, final Context context) {
    this(ids, context, PreferenceManager.getDefaultSharedPreferences(context));
  }

  /**
   * Constructor
   * <p>
   * Retrieves the version names and stores the new version name in
   * SharedPreferences
   *
   * @param sp the shared preferences to store the last version name into
   */
  private ChangeLog(final ChangeLogIds ids, final Context context,
                    final SharedPreferences sp) {
    mContext = context;
    mIds = ids;

    // get version numbers
    mLastVersion = sp.getString(VERSION_KEY, NO_VERSION);
    Log.d(TAG, "lastVersion: " + mLastVersion);
    try {
      mThisVersion = context.getPackageManager().getPackageInfo(
        context.getPackageName(), 0).versionName;
    } catch (final NameNotFoundException e) {
      mThisVersion = NO_VERSION;
      Log.e(TAG, "could not get version name from manifest!");
      e.printStackTrace();
    }
    Log.d(TAG, "appVersion: " + mThisVersion);
  }

  /**
   * @return <code>true</code> if this version of your app is started the first
   * time
   */
  public boolean firstRun() {
    return !mLastVersion.equals(mThisVersion);
  }

  /**
   * @return <code>true</code> if your app including ChangeLog is started the
   * first time ever. Also <code>true</code> if your app was deinstalled
   * and installed again.
   */
  private boolean firstRunEver() {
    return NO_VERSION.equals(mLastVersion);
  }

  /**
   * @return An AlertDialog displaying the changes since the previous installed
   * version of your app (what's new). But when this is the first run of
   * your app including ChangeLog then the full log dialog is show.
   */
  public AlertDialog getLogDialog() {
    return this.getDialog(this.firstRunEver());
  }

  /**
   * @return an AlertDialog with a full change log displayed
   */
  public AlertDialog getFullLogDialog() {
    return this.getDialog(true);
  }

  private AlertDialog getDialog(final boolean full) {
    final WebView wv = new WebView(mContext);

    wv.setBackgroundColor(Color.parseColor(mContext.getResources().getString(
      mIds.getStringBackgroundColor())));
    wv.loadDataWithBaseURL(null, this.getLog(full), "text/html", "UTF-8", null);

    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
    builder
      .setTitle(
        mContext.getResources().getString(
          full ? mIds.getStringChangelogFullTitle() : mIds
            .getStringChangelogTitle()))
      .setView(wv)
      .setCancelable(false)
      // OK button
      .setPositiveButton(
        mContext.getResources().getString(mIds.getStringChangelogOkButton()),
        (dialog, which) -> updateVersionInPreferences());

    if (!full) {
      // "more ..." button
      builder.setNegativeButton(mIds.getStringChangelogShowFull(),
        (dialog, which) -> getFullLogDialog().show());
    }

    return builder.create();
  }

  private void updateVersionInPreferences() {
    // save new version number to preferences
    final SharedPreferences sp = PreferenceManager
      .getDefaultSharedPreferences(mContext);
    final SharedPreferences.Editor editor = sp.edit();
    editor.putString(VERSION_KEY, mThisVersion);
    editor.apply();
  }

  /**
   * @return HTML displaying the changes since the previous installed version of
   * your app (what's new)
   */
  public String getLog() {
    return this.getLog(false);
  }

  /**
   * modes for HTML-Lists (bullet, numbered)
   */
  private enum Listmode {
    NONE, ORDERED, UNORDERED,
  }

  private Listmode listMode = Listmode.NONE;
  private StringBuilder sb = null;
  private static final String EOCL = "END_OF_CHANGE_LOG";

  private String getLog(final boolean full) {
    // read changelog.txt file
    sb = new StringBuilder();
    try {
      final InputStream ins = mContext.getResources().openRawResource(
        mIds.getRawChangelog());
      try (final BufferedReader br = new BufferedReader(new InputStreamReader(ins))) {

        String line;
        boolean advanceToEOVS = false; // if true: ignore further version
        // sections
        while ((line = br.readLine()) != null) {
          line = line.trim();
          final char marker = line.length() > 0 ? line.charAt(0) : 0;
          if (marker == '$') {
            // begin of a version section
            this.closeList();
            final String version = line.substring(1).trim();
            // stop output?
            if (!full) {
              if (mLastVersion.equals(version)) {
                advanceToEOVS = true;
              } else if (version.equals(EOCL)) {
                advanceToEOVS = false;
              }
            }
          } else if (!advanceToEOVS) {
            switch (marker) {
              case '%':
                // line contains version title
                this.closeList();
                sb.append("<div class='title'>").append(line.substring(1).trim()).append("</div>\n");
                break;
              case '_':
                // line contains version title
                this.closeList();
                sb.append("<div class='subtitle'>").append(line.substring(1).trim()).append("</div>\n");
                break;
              case '!':
                // line contains free text
                this.closeList();
                sb.append("<div class='freetext'>").append(line.substring(1).trim()).append("</div>\n");
                break;
              case '#':
                // line contains numbered list item
                this.openList(Listmode.ORDERED);
                sb.append("<li>").append(line.substring(1).trim()).append("</li>\n");
                break;
              case '*':
                // line contains bullet list item
                this.openList(Listmode.UNORDERED);
                sb.append("<li>").append(line.substring(1).trim()).append("</li>\n");
                break;
              default:
                // no special character: just use line as is
                this.closeList();
                sb.append(line).append("\n");
            }
          }
        }
        this.closeList();
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return sb.toString();
  }

  private void openList(final Listmode listMode) {
    if (this.listMode != listMode) {
      closeList();
      if (listMode == Listmode.ORDERED) {
        sb.append("<div class='list'><ol>\n");
      } else if (listMode == Listmode.UNORDERED) {
        sb.append("<div class='list'><ul>\n");
      }
      this.listMode = listMode;
    }
  }

  private void closeList() {
    if (this.listMode == Listmode.ORDERED) {
      sb.append("</ol></div>\n");
    } else if (this.listMode == Listmode.UNORDERED) {
      sb.append("</ul></div>\n");
    }
    this.listMode = Listmode.NONE;
  }

  private static final String TAG = "ChangeLog";
}