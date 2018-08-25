package fr.ralala.worktime.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.services.QuickAccessService;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the quick access fragment view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class QuickAccessFragment extends Fragment implements OnClickListener {
  private MainApplication mApp = null;
  private Button mBtFinalize = null;
  private ToggleButton mBtStart = null;
  private TextView mTvTime = null;
  private Blink mBlink = null;

  /**
   * Called when the fragment is created.
   * @param inflater The fragment inflater.
   * @param container The fragment container.
   * @param savedInstanceState The saved instance state.
   * @return The created view.
   */
  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_quick_access, container, false);

    mBtFinalize = rootView.findViewById(R.id.btFinalize);
    mBtStart = rootView.findViewById(R.id.btStart);
    mTvTime = rootView.findViewById(R.id.tvTime);

    mBtFinalize.setOnClickListener(this);
    mBtStart.setOnClickListener(this);
    mBtFinalize.setVisibility(View.GONE);

    mTvTime.setText(getString(R.string.default_time));
    mApp = MainApplication.getInstance();
    return rootView;
  }

  /**
   * Called when the fragment is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    if(mBlink == null) {
      mBlink = new Blink();
      mBlink.start();
    }
    if(isStarted() || mApp.getQuickAccessNotification().isVisible()) {
      mBtStart.setChecked(true);
      mBtFinalize.setVisibility(View.VISIBLE);
    } else {
      mBtStart.setChecked(false);
      mBtFinalize.setVisibility(View.GONE);
    }
    updateText(mApp.getDaysFactory().getCurrentDay(), false, false);
  }

  /**
   * Called when the fragment is destroyed.
   */
  @Override
  public void onDestroy() {
    if(mBlink != null) {
      mBlink.kill();
      mBlink = null;
    }
    super.onDestroy();
  }

  /**
   * Called when the user click on a button (start finalize).
   * @param v The clicked view.
   */
  @Override
  public void onClick(View v) {
    Activity activity = getActivity();
    if(activity == null) return;
    if(v.equals(mBtStart)) {
      String text = getString(R.string.work_time) + ": ";
      text += mTvTime.getText() + ":00";
      if(mBtStart.isChecked()) {
        mApp.setQuickAccessPause(false);
        mBtFinalize.setVisibility(View.VISIBLE);
        mApp.getQuickAccessNotification().set(text);
      } else {
        mApp.setQuickAccessPause(true);
        mApp.getQuickAccessNotification().update(null, mApp.isQuickAccessPause());
      }
      if(!mApp.isQuickAccessPause()) {
        if(!isStarted()) {
          activity.startService(new Intent(activity, QuickAccessService.class));
        }
      } else if(isStarted())  {
        activity.stopService(new Intent(activity, QuickAccessService.class));
      }
    } else if(v.equals(mBtFinalize)) {
      mApp.setQuickAccessPause(true);
      mBtStart.setChecked(false);
      mBtFinalize.setVisibility(View.GONE);
      DayEntry de = mApp.getDaysFactory().getCurrentDay();
      mApp.getDaysFactory().remove(de);
      mApp.getDaysFactory().add(de);
      activity.stopService(new Intent(activity, QuickAccessService.class));
      mApp.getQuickAccessNotification().remove(activity);
    }
  }

  /**
   * Class used to blink(:) and update the time label.
   */
  class Blink extends Thread {
    private boolean end = false;

    private void kill() {
      end = true;
      interrupt();
    }

    public void run() {
      long delay = 1000;
      while(!end) {
        try {
          sleep(delay);
        } catch (InterruptedException e) {
          return;
        }
        boolean restore = true;
        /* updated from the notification */
        if(isStarted() && !mBtStart.isChecked() && !mApp.isQuickAccessPause()) {
          mBtStart.post(() -> mBtStart.setChecked(true));
        } else if(!isStarted() && mBtStart.isChecked()) {
          mBtStart.post(() -> mBtStart.setChecked(false));
        }
        while(mApp.isQuickAccessPause()) try {
          if(restore) {
            updateText(mApp.getDaysFactory().getCurrentDay(), true, true);
            restore = false;
          }
          sleep(delay);
        } catch (InterruptedException e) {
          return;
        }
        updateText(mApp.getDaysFactory().getCurrentDay(), false, true);
      }
    }
  }

  /**
   * Tests if the service is started.
   * @return boolean
   */
  private boolean isStarted() {
    return AndroidHelper.isServiceRunning(getContext(), QuickAccessService.class);
  }

  /**
   * Updates the time text.
   * @param d The current time entry
   * @param reset True to reset the text.
   * @param post True to post the treatment in the UI thread.
   */
  private void updateText(DayEntry d, final boolean reset, final boolean post) {
    if(!post) {
      WorkTimeDay w = d.getWorkTime();
      mTvTime.setText(String.format(Locale.US, "%02d:%02d", w.getHours(), w.getMinutes()));
    } else {
      mTvTime.post(() -> {
        String text = mTvTime.getText().toString();
        if (reset) {
          mTvTime.setText(text.replaceAll(" ", ":"));
        } else {
          boolean space = text.contains(" ");
          DayEntry dd = mApp.getDaysFactory().getCurrentDay();
          WorkTimeDay w = dd.getWorkTime();
          if(w.getHours() < 0 || w.getMinutes() < 0)
            text = String.format(Locale.US, "-%02d%s%02d", Math.abs(w.getHours()), space ? ":" : " ", Math.abs(w.getMinutes()));
          else
            text = String.format(Locale.US, "%02d%s%02d", w.getHours(), space ? ":" : " ", w.getMinutes());
          mTvTime.setText(text);
        }
      });
    }
  }
}
