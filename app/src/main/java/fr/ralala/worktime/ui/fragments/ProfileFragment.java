package fr.ralala.worktime.ui.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import fr.ralala.worktime.ui.activities.DayActivity;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.adapters.ProfilesEntriesArrayAdapter;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.ui.utils.SwipeEditDeleteRecyclerViewItem;
import fr.ralala.worktime.ui.utils.UIHelper;


/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the profiles fragment view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ProfileFragment  extends Fragment implements View.OnClickListener, SwipeEditDeleteRecyclerViewItem.SwipeEditDeleteRecyclerViewItemListener {

  private ProfilesEntriesArrayAdapter mAdapter = null;
  private MainApplication mApp = null;
  private Activity mActivity;

  /**
   * Called when the fragment is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

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
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);
    FloatingActionButton fab = rootView.findViewById(R.id.fab);
    fab.setOnClickListener(this);
    mApp = MainApplication.getApp(getContext());
    mActivity = getActivity();
    if(mActivity == null)
      return rootView;

    RecyclerView recyclerView = rootView.findViewById(R.id.profiles);
    recyclerView.setHasFixedSize(true);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    mAdapter = new ProfilesEntriesArrayAdapter(
        getContext(), R.layout.listview_item, mApp.getProfilesFactory().list());
    recyclerView.setAdapter(mAdapter);
    mAdapter.notifyDataSetChanged();
    new SwipeEditDeleteRecyclerViewItem(mActivity, recyclerView, this);

    return rootView;
  }

  /**
   * Called when the options menu is created.
   * @param menu The menu to inflate.
   * @param inflater The menu inflater.
   */
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.profile_fragment, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  /**
   * Called when a menu is selected.
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_help:
        Activity a = getActivity();
        if(a == null)
          break;
        UIHelper.snack(a, getString(R.string.help_profile));
        break;
    }
    return true;
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
   * Called when the user click on a button (fab).
   * @param view The clicked view.
   */
  @Override
  public void onClick(View view) {
    mApp.setResumeAfterActivity(true);
    DayActivity.startActivity(getActivity(), "null", false);
  }
  /**
   * Called when a ViewHolder is swiped from left to right by the user.
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickEdit(int adapterPosition) {
    DayEntry de = mAdapter.getItem(adapterPosition);
    if(de == null) return;
    mApp.setResumeAfterActivity(true);
    DayActivity.startActivity(getActivity(), de.getName(), false);
  }

  /**
   * Called when a ViewHolder is swiped from right to left by the user.
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickDelete(int adapterPosition) {
    DayEntry de = mAdapter.getItem(adapterPosition);
    if(de == null) return;
    UIHelper.showConfirmDialog(getActivity(),
        (getString(R.string.delete_public_holiday) + " '" + de.getName() + "'" + getString(R.string.help)),
        (v) -> {
          mAdapter.removeItem(de);
          mApp.getProfilesFactory().remove(de);
          UIHelper.snack(mActivity, getString(R.string.profile_removed),
              getString(R.string.undo), (nullview) -> {
                mAdapter.addItem(de);
                mApp.getProfilesFactory().add(de);
              });
        });

  }
}
