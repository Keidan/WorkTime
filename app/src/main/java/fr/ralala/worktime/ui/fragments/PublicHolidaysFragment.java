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

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.launchers.LauncherCallback;
import fr.ralala.worktime.models.PublicHolidayEntry;
import fr.ralala.worktime.ui.activities.MainActivity;
import fr.ralala.worktime.ui.adapters.PublicHolidaysEntriesArrayAdapter;
import fr.ralala.worktime.ui.utils.SwipeEditDeleteRecyclerViewItem;
import fr.ralala.worktime.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the public holidays fragment view
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class PublicHolidaysFragment extends Fragment implements View.OnClickListener, SwipeEditDeleteRecyclerViewItem.SwipeEditDeleteRecyclerViewItemListener, LauncherCallback {
  private PublicHolidaysEntriesArrayAdapter mAdapter = null;
  private MainApplication mApp = null;
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
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_public_holidays, container, false);
    mActivity = (MainActivity) getActivity();
    assert mActivity != null;
    FloatingActionButton fab = rootView.findViewById(R.id.fab);
    fab.setOnClickListener(this);

    mApp = (MainApplication) mActivity.getApplication();

    mRecyclerView = rootView.findViewById(R.id.public_holidays);
    mRecyclerView.setHasFixedSize(true);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.getRecycledViewPool().clear();
    new SwipeEditDeleteRecyclerViewItem(mActivity, mRecyclerView, this);
    refreshView();
    return rootView;
  }

  /**
   * Refresh the adapter view.
   */
  private void refreshView() {
    mActivity.progressShow(true);
    new Thread(() -> {
      final List<PublicHolidayEntry> list = mApp.getPublicHolidaysFactory().list();
      mAdapter = new PublicHolidaysEntriesArrayAdapter(mRecyclerView,
        R.layout.listview_item, list);
      mActivity.runOnUiThread(() -> {
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.safeNotifyDataSetChanged();
        mActivity.progressDismiss();
      });
    }).start();
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
            UIHelper.snack(a, getString(R.string.help_public_holidays));
        }
        return true;
      }
    }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
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
    mActivity.getLauncherPublicHolidayActivity().startActivity(this, "null");
  }

  /**
   * Called when a ViewHolder is swiped from left to right by the user.
   *
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickEdit(int adapterPosition) {
    PublicHolidayEntry phe = mAdapter.getItem(adapterPosition);
    if (phe == null) return;
    mActivity.getLauncherPublicHolidayActivity().startActivity(this, phe.getName() + "|" + phe.getDay().dateString());
  }

  /**
   * Called when a ViewHolder is swiped from right to left by the user.
   *
   * @param adapterPosition The position in the adapter.
   */
  @Override
  public void onClickDelete(int adapterPosition) {
    final PublicHolidayEntry phe = mAdapter.getItem(adapterPosition);
    if (phe == null) return;
    UIHelper.showConfirmDialog(getActivity(),
      (getString(R.string.delete_public_holiday) + " '" + phe.getName() + "'" + getString(R.string.help)),
      v -> {
        mAdapter.removeItem(phe);
        mApp.getPublicHolidaysFactory().remove(phe);
        UIHelper.snack(mActivity, getString(R.string.public_holidays_removed),
          getString(R.string.undo), nullview -> {
            mAdapter.addItem(phe);
            mApp.getPublicHolidaysFactory().add(phe);
          });
      });

  }
}
