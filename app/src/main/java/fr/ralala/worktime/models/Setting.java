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
  private String name = null;
  private String value = null;

  public Setting(final String name, final String value) {
    this.name = name;
    this.value = value;
  }

  public boolean equals(Object o) {
    if(o == null || !Setting.class.isInstance(o))
      return false;
    Setting s = (Setting)o;
    return s.name.compareTo(name) == 0 && s.value.compareTo(value) == 0;
  }

  public String toString() {
    return name+":"+value;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setValue(final String value) {
    this.value = value;
  }

}
