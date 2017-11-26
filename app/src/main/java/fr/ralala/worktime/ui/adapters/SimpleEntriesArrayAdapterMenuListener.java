package fr.ralala.worktime.ui.adapters;


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
  void onMenuEdit(T t);
  void onMenuDelete(T t);
}
