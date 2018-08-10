package fr.ralala.worktime.models;


import android.content.Context;
import android.location.Location;

import java.util.Calendar;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Representation of a profile entry
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ProfileEntry extends DayEntry{
  private String mName = "";
  private Location mLocation = new Location("");
  private int mLearningWeight = 0;

  /**
   * Tests if an object is equal to this instance.
   * @param o The input object.
   * @return boolean
   */
  public boolean equals(Object o) {
    if(o == null || !ProfileEntry.class.isInstance(o))
      return false;
    if (this == o)
      return true;
    if(ProfileEntry.class.isInstance(o)) {
      ProfileEntry de = (ProfileEntry) o;
      if (mName != null || de.mName != null) {
        if (mName != null && de.mName != null && mName.compareTo(de.mName) != 0)
          return false;
        else if ((mName == null && de.mName != null) || (mName != null && de.mName == null))
          return false;
      }
      return super.equals(o) &&  mLearningWeight == de.mLearningWeight;
    }
    return false;
  }

  /**
   * Constructs a new day entry.
   * @param day Associated day.
   * @param typeMorning Type of morning.
   * @param typeAfternoon Type of afternoon.
   */
  public ProfileEntry(final WorkTimeDay day, final DayType typeMorning, final DayType typeAfternoon) {
    super(day, typeMorning, typeAfternoon);
  }

  /**
   * Constructs a new day entry.
   * @param day Associated day.
   * @param typeMorning Type of morning.
   * @param typeAfternoon Type of afternoon.
   */
  public ProfileEntry(final Calendar day, final DayType typeMorning, final DayType typeAfternoon) {
    super(day, typeMorning, typeAfternoon);
  }

  /**
   * Tests if the current instance matches with the current entry.
   * @param de The entry to test.
   * @param testName Test name.
   * @return boolean
   */
  public boolean match(ProfileEntry de, boolean testName) {
    return !(testName && !mName.equals(de.mName))
        && mLocation.getLatitude() == de.getLatitude()
        && mLocation.getLongitude() == de.getLongitude()
        && super.match(de, testName);
  }

  /**
   * Returns the learning weight.
   * @return int
   */
  public int getLearningWeight() {
    return mLearningWeight;
  }

  /**
   * Sets the learning weight.
   * @param learningWeight The new value.
   */
  public void setLearningWeight(int learningWeight) {
    mLearningWeight = learningWeight;
  }

  /**
   * Returns the name (if used with profile or public holidays.
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Sets the name (if used with profile or public holidays)
   * @param name The new value.
   */
  public void setName(String name) {
    mName = name;
  }

  /**
   * Returns the location.
   * @return Location
   */
  public Location getLocation() {
    return mLocation;
  }
  /**
   * Returns the latitude.
   * @return double
   */
  public double getLatitude() {
    return mLocation.getLatitude();
  }

  /**
   * Sets the latitude.
   * @param latitude The new value.
   */
  public void setLatitude(final double latitude) {
    mLocation.setLatitude(latitude);
  }

  /**
   * Returns the longitude.
   * @return double
   */
  public double getLongitude() {
    return mLocation.getLongitude();
  }

  /**
   * Sets the longitude.
   * @param longitude The new value.
   */
  public void setLongitude(final double longitude) {
    mLocation.setLongitude(longitude);
  }
}
