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

  /**
   * Returns the String resource for the label of the OK button.
   * @return int
   */
  int getStringChangelogOkButton() {
    return stringChangelogOkButton;
  }

  /**
   * Returns the String resource for the background.
   * @return int
   */
  int getStringBackgroundColor() {
    return stringBackgroundColor;
  }

  /**
   * Returns the String resource for the title.
   * @return int
   */
  int getStringChangelogTitle() {
    return stringChangelogTitle;
  }

  /**
   * Returns the String resource for the full title.
   * @return int
   */
  int getStringChangelogFullTitle() {
    return stringChangelogFullTitle;
  }

  /**
   * Returns the String resource for show full field.
   * @return int
   */
  int getStringChangelogShowFull() {
    return stringChangelogShowFull;
  }


  /**
   * Returns the resource id of the changelog file.
   * @return int
   */
  int getRawChangelog() {
    return rawChangelog;
  }

}
