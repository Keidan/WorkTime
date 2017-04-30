package fr.ralala.worktime.fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import fr.ralala.worktime.activities.DayActivity;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.adapters.ProfilesEntriesArrayAdapter;
import fr.ralala.worktime.adapters.SimpleEntriesArrayAdapterMenuListener;
import fr.ralala.worktime.models.DayEntry;


/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the profiles fragment view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ProfileFragment  extends Fragment implements SimpleEntriesArrayAdapterMenuListener<DayEntry>, View.OnClickListener {

  private ProfilesEntriesArrayAdapter adapter = null;
  private MainApplication app = null;
  private boolean resumeAfterActivity = false;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_profile, container, false);

    ListView lv = (ListView) rootView.findViewById(R.id.profiles);
    FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
    fab.setOnClickListener(this);
    app = MainApplication.getApp(getContext());
    adapter = new ProfilesEntriesArrayAdapter(
      getContext(), R.layout.public_holidays_listview_item, app.getProfilesFactory().list(), this);
    lv.setAdapter(adapter);
    return rootView;
  }

  public void onResume() {
    super.onResume();
    if(resumeAfterActivity) {
      adapter.notifyDataSetChanged();
      resumeAfterActivity = false;
    }
  }

  @Override
  public void onClick(View view) {
    DayActivity.startActivity(getActivity(), "null", false);
  }


  public boolean onMenuEdit(DayEntry de) {
    resumeAfterActivity = true;
    DayActivity.startActivity(getActivity(), de.getName(), false);
    return true;
  }

  public void onMenuDelete(DayEntry de) {
    app.getProfilesFactory().remove(de);
    adapter.remove(de);
  }
}
