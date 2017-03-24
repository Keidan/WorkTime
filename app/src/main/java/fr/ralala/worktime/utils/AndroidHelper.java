package fr.ralala.worktime.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.R;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.quickaccess.QuickAccessService;

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


  public static boolean isServiceRunning(final Context context,
                                         final Class<?> serviceClass) {
    if(context == null) return false;
    final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if(manager != null) {
      for (final ActivityManager.RunningServiceInfo service : manager
        .getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.getName().equals(service.service.getClassName())) {
          return true;
        }
      }
    }
    return false;
  }

  public static void killServiceIfRunning(final Context context, final Class<?> serviceClass) {
    if(isServiceRunning(context, serviceClass))
      context.stopService(new Intent(context, serviceClass));
  }

  public static void restartApplication(final Context c, final int string_id) {
    if(string_id != -1)
      toast(c, string_id);
    Intent i = c.getApplicationContext().getPackageManager()
      .getLaunchIntentForPackage(c.getApplicationContext().getPackageName() );
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    c.startActivity(i);
  }

  public static void applyLinearGradient(final View view, final int ...colors) {
    Drawable[] layers = new Drawable[1];

    ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
      @Override
      public Shader resize(int width, int height) {
        LinearGradient lg = new LinearGradient(
          view.getWidth(),
          0,
          0,
          0,
          colors,
          new float[] { 0, 1 },
          Shader.TileMode.CLAMP);
        return lg;
      }
    };
    PaintDrawable p = new PaintDrawable();
    p.setShape(new RectShape());
    p.setShaderFactory(sf);
    layers[0] = p;

    LayerDrawable composite = new LayerDrawable(layers);
    view.setBackgroundDrawable(composite);
  }

  public static void openAnimation(final Activity a) {
    a.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
  }
  public static void closeAnimation(final Activity a) {
    a.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
  }

  public static void sentMailTo(final Activity activity, String mailto, Uri attachment, String subject, String body, String senderMsg) {
    Intent emailIntent = new Intent(Intent.ACTION_SEND);
    emailIntent .setType("application/excel");
    String to[] = {mailto};
    emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
    emailIntent .putExtra(Intent.EXTRA_STREAM, attachment);
    emailIntent .putExtra(Intent.EXTRA_SUBJECT, subject == null ? "" : subject);
    emailIntent.putExtra(Intent.EXTRA_TEXT, body == null ? "" : body);
    activity.startActivity(Intent.createChooser(emailIntent , senderMsg));
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

  public static void openDatePicker(final Context c, final Calendar current, DatePickerDialog.OnDateSetListener li) {
    DatePickerDialog dpd = new DatePickerDialog(c, li, current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
    dpd.show();
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

  private static class ListItem<T> {
    public String name;
    public T value;

    public ListItem(final String name, final T value) {
      this.name = name;
      this.value = value;
    }

    public String toString() {
      return name;
    }
  }

  public interface AlertDialogListListener<T> {
    void onClick(T t);
  }

  public static <T> void showAlertDialog(final Context c, final int title, List<T> list, final AlertDialogListListener yes) {
    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    builder.setTitle(c.getResources().getString(title));
    builder.setIcon(android.R.drawable.ic_dialog_alert);
    List<ListItem> items = new ArrayList<>();
    for(T s : list) {
      String ss = new File(s.toString()).getName().toString();
      if(ss.endsWith("\"}")) ss = ss.substring(0, ss.length() - 2);
      items.add(new ListItem<T>(ss, s));
    }
    final ArrayAdapter<ListItem> arrayAdapter = new ArrayAdapter<>(c, android.R.layout.select_dialog_singlechoice, items);
    builder.setNegativeButton(c.getString(R.string.cancel), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        if(yes != null) yes.onClick(arrayAdapter.getItem(which).value);
      }
    });
    builder.show();
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
