package fr.ralala.worktime.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;

import fr.ralala.worktime.utils.AndroidHelper;

/**
 * Created by MG on 03-04-2016.
 * http://www.truiton.com/2016/04/obtaining-runtime-permissions-android-marshmallow-6-0/
 */
public abstract class RuntimePermissionsActivity extends AppCompatActivity {
  private SparseIntArray mErrorString;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidHelper.openAnimation(this);
    super.onCreate(savedInstanceState);
    mErrorString = new SparseIntArray();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    AndroidHelper.closeAnimation(this);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    int permissionCheck = PackageManager.PERMISSION_GRANTED;
    for (int permission : grantResults) {
      permissionCheck = permissionCheck + permission;
    }
    if ((grantResults.length > 0) && permissionCheck == PackageManager.PERMISSION_GRANTED) {
      onPermissionsGranted(requestCode);
    } else {
      Snackbar.make(findViewById(android.R.id.content), mErrorString.get(requestCode),
        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
          }
        }).show();
    }
  }

  public void requestAppPermissions(final String[] requestedPermissions,
                                    final int stringId, final int requestCode) {
    mErrorString.put(requestCode, stringId);
    int permissionCheck = PackageManager.PERMISSION_GRANTED;
    boolean shouldShowRequestPermissionRationale = false;
    for (String permission : requestedPermissions) {
      permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
      shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      if (shouldShowRequestPermissionRationale) {
        Snackbar.make(findViewById(android.R.id.content), stringId,
          Snackbar.LENGTH_INDEFINITE).setAction("GRANT",
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              ActivityCompat.requestPermissions(RuntimePermissionsActivity.this, requestedPermissions, requestCode);
            }
          }).show();
      } else {
        ActivityCompat.requestPermissions(this, requestedPermissions, requestCode);
      }
    } else {
      onPermissionsGranted(requestCode);
    }
  }

  public abstract void onPermissionsGranted(int requestCode);
}
