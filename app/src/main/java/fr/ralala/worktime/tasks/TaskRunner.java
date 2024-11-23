package fr.ralala.worktime.tasks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Task runner
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public abstract class TaskRunner<C, P, I, R> implements TaskRunnerCallback<C, P, I, R> {
  private static final String EXCEPTION_TAG = "Exception: ";
  private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
  private final Handler mHandler = new Handler(Looper.getMainLooper());
  protected final AtomicBoolean mCancel = new AtomicBoolean(false);

  /**
   * This method can be invoked from {@link #doInBackground} to
   * publish updates on the UI thread while the background computation is
   * still running.
   *
   * @param value The progress value to update the UI with.
   * @see #onProgressUpdate
   * @see #doInBackground
   */
  public void publishProgress(I value) {
    mHandler.post(() -> onProgressUpdate(value));
  }

  /**
   * Cancels the task, this call generates a call to the {@link #onCancelled} method.
   */
  public void cancel() {
    mCancel.set(true);
    mExecutor.shutdownNow();
    mHandler.post(this::onCancelled);
  }

  /**
   * Tests if the task is cancelled or not.
   *
   * @return boolean
   */
  public boolean isCancelled() {
    return mCancel.get();
  }

  public void execute(final P param) {
    final C config = onPreExecute();
    mExecutor.submit(() -> {
      R result = null;
      try {
        result = doInBackground(config, param);
      } catch (Exception e) {
        Log.e(getClass().getName(), EXCEPTION_TAG + e.getMessage(), e);
        onException(e);
      } finally {
        final R finalResult = result;
        mHandler.post(() -> onPostExecute(finalResult));
        if (!mExecutor.isShutdown())
          mExecutor.shutdown();
      }
    });
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public C onPreExecute() {
    /* user implementation */
    return null;
  }

  /**
   * Runs on the UI thread.
   *
   * @param value The value indicating progress.
   */
  @Override
  public void onProgressUpdate(I value) {
    /* user implementation */
  }

  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  @Override
  public void onPostExecute(final R result) {
    /* user implementation */
  }

  /**
   * Called when the async task is cancelled.
   */
  @Override
  public void onCancelled() {
    /* user implementation */
  }

  /**
   * Called when the method {@link #doInBackground} raises an exception
   *
   * @param t The exception.
   */
  @Override
  public void onException(Throwable t) {
    /* user implementation */
  }
}

