package fr.ralala.worktime.models;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

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
  private String name;
  private String data;
  private String path;
  private Drawable icon;

  public FileChooserOption(final String n, final String d, final String p, final Drawable i) {
    name = n;
    data = d;
    path = p;
    icon = i;
  }

  public String getName() {
    return name;
  }

  public String getData() {
    return data;
  }

  public String getPath() {
    return path;
  }

  public Drawable getIcon() {
    return icon;
  }

  @SuppressLint("DefaultLocale")
  @Override
  public int compareTo(final FileChooserOption o) {
    if (this.name != null)
      return this.name.toLowerCase(Locale.getDefault()).compareTo(
          o.getName().toLowerCase());
    else
      throw new IllegalArgumentException();
  }
}
