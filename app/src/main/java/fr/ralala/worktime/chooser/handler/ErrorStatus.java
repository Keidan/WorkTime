package fr.ralala.worktime.chooser.handler;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Selection errors
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public enum ErrorStatus {
  NO_ERROR, CANCEL, ERROR_NOT_MOUNTED, ERROR_CANT_READ, ERROR_NOT_FOUND, ERROR_INVALID_FORMAT, ERROR_INVALID_VALUE;

  String message = null;

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
};
