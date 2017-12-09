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
  /**
   * Called when the edit button is clicked.
   * @param t The clicked entry object.
   */
  void onMenuEdit(T t);

  /**
   * Called when the delete button is clicked.
   * @param t The clicked entry object.
   */
  void onMenuDelete(T t);
}
