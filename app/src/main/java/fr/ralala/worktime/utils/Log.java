package fr.ralala.worktime.utils;

import android.content.Context;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.ralala.worktime.ApplicationCtx;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Log
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class Log {
  private static final int CIRCULAR_BUFFER_DEPTH = 2000;

  public enum Type {
    INFO,
    ERROR
  }

  private final Queue<String> mQueue;
  private final Lock mLock = new ReentrantLock();

  public Log() {
    mQueue = new CircularFifoQueue<>(CIRCULAR_BUFFER_DEPTH);
  }

  public Queue<String> getQueue() {
    return mQueue;
  }

  /**
   * Adds a new entry to the logs.
   *
   * @param c   Android context.
   * @param tag Log tag (can be null).
   * @param msg Log message.
   * @param ex  Exception.
   */
  public static void error(final Context c, final String tag, final String msg, Throwable ex) {
    add(c, tag, msg, Type.ERROR, ex);
  }

  /**
   * Adds a new entry to the logs.
   *
   * @param c   Android context.
   * @param tag Log tag (can be null).
   * @param msg Log message.
   */
  public static void error(final Context c, final String tag, final String msg) {
    error(c, tag, msg, null);
  }

  /**
   * Adds a new entry to the logs.
   *
   * @param c   Android context.
   * @param tag Log tag (can be null).
   * @param msg Log message.
   */
  public static void info(final Context c, final String tag, final String msg) {
    add(c, tag, msg, Type.INFO, null);
  }

  /**
   * Adds a new entry to the logs.
   *
   * @param c    Android context.
   * @param tag  Log tag (can be null).
   * @param msg  Log message.
   * @param type Log type.
   * @param ex   Exception.
   */
  private static void add(final Context c, final String tag, final String msg, Type type, Throwable ex) {
    ApplicationCtx ctx;
    if (c instanceof ApplicationCtx)
      ctx = (ApplicationCtx) c;
    else
      ctx = (ApplicationCtx) c.getApplicationContext();
    ctx.getLog().mLock.lock();
    String head = new SimpleDateFormat("yyyyMMdd [hhmmssa]:\n",
      Locale.US).format(new Date());
    if (tag != null)
      head += "(" + tag + ") -> ";
    ctx.getLog().mQueue.add(head + msg);
    ctx.getLog().mLock.unlock();
    if (type == Type.INFO) {
      android.util.Log.i(tag, msg);
    } else if (type == Type.ERROR) {
      if (ex == null)
        android.util.Log.e(tag, msg);
      else
        android.util.Log.e(tag, msg, ex);
    }
  }
}
