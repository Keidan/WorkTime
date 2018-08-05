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
  private String mName;
  private String mValue;

  /**
   * Creates a setting instance.
   * @param name Setting name.
   * @param value Setting value.
   */
  public Setting(final String name, final String value) {
    mName = name;
    mValue = value;
  }

  /**
   * Tests if the input settings matches with this instance.
   * @param o The input setting.
   * @return boolean
   */
  public boolean equals(Object o) {
    if(o == null || !Setting.class.isInstance(o))
      return false;
    Setting s = (Setting)o;
    return s.mName.compareTo(mName) == 0 && s.mValue.compareTo(mValue) == 0;
  }

  /**
   * Returns the setting name and value.
   * @return String
   */
  public String toString() {
    return mName+":"+mValue;
  }

  /**
   * Returns the name.
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Sets the name.
   * @param name The new name.
   */
  public void setName(final String name) {
    mName = name;
  }

  /**
   * Forces the value to 'false'.
   */
  public void disable() {
    mValue = "false";
  }

}
