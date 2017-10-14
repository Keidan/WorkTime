package fr.ralala.worktime.factories;




import java.util.ArrayList;
import java.util.Collections;
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
  private final List<DayEntry> profiles;
  private SqlFactory sql = null;

  public ProfilesFactory() {
    profiles = new ArrayList<>();
  }

  public void reload(final SqlFactory sql) {
    this.sql = sql;
    profiles.clear();
    profiles.addAll(sql.getProfiles());
  }

  public void resetProfilesLearningWeight() {
    for(DayEntry p : profiles) {
      if(p.getLearningWeight() > 0) {
        p.setLearningWeight(0);
        sql.updateProfile(p);
      }
    }
  }

  public void updateProfilesLearningWeight(DayEntry profile, int weightLimit, boolean fromClear) {
    for(DayEntry p : profiles) {
      int weight = p.getLearningWeight();
      int max = weightLimit*2;
      if(profile != null && p.getName().equals(profile.getName())) {
        p.setLearningWeight(weight < max ? ++weight : max);
        sql.updateProfile(p);
      } else if(weight > 0) {
        if(fromClear)
          p.setLearningWeight(p.getLearningWeight() - 1);
        sql.updateProfile(p);
      }
    }
  }

  public DayEntry getHighestLearningWeight() {
    DayEntry profile = null;
    int weight = 0;
    for(DayEntry p : profiles) {
      if(weight == 0 || weight <= p.getLearningWeight()) {
        weight = p.getLearningWeight();
        profile = p;
      }
    }
    if(weight == 0) return null;
    return profile;
  }

  public List<DayEntry> list() {
    return profiles;
  }

  public void remove(final DayEntry de) {
    profiles.remove(de);
    sql.removeProfile(de);
  }

  public void add(final DayEntry de) {
    profiles.add(de);
    sql.insertProfile(de);
    Collections.sort(profiles, new Comparator<DayEntry>() {
      @Override
      public int compare(DayEntry a, DayEntry b) {
        return a.getName().compareTo(b.getName());
      }
    });
  }
  public DayEntry getByName(final String name) {
    for(DayEntry de : profiles) {
      if(de.getName().equals(name))
        return de;
    }
    return null;
  }
}
