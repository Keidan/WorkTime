package fr.ralala.worktime.chooser.handler;

/**
 *******************************************************************************
 * @file ISelectionHandler.java
 * @author Keidan
 * @date 24/11/2015
 * @par Project
 * ATK
 *
 * @par 
 * Copyright 2015 Keidan, all right reserved
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY.
 *
 * License summary : 
 *    You can modify and redistribute the sources code and binaries.
 *    You can send me the bug-fix
 *
 * Term of the license in in the file license.txt.
 *
 *******************************************************************************
 */
public interface ISelectionHandler {

  public ErrorStatus doCompute(Object userObject);

  public void onSuccess();

  public void onCancel();

  public void onError();
}
