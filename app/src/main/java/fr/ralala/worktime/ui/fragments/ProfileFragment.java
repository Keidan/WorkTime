package fr.ralala.worktime.ui.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import fr.ralala.worktime.ApplicationCtx;
import fr.ralala.worktime.R;
import fr.ralala.worktime.launchers.LauncherCallback;
import fr.ralala.worktime.models.ProfileEntry;
import fr.ralala.worktime.ui.activities.MainActivity;
import fr.ralala.worktime.ui.adapters.EntriesArrayAdapter;
import fr.ralala.worktime.ui.adapters.ProfilesEntriesArrayAdapter;
import fr.ralala.worktime.ui.utils.SwipeEditDeleteRecyclerViewItem;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.utils.AndroidHelper;


/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the profiles fragment view
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ProfileFragment extends Fragment implements View.OnClickListener,
  SwipeEditDeleteRecyclerViewItem.SwipeEditDeleteRecyclerViewItemListener,
  EntriesArrayAdapter.OnLongPressListener<ProfileEntry>, LauncherCallback {

  private ProfilesEntriesArrayAdapter mAdapter = null;
  private ApplicationCtx mApp = null;
  private MainActivity mActivity;
  private RecyclerView mRecyclerView;

  /**
   * Called when the fragment is created.
   *
   * @param inflater           The fragment inflater.
   * @param container          The fragment container.
   * @param savedInstanceState The saved instance state.
   * @return The created view.
   */
  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);
    mActivity = (MainActivity) getActivity();
    assert mActivity != null;
    FloatingActionButton fab = rootView.findViewById(R.id.fab);
    fab.setOnClickListener(this);
    mApp = (ApplicationCtx) mActivity.getApplication();

    mRecyclerView = rootView.findViewById(R.id.profiles);
    mRecyclerView.setHasFixedSize(true);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.getRecycledViewPool().clear();
    new SwipeEditDeleteRecyclerViewItem(mActivity, mRecyclerView, this);
    refreshView();
    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    MenuHost menuHost = mActivity;
    // Add menu items without using the Fragment Menu APIs
    // Note how we can tie the MenuProvider to the viewLifecycleOwner
    // and an optional Lifecycle.State (here, RESUMED) to indicate when
    // the menu should be visible
    menuHost.addMenuProvider(new MenuProvider() {
      public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        // Add menu items here
        menuInflater.inflate(R.menu.profile_fragment, menu);
      }

      public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_help) {
          Activity a = getActivity();
          if (a != null)
            UIHelper.snack(a, getString(R.string.help_profile));
        }
        return true;
      }
    }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
  }

  /**
   * Refresh the adapter view.
   */
  private void refreshView() {
    mActivity.progressShow(true);
    new Thread(() -> {
      final List<ProfileEntry> list = mApp.getProfilesFactory().list();
      mActivity.runOnUiThread(() -> {
        mAdapter = new ProfilesEntriesArrayAdapter(mRecyclerView,
          getContext(), R.layout.listview_item, list);
        mAdapter.setOnLongPressListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.safeNotifyDataSetChanged();
        mActivity.progressDismiss();
      });
    }).start();
  }

  /**
   * Called when a long click is captured.
   *
   * @param t The associated item.
   */
  @Override
  public void onLongPressListener(@NonNull ProfileEntry t) {
    if (!Double.isNaN(t.getLongitude()) && !Double.isNaN(t.getLatitude())) {
      AndroidHelper.vibrate(mActivity, 50);
      AndroidHelper.openDefaultNavigationApp(mActivity, t.getName(), t.getLocation());
    } else
      UIHelper.toast(mActivity, (getString(R.string.no_location_p1) + t.getName() + getString(R.string.no_location_p2)));
  }

  @Override
  public void onLauncherResult(ActivityResult result) {
    refreshView();
  }

  /**
   * Called when the user click on a button (fab).
   *
   * @param view The clicked view.
   */
  @Override
  public void onClick(View view) {
    mActivity.getLauncherDayActivity().startActivity(this, "null", false);
  }

  /**
   * Called when a ViewHolder is swiped from left to right by the user.
   *
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickEdit(int adapterPosition) {
    ProfileEntry pe = mAdapter.getItem(adapterPosition);
    if (pe == null) return;
    mActivity.getLauncherDayActivity().startActivity(this, pe.getName(), false);
  }

  /**
   * Called when a ViewHolder is swiped from right to left by the user.
   *
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickDelete(int adapterPosition) {
    ProfileEntry pe = mAdapter.getItem(adapterPosition);
    if (pe == null) return;
    UIHelper.showConfirmDialog(getActivity(),
      (getString(R.string.delete_profile) + " '" + pe.getName() + "'" + getString(R.string.help)),
      v -> {
        mAdapter.removeItem(pe);
        mApp.getProfilesFactory().remove(pe);
        UIHelper.snack(mActivity, getString(R.string.profile_removed),
          getString(R.string.undo), nullview -> {
            mAdapter.addItem(pe);
            mApp.getProfilesFactory().add(pe);
          });
      });

  }
}
