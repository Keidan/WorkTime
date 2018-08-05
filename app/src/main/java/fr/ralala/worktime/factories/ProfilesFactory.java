package fr.ralala.worktime.factories;

import java.util.List;

import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.sql.SqlFactory;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Profiles factory functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ProfilesFactory {
  private SqlFactory mSql = null;

  /**
   * Sets the reference to the SqlFactory.
   * @param sql The SQLite factory.
   */
  public void setSqlFactory(final SqlFactory sql) {
    mSql = sql;
  }

  /**
   * Resets the profiles weight.
   */
  public void resetProfilesLearningWeight() {
    List<DayEntry> list = mSql.getProfiles(-1, -1, -1);
    for(DayEntry p : list) {
      if(p.getLearningWeight() > 0) {
        p.setLearningWeight(0);
        mSql.updateProfile(p);
      }
    }
  }

  /**
   * Updates all learning weight (+1 for the current entry and -1 for others).
   * @param profile The selected profile.
   * @param weightLimit The weight limit.
   * @param fromClear Called from clear.
   */
  public void updateProfilesLearningWeight(DayEntry profile, int weightLimit, boolean fromClear) {
    List<DayEntry> list = mSql.getProfiles(-1, -1, -1);
    boolean selected = false;
    for(DayEntry p : list) {
      int weight = p.getLearningWeight();
      int max = weightLimit*2;
      if(profile != null && p.getName().equals(profile.getName())) {
        p.setLearningWeight(weight < max ? ++weight : max);
        mSql.updateProfile(p);
        selected = true;
      } else if(weight > 0) {
        if(fromClear || selected)
          p.setLearningWeight(p.getLearningWeight() - 1);
        mSql.updateProfile(p);
      }
    }
  }

  /**
   * Returns an entry with the highest learning weight.
   * @return DayEntry
   */
  public DayEntry getHighestLearningWeight() {
    List<DayEntry> list = mSql.getProfiles(-1, -1, -1);
    DayEntry profile = null;
    int weight = 0;
    for(DayEntry p : list) {
      if(weight == 0 || weight <= p.getLearningWeight()) {
        weight = p.getLearningWeight();
        profile = p;
      }
    }
    return (weight == 0) ? null : profile;
  }

  /**
   * Returns the list of profiles.
   * @return List<DayEntry>
   */
  public List<DayEntry> list() {
    return mSql.getProfiles(-1, -1, -1);
  }

  /**
   * Removes an existing entry.
   * @param de The entry to remove.
   */
  public void remove(final DayEntry de) {
    mSql.removeProfile(de);
  }

  /**
   * Adds a new entry.
   * @param de The entry to add.
   */
  public void add(final DayEntry de) {
    mSql.insertProfile(de);
  }

  /**
   * Returns a profile by name.
   * @param name The profile name
   * @return DayEntry
   */
  public DayEntry getByName(final String name) {
    return mSql.getProfile(name);
  }
}
