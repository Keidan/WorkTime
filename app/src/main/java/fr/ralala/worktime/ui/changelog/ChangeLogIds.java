package fr.ralala.worktime.ui.changelog;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the changelog
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ChangeLogIds {
  private final int mStringBackgroundColor;
  private final int mStringChangelogTitle;
  private final int mStringChangelogFullTitle;
  private final int mStringChangelogShowFull;
  private final int mStringChangelogOkButton;
  private final int mRawChangelog;

  public ChangeLogIds(final int rawChangelog, final int stringChangelogOkButton, final int stringBackgroundColor,
                      final int stringChangelogTitle, final int stringChangelogFullTitle,
                      final int stringChangelogShowFull) {
    mStringChangelogOkButton = stringChangelogOkButton;
    mStringBackgroundColor = stringBackgroundColor;
    mStringChangelogTitle = stringChangelogTitle;
    mStringChangelogFullTitle = stringChangelogFullTitle;
    mStringChangelogShowFull = stringChangelogShowFull;
    mRawChangelog = rawChangelog;
  }

  /**
   * Returns the String resource for the label of the OK button.
   *
   * @return int
   */
  int getStringChangelogOkButton() {
    return mStringChangelogOkButton;
  }

  /**
   * Returns the String resource for the background.
   *
   * @return int
   */
  int getStringBackgroundColor() {
    return mStringBackgroundColor;
  }

  /**
   * Returns the String resource for the title.
   *
   * @return int
   */
  int getStringChangelogTitle() {
    return mStringChangelogTitle;
  }

  /**
   * Returns the String resource for the full title.
   *
   * @return int
   */
  int getStringChangelogFullTitle() {
    return mStringChangelogFullTitle;
  }

  /**
   * Returns the String resource for show full field.
   *
   * @return int
   */
  int getStringChangelogShowFull() {
    return mStringChangelogShowFull;
  }


  /**
   * Returns the resource id of the changelog file.
   *
   * @return int
   */
  int getRawChangelog() {
    return mRawChangelog;
  }

}
