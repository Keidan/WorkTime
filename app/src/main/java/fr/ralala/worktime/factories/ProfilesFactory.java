package fr.ralala.worktime.factories;

import java.util.ArrayList;
import java.util.Comparator;
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
  private final List<DayEntry> mProfiles;
  private SqlFactory mSql = null;

  /**
   * Creates the factory.
   */
  public ProfilesFactory() {
    mProfiles = new ArrayList<>();
  }

  /**
   * Reloads the entries from the SQLite databases.
   * @param sql The SQLite factory.
   */
  public void reload(final SqlFactory sql) {
    mSql = sql;
    mProfiles.clear();
    mProfiles.addAll(sql.getProfiles());
  }

  /**
   * Resets the profiles weight.
   */
  public void resetProfilesLearningWeight() {
    for(DayEntry p : mProfiles) {
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
    boolean selected = false;
    for(DayEntry p : mProfiles) {
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
    DayEntry profile = null;
    int weight = 0;
    for(DayEntry p : mProfiles) {
      if(weight == 0 || weight <= p.getLearningWeight()) {
        weight = p.getLearningWeight();
        profile = p;
      }
    }
    if(weight == 0) return null;
    return profile;
  }

  /**
   * Returns the list of profiles.
   * @return List<DayEntry>
   */
  public List<DayEntry> list() {
    return mProfiles;
  }

  /**
   * Removes an existing entry.
   * @param de The entry to remove.
   */
  public void remove(final DayEntry de) {
    mProfiles.remove(de);
    mSql.removeProfile(de);
    mProfiles.sort(Comparator.comparing(DayEntry::getName));
  }

  /**
   * Adds a new entry.
   * @param de The entry to add.
   */
  public void add(final DayEntry de) {
    boolean found = false;
    for(DayEntry d : mProfiles)
      if(d.getName().equals(de.getName())) {
        found = true;
        break;
      }
    if(!found)
      mProfiles.add(de);
    mSql.insertProfile(de);
    mProfiles.sort(Comparator.comparing(DayEntry::getName));
  }

  /**
   * Returns a profile by name.
   * @param name The profile name
   * @return DayEntry
   */
  public DayEntry getByName(final String name) {
    for(DayEntry de : mProfiles) {
      if(de.getName().equals(name))
        return de;
    }
    return null;
  }
}
