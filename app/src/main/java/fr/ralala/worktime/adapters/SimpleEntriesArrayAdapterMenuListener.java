package fr.ralala.worktime.adapters;


/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Called when the user click on the entry menu
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public interface SimpleEntriesArrayAdapterMenuListener<T> {
  boolean onMenuEdit(T t);
  void onMenuDelete(T t);
}
