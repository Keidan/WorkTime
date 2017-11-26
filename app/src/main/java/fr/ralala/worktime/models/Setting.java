package fr.ralala.worktime.models;


/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Setting representation
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class Setting {
  private String mName = null;
  private String mValue = null;

  public Setting(final String name, final String value) {
    mName = name;
    mValue = value;
  }

  public boolean equals(Object o) {
    if(o == null || !Setting.class.isInstance(o))
      return false;
    Setting s = (Setting)o;
    return s.mName.compareTo(mName) == 0 && s.mValue.compareTo(mValue) == 0;
  }

  public String toString() {
    return mName+":"+mValue;
  }

  public String getName() {
    return mName;
  }

  public void setName(final String name) {
    mName = name;
  }

  public void disable() {
    mValue = "false";
  }

}
