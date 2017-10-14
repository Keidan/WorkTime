package fr.ralala.worktime.fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import fr.ralala.worktime.activities.PublicHolidayActivity;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.adapters.PublicHolidaysEntriesArrayAdapter;
import fr.ralala.worktime.adapters.SimpleEntriesArrayAdapterMenuListener;
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

  private PublicHolidaysEntriesArrayAdapter adapter = null;
  private MainApplication app = null;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_public_holidays, container, false);

    ListView lv = (ListView) rootView.findViewById(R.id.public_holidays);
    FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

    app = MainApplication.getApp(getContext());
    adapter = new PublicHolidaysEntriesArrayAdapter(
      getContext(), R.layout.public_holidays_listview_item, app.getPublicHolidaysFactory().list(), this);
    lv.setAdapter(adapter);


    fab.setOnClickListener(this);
    return rootView;
  }
  public void onResume() {
    super.onResume();
    if(app.isResumeAfterActivity()) {
      adapter.notifyDataSetChanged();
    app.setResumeAfterActivity(false);
    }
  }

  public boolean onMenuEdit(DayEntry de) {
    app.setResumeAfterActivity(true);
    PublicHolidayActivity.startActivity(getActivity(), de.getName());
    return true;
  }

  public void onMenuDelete(DayEntry de) {
    app.getPublicHolidaysFactory().remove(de);
    adapter.remove(de);
  }

  public void onClick(View view) {
    app.setResumeAfterActivity(true);
    PublicHolidayActivity.startActivity(getActivity(), "null");
  }


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
        adapter.notifyDataSetChanged();
      }
    });
    return anim;
  }

}
