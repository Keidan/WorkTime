package fr.ralala.worktime.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import fr.ralala.worktime.ui.activities.PublicHolidayActivity;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.adapters.PublicHolidaysEntriesArrayAdapter;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.ui.utils.SwipeEditDeleteRecyclerViewItem;
import fr.ralala.worktime.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the public holidays fragment view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class PublicHolidaysFragment extends Fragment implements View.OnClickListener, SwipeEditDeleteRecyclerViewItem.SwipeEditDeleteRecyclerViewItemListener{
  private PublicHolidaysEntriesArrayAdapter mAdapter = null;
  private MainApplication mApp = null;
  private Activity mActivity;
  private RecyclerView mRecyclerView;

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
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_public_holidays, container, false);
    mActivity = getActivity();
    if(mActivity == null)
      return rootView;
    FloatingActionButton fab = rootView.findViewById(R.id.fab);
    fab.setOnClickListener(this);

    mApp = MainApplication.getApp(getContext());

    mRecyclerView = rootView.findViewById(R.id.public_holidays);
    mRecyclerView.setHasFixedSize(true);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.getRecycledViewPool().clear();
    mAdapter = new PublicHolidaysEntriesArrayAdapter(mRecyclerView,
        R.layout.listview_item, mApp.getPublicHolidaysFactory().list());
    mRecyclerView.setAdapter(mAdapter);
    mAdapter.safeNotifyDataSetChanged();
    new SwipeEditDeleteRecyclerViewItem(mActivity, mRecyclerView, this);
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
        UIHelper.snack(a, getString(R.string.help_public_holidays));
        break;
    }
    return true;
  }

  /**
   * Receive the result from a previous call to startActivityForResult
   * @param requestCode The integer request code originally supplied to startActivityForResult.
   * @param resultCode The integer result code returned by the child activity through its setResult().
   * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == PublicHolidayActivity.REQUEST_START_ACTIVITY)
      new Thread(() -> {
        try { Thread.sleep(100); } catch(InterruptedException ie) {
          Log.e(getClass().getSimpleName(), "Exception: " + ie.getMessage(), ie);
        }
        mActivity.runOnUiThread(() -> mAdapter.safeNotifyDataSetChanged());
      }).start();
  }

  /**
   * Called when the user click on a button (fab).
   * @param view The clicked view.
   */
  @Override
  public void onClick(View view) {
    PublicHolidayActivity.startActivity(this, "null");
  }

  /**
   * Called when a ViewHolder is swiped from left to right by the user.
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickEdit(int adapterPosition) {
    DayEntry de = mAdapter.getItem(adapterPosition);
    if(de == null) return;
    PublicHolidayActivity.startActivity(this, de.getName() + "|" + de.getDay().dateString());
  }

  /**
   * Called when a ViewHolder is swiped from right to left by the user.
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickDelete(int adapterPosition) {
    final DayEntry de = mAdapter.getItem(adapterPosition);
    if(de == null) return;
    UIHelper.showConfirmDialog(getActivity(),
        (getString(R.string.delete_public_holiday) + " '" + de.getName() + "'" + getString(R.string.help)),
        (v) -> {
          mAdapter.removeItem(de);
          mApp.getPublicHolidaysFactory().remove(de);
          UIHelper.snack(mActivity, getString(R.string.public_holidays_removed),
              getString(R.string.undo), (nullview) -> {
                mAdapter.addItem(de);
                mApp.getPublicHolidaysFactory().add(de);
              });
        });

  }
}
