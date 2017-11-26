package fr.ralala.worktime.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import fr.ralala.worktime.ui.activities.DayActivity;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.adapters.ProfilesEntriesArrayAdapter;
import fr.ralala.worktime.ui.adapters.SimpleEntriesArrayAdapterMenuListener;
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

  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);

    ListView lv = rootView.findViewById(R.id.profiles);
    FloatingActionButton fab = rootView.findViewById(R.id.fab);
    fab.setOnClickListener(this);
    app = MainApplication.getApp(getContext());
    adapter = new ProfilesEntriesArrayAdapter(
      getContext(), R.layout.listview_item, app.getProfilesFactory().list(), this);
    lv.setAdapter(adapter);
    return rootView;
  }

  public void onResume() {
    super.onResume();
    if(app.isResumeAfterActivity()) {
      adapter.notifyDataSetChanged();
      app.setResumeAfterActivity(false);
    }
  }

  @Override
  public void onClick(View view) {
    app.setResumeAfterActivity(true);
    DayActivity.startActivity(getActivity(), "null", false);
  }


  public void onMenuEdit(DayEntry de) {
    app.setResumeAfterActivity(true);
    DayActivity.startActivity(getActivity(), de.getName(), false);
  }

  public void onMenuDelete(DayEntry de) {
    app.getProfilesFactory().remove(de);
    adapter.remove(de);
  }
}
