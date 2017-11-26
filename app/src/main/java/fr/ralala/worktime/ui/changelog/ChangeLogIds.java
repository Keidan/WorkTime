package fr.ralala.worktime.ui.changelog;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the changelog
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ChangeLogIds {
  private int stringBackgroundColor    = 0;
  private int stringChangelogTitle     = 0;
  private int stringChangelogFullTitle = 0;
  private int stringChangelogShowFull  = 0;
  private int stringChangelogOkButton  = 0;
  private int rawChangelog             = 0;

  public ChangeLogIds(final int rawChangelog, final int stringChangelogOkButton, final int stringBackgroundColor,
      final int stringChangelogTitle, final int stringChangelogFullTitle,
      final int stringChangelogShowFull) {
    this.stringChangelogOkButton = stringChangelogOkButton;
    this.stringBackgroundColor = stringBackgroundColor;
    this.stringChangelogTitle = stringChangelogTitle;
    this.stringChangelogFullTitle = stringChangelogFullTitle;
    this.stringChangelogShowFull = stringChangelogShowFull;
    this.rawChangelog = rawChangelog;
  }

  int getStringChangelogOkButton() {
    return stringChangelogOkButton;
  }

  int getStringBackgroundColor() {
    return stringBackgroundColor;
  }

  int getStringChangelogTitle() {
    return stringChangelogTitle;
  }

  int getStringChangelogFullTitle() {
    return stringChangelogFullTitle;
  }

  int getStringChangelogShowFull() {
    return stringChangelogShowFull;
  }

  int getRawChangelog() {
    return rawChangelog;
  }

}
