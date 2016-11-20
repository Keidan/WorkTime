package fr.ralala.worktime.utils;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import fr.ralala.worktime.R;
import fr.ralala.worktime.models.WorkTimeDay;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Helper functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class AndroidHelper {

  public static void sentMailTo(final Activity activity, String mailto, Uri attachment, String subject, String body, String senderMsg) {
    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("*/*");
    if(attachment != null) {
      ContentResolver cR = activity.getContentResolver();
      i.setType(cR.getType(attachment));
      i.putExtra(Intent.EXTRA_STREAM, attachment);
    }
    i.putExtra(Intent.EXTRA_EMAIL, new String[] { mailto });
    i.putExtra(Intent.EXTRA_SUBJECT, subject == null ? "" : subject);
    i.putExtra(Intent.EXTRA_TEXT, body == null ? "" : body);

    activity.startActivity(createEmailOnlyChooserIntent(activity, i, mailto, senderMsg));
  }

  public static Intent createEmailOnlyChooserIntent(final Activity activity, Intent source,
                                                    String mailto,
                                             CharSequence chooserTitle) {
    Stack<Intent> intents = new Stack<>();
    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mailto, null));
    List<ResolveInfo> activities = activity.getPackageManager().queryIntentActivities(i, 0);

    for(ResolveInfo ri : activities) {
      Intent target = new Intent(source);
      target.setPackage(ri.activityInfo.packageName);
      intents.add(target);
    }

    if(!intents.isEmpty()) {
      Intent chooserIntent = Intent.createChooser(intents.remove(0),
        chooserTitle);
      chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
        intents.toArray(new Parcelable[intents.size()]));

      return chooserIntent;
    } else {
      return Intent.createChooser(source, chooserTitle);
    }
  }

  public static String getMonthString(int month) {
    String[] months = new DateFormatSymbols().getMonths();
    return months[month].substring(0, 1).toUpperCase() + months[month].substring(1);
  }

  public static void snack(final Activity activity, int resId) {
    snack(activity, activity.getString(resId));
  }

  public static void snack(final Activity activity, String msg) {
    final View cl = activity.findViewById(R.id.coordinatorLayout);
    final Snackbar snackbar = Snackbar
      .make(cl, msg, Snackbar.LENGTH_LONG);
    snackbar.setAction(R.string.snack_hide, new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        snackbar.dismiss();
      }
    });

    snackbar.show();
  }

  public static void showConfirmDialog(final Context c, final String title,
                                       String message, final android.view.View.OnClickListener yes,
                                       final android.view.View.OnClickListener no) {
    new AlertDialog.Builder(c)
      .setTitle(title)
      .setMessage(message)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          if(yes != null) yes.onClick(null);
        }})
      .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          if(no != null) no.onClick(null);
        }}).show();
  }

  public static void openTimePicker(final Context c, final WorkTimeDay current, final TextView tv) {
    initTimeTextView(current, tv);
    TimePickerDialog timePicker = new TimePickerDialog(c, new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
        tv.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));
      }
    }, current.getHours(), current.getMinutes(), true);//Yes 24 hour time
    timePicker.show();
  }

  public static void initTimeTextView(final WorkTimeDay current, final TextView tv) {
    tv.setText(current.timeString());
  }

  /* tool function used to display a message box */
  public static void showAlertDialog(final Context c, final int title, final int message) {
    showAlertDialog(c, title, c.getResources().getString(message));
  }
  /* tool function used to display a message box */
  public static void showAlertDialog(final Context c, final int title, final String message) {
    AlertDialog alertDialog = new AlertDialog.Builder(c).create();
    alertDialog.setTitle(c.getResources().getString(title));
    alertDialog.setMessage(message);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, c.getResources().getString(R.string.ok),
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
    alertDialog.show();
  }


  public static void forcePopupMenuIcons(final PopupMenu popup) {
    try {
      Field[] fields = popup.getClass().getDeclaredFields();
      for (Field field : fields) {
        if ("mPopup".equals(field.getName())) {
          field.setAccessible(true);
          Object menuPopupHelper = field.get(popup);
          Class<?> classPopupHelper = Class.forName(menuPopupHelper
            .getClass().getName());
          Method setForceIcons = classPopupHelper.getMethod(
            "setForceShowIcon", boolean.class);
          setForceIcons.invoke(menuPopupHelper, true);
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void toast(final Context c, final String message, final int timer) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, timer);
    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
    if (null!=tv) {
      Drawable drawable = c.getResources().getDrawable(R.mipmap.ic_launcher);
      final Bitmap b = ((BitmapDrawable) drawable).getBitmap();
      final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
      tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(c.getResources(), bitmapResized), null, null, null);
      tv.setCompoundDrawablePadding(5);
    }
    toast.show();
  }

  public static void toast_long(final Context c, final int message) {
    toast(c, c.getResources().getString(message), Toast.LENGTH_LONG);
  }

  public static void toast_long(final Context c, final String message) {
    toast(c, message, Toast.LENGTH_LONG);
  }

  public static void toast(final Context c, final String message) {
    toast(c, message, Toast.LENGTH_SHORT);
  }

  public static void toast(final Context c, final int message) {
    toast(c, c.getResources().getString(message));
  }

}
