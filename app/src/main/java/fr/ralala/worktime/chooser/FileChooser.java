package fr.ralala.worktime.chooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;


import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import fr.ralala.worktime.AndroidHelper;
import fr.ralala.worktime.R;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * File chooser activity
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class FileChooser extends AppCompatActivity implements AdapterView.OnItemClickListener{
  public static final String FILECHOOSER_TYPE_KEY = "type";
  public static final String FILECHOOSER_TITLE_KEY = "title";
  public static final String FILECHOOSER_MESSAGE_KEY = "message";
  public static final String FILECHOOSER_SHOW_KEY = "show";
  public static final String FILECHOOSER_FILE_FILTER_KEY = "file_filter";
  public static final String FILECHOOSER_DEFAULT_DIR = "default_dir";
  public static final String FILECHOOSER_USER_MESSAGE = "user_message";
  public static final int FILECHOOSER_TYPE_FILE_ONLY = 0;
  public static final int FILECHOOSER_TYPE_DIRECTORY_ONLY  = 1;
  public static final int FILECHOOSER_TYPE_FILE_AND_DIRECTORY = 2;
  public static final int FILECHOOSER_SHOW_DIRECTORY_ONLY = 1;
  public static final int FILECHOOSER_SHOW_FILE_AND_DIRECTORY = 2;
  public static final String FILECHOOSER_FILE_FILTER_ALL = "*";
  protected File currentDir = null;
  protected File defaultDir = null;
  private FileArrayAdapter adapter = null;
  private String confirmMessage = null;
  private String confirmTitle = null;
  private String userMessage = null;
  private String fileFilter = FILECHOOSER_FILE_FILTER_ALL;
  private int type = FILECHOOSER_TYPE_FILE_AND_DIRECTORY;
  private int show = FILECHOOSER_SHOW_FILE_AND_DIRECTORY;
  private ListView listview = null;

  private final OnItemLongClickListener longClick = new OnItemLongClickListener() {
    @Override
    public boolean onItemLongClick(
        final AdapterView<?> parent, final View v, final int position, final long id) {
      final Option o = adapter.getItem(position);
      if (o == null || o.getPath() == null)
        return false;
      boolean folder = false;
      if (o.getData().equalsIgnoreCase("folder"))
        folder = true;
      if (!o.getData().equalsIgnoreCase("parent directory")) {
        if (folder && type == FILECHOOSER_TYPE_FILE_ONLY)
          return false;
        if (!folder && type == FILECHOOSER_TYPE_DIRECTORY_ONLY)
          return false;
        confirm(o);

      }
      return true;
    }
  };
  
  private void confirm(final Option o) {
    final android.view.View.OnClickListener yes = new android.view.View.OnClickListener() {
      @Override
      public void onClick(
          android.view.View view) {
        onFileSelected(o);
      }
    };
    final android.view.View.OnClickListener no = new android.view.View.OnClickListener() {
      @Override
      public void onClick(
          android.view.View view) {
        onFileSelected(null);
      }
    };
    AndroidHelper.showConfirmDialog(
            this,
            confirmTitle, confirmMessage + "\n" + o.getPath(),
            yes, no);
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.content_filechooser);
    listview = (ListView)findViewById(R.id.list);
    Bundle b = getIntent().getExtras();
    if (b != null && b.containsKey(FILECHOOSER_TYPE_KEY))
      type = Integer.parseInt(b.getString(FILECHOOSER_TYPE_KEY));
    if (b != null && b.containsKey(FILECHOOSER_TITLE_KEY))
      confirmTitle = b.getString(FILECHOOSER_TITLE_KEY);
    if (b != null && b.containsKey(FILECHOOSER_MESSAGE_KEY))
      confirmMessage = b.getString(FILECHOOSER_MESSAGE_KEY);
    if (b != null && b.containsKey(FILECHOOSER_SHOW_KEY))
      show = Integer.parseInt(b.getString(FILECHOOSER_SHOW_KEY));
    if (b != null && b.containsKey(FILECHOOSER_FILE_FILTER_KEY))
      fileFilter = b.getString(FILECHOOSER_FILE_FILTER_KEY);
    if (b != null && b.containsKey(FILECHOOSER_DEFAULT_DIR))
      defaultDir = new File(""+b.getString(FILECHOOSER_DEFAULT_DIR));
    if (b != null && b.containsKey(FILECHOOSER_USER_MESSAGE))
      userMessage = b.getString(FILECHOOSER_USER_MESSAGE);
    if(confirmTitle == null) confirmTitle = "title";
    if(confirmMessage == null) confirmMessage = "message";
    currentDir = defaultDir;
    listview.setLongClickable(true);
    fill(currentDir);
    listview.setOnItemLongClickListener(longClick);
    listview.setOnItemClickListener(this);
    AndroidHelper.toast_long(this, R.string.chooser_long_press_message);
  }

  @SuppressWarnings("deprecation")
  protected void fill(final File f) {
    final File[] dirs = f.listFiles();
    this.setTitle(f.getAbsolutePath());
    final List<Option> dir = new ArrayList<>();
    final List<Option> fls = new ArrayList<>();
    try {
      for (final File ff : dirs) {
        if (ff.isDirectory())
          dir.add(new Option(ff.getName(), "Folder", ff.getAbsolutePath(), getResources().getDrawable(R.mipmap.ic_folder)));
        else if(show != FILECHOOSER_SHOW_DIRECTORY_ONLY) {
          if(isFiltered(ff))
            fls.add(new Option(ff.getName(), "File Size: " + ff.length(), ff
                .getAbsolutePath(), getResources().getDrawable(R.mipmap.ic_file)));
        }
      }
    } catch (final Exception e) {
      Log.e(getClass().getSimpleName(), "Exception : " + e.getMessage(), e);
    }
    Collections.sort(dir);
    if(!fls.isEmpty()){
      Collections.sort(fls);
      dir.addAll(fls);
    }
    dir.add(
        0,
        new Option("..", "Parent Directory", f.getParent(), getResources().getDrawable(R.mipmap.ic_folder)));
    // if(adapter == null) {
    adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
    listview.setAdapter(adapter);
    // } else
    // adapter.reload(dir);
  }
  
  public boolean isFiltered(final File file) {
    StringTokenizer token = new StringTokenizer(fileFilter, ",");
    while(token.hasMoreTokens()) {
      String filter = token.nextToken();
      if(filter.equals("*")) return true;
      if(file.getName().endsWith("." + filter)) return true;
    }
    return false;
  }

  @Override
  public void onItemClick(final AdapterView<?> l, final View v,
      final int position, final long id) {
    final Option o = adapter.getItem(position);
    if (o == null || o.getPath() == null)
      return;
    if (o.getData().equalsIgnoreCase("folder")
        || o.getData().equalsIgnoreCase("parent directory")) {
      currentDir = new File(o.getPath());
      fill(currentDir);
    } else if (type == FILECHOOSER_TYPE_FILE_ONLY)
      confirm(o);
  }

  protected void onFileSelected(final Option opt) {
  }

  public String getUserMessage() {
    return userMessage;
  }
}
