package fr.ralala.worktime.models;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * File option
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class FileChooserOption implements Comparable<FileChooserOption> {
  private String mName;
  private String mData;
  private String mPath;
  private Drawable mIcon;

  public FileChooserOption(final String n, final String d, final String p, final Drawable i) {
    mName = n;
    mData = d;
    mPath = p;
    mIcon = i;
  }

  public String getName() {
    return mName;
  }

  public String getData() {
    return mData;
  }

  public String getPath() {
    return mPath;
  }

  public Drawable getIcon() {
    return mIcon;
  }

  @SuppressLint("DefaultLocale")
  @Override
  public int compareTo(@NonNull final FileChooserOption o) {
    if (this.mName != null)
      return this.mName.toLowerCase(Locale.getDefault()).compareTo(
          o.getName().toLowerCase());
    else
      throw new IllegalArgumentException();
  }
}
