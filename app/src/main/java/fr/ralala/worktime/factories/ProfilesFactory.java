package fr.ralala.worktime.factories;

import java.util.List;

import fr.ralala.worktime.models.ProfileEntry;
import fr.ralala.worktime.sql.SqlConstants;
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
    List<ProfileEntry> list = mSql.getProfiles(-1, -1, -1);
    for(ProfileEntry p : list) {
      if(p.getLearningWeight() > 0) {
        p.setLearningWeight(0);
        mSql.insertOrUpdateProfile(p, true);
      }
    }
  }

  /**
   * Updates all learning weight (+1 for the current entry and -1 for others).
   * @param profile The selected profile.
   * @param weightLimit The weight limit.
   * @param fromClear Called from clear.
   */
  public void updateProfilesLearningWeight(ProfileEntry profile, int weightLimit, boolean fromClear) {
    List<ProfileEntry> list = mSql.getProfiles(-1, -1, -1);
    boolean selected = false;
    for(ProfileEntry p : list) {
      int weight = p.getLearningWeight();
      int max = weightLimit*2;
      if(profile != null && p.getName().equals(profile.getName())) {
        p.setLearningWeight(weight < max ? ++weight : max);
        mSql.insertOrUpdateProfile(p, true);
        selected = true;
      } else if(weight > 0) {
        if(fromClear || selected)
          p.setLearningWeight(p.getLearningWeight() - 1);
        mSql.insertOrUpdateProfile(p, true);
      }
    }
  }

  /**
   * Returns an entry with the highest learning weight.
   * @return ProfileEntry
   */
  public ProfileEntry getHighestLearningWeight() {
    List<ProfileEntry> list = mSql.getProfiles(-1, -1, -1);
    ProfileEntry profile = null;
    int weight = 0;
    for(ProfileEntry p : list) {
      if(weight == 0 || weight <= p.getLearningWeight()) {
        weight = p.getLearningWeight();
        profile = p;
      }
    }
    return (weight == 0) ? null : profile;
  }

  /**
   * Returns the list of profiles.
   * @return List<ProfileEntry>
   */
  public List<ProfileEntry> list() {
    return mSql.getProfiles(-1, -1, -1);
  }

  /**
   * Removes an existing entry.
   * @param pe The entry to remove.
   */
  public void remove(final ProfileEntry pe) {
    mSql.removeProfile(pe);
  }

  /**
   * Adds a new entry.
   * @param pe The entry to add.
   */
  public void add(final ProfileEntry pe) {
    mSql.insertOrUpdateProfile(pe, pe.getID() != SqlConstants.INVALID_ID);
  }

  /**
   * Returns a profile by name.
   * @param name The profile name
   * @return ProfileEntry
   */
  public ProfileEntry getByName(final String name) {
    return mSql.getProfile(name);
  }
}
