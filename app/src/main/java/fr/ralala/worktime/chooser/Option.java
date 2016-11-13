package fr.ralala.worktime.chooser;

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
public class Option implements Comparable<Option> {
  private String name;
  private String data;
  private String path;
  private Drawable icon;

  public Option(final String n, final String d, final String p, final Drawable i) {
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

  public boolean isValid() {
    return name != null && path != null;
  }

  @SuppressLint("DefaultLocale")
  @Override
  public int compareTo(final Option o) {
    if (this.name != null)
      return this.name.toLowerCase(Locale.getDefault()).compareTo(
          o.getName().toLowerCase());
    else
      throw new IllegalArgumentException();
  }
}
