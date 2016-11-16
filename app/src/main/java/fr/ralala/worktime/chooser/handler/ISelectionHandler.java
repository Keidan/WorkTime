package fr.ralala.worktime.chooser.handler;


/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Listener for the selections
 * </p>
 *
 * @author Keidan
 *         <p>
 *******************************************************************************
 */
public interface ISelectionHandler {

  ErrorStatus doCompute(Object userObject);

  void onSuccess();

  void onCancel();

  void onError();
}
