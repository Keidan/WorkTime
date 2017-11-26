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

  public ProfilesFactory() {
    mProfiles = new ArrayList<>();
  }

  public void reload(final SqlFactory sql) {
    mSql = sql;
    mProfiles.clear();
    mProfiles.addAll(sql.getProfiles());
  }

  public void resetProfilesLearningWeight() {
    for(DayEntry p : mProfiles) {
      if(p.getLearningWeight() > 0) {
        p.setLearningWeight(0);
        mSql.updateProfile(p);
      }
    }
  }

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

  public List<DayEntry> list() {
    return mProfiles;
  }

  public void remove(final DayEntry de) {
    mProfiles.remove(de);
    mSql.removeProfile(de);
  }

  public void add(final DayEntry de) {
    mProfiles.add(de);
    mSql.insertProfile(de);
    mProfiles.sort(Comparator.comparing(DayEntry::getName));
  }
  public DayEntry getByName(final String name) {
    for(DayEntry de : mProfiles) {
      if(de.getName().equals(name))
        return de;
    }
    return null;
  }
}
