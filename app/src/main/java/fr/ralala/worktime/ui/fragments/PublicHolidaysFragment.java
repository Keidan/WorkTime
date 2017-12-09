package fr.ralala.worktime.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import fr.ralala.worktime.ui.activities.PublicHolidayActivity;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.adapters.PublicHolidaysEntriesArrayAdapter;
import fr.ralala.worktime.ui.adapters.SimpleEntriesArrayAdapterMenuListener;
import fr.ralala.worktime.models.DayEntry;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the public holidays fragment view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class PublicHolidaysFragment extends Fragment implements SimpleEntriesArrayAdapterMenuListener<DayEntry>, View.OnClickListener{

  private PublicHolidaysEntriesArrayAdapter mAdapter = null;
  private MainApplication mApp = null;

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
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_public_holidays, container, false);

    ListView lv = rootView.findViewById(R.id.public_holidays);
    FloatingActionButton fab = rootView.findViewById(R.id.fab);

    mApp = MainApplication.getApp(getContext());
    mAdapter = new PublicHolidaysEntriesArrayAdapter(
      getContext(), R.layout.listview_item, mApp.getPublicHolidaysFactory().list(), this);
    lv.setAdapter(mAdapter);


    fab.setOnClickListener(this);
    return rootView;
  }

  /**
   * Called when the fragment is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    if(mApp.isResumeAfterActivity()) {
      mAdapter.notifyDataSetChanged();
      mApp.setResumeAfterActivity(false);
    }
  }

  /**
   * Called when the edit button is clicked.
   * @param de The clicked entry object.
   */
  @Override
  public void onMenuEdit(DayEntry de) {
    mApp.setResumeAfterActivity(true);
    PublicHolidayActivity.startActivity(getActivity(), de.getName());
  }

  /**
   * Called when the delete button is clicked.
   * @param de The clicked entry object.
   */
  @Override
  public void onMenuDelete(DayEntry de) {
    mApp.getPublicHolidaysFactory().remove(de);
    mAdapter.remove(de);
  }

  /**
   * Called when the user click on a button (fab).
   * @param view The clicked view.
   */
  @Override
  public void onClick(View view) {
    mApp.setResumeAfterActivity(true);
    PublicHolidayActivity.startActivity(getActivity(), "null");
  }


  /**
   * Creates an animation when the adapter is updated.
   * @param transit See official javadoc.
   * @param enter See official javadoc.
   * @param nextAnim See official javadoc.
   * @return Animation
   */
  @Override
  public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
    Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
    anim.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) { }
      @Override
      public void onAnimationRepeat(Animation animation) { }
      @Override
      public void onAnimationEnd(Animation animation) {
        mAdapter.notifyDataSetChanged();
      }
    });
    return anim;
  }

}
