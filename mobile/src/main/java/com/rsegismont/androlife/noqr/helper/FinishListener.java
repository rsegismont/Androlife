package com.rsegismont.androlife.noqr.helper;

import android.app.Activity;
import android.content.DialogInterface;

public final class FinishListener
  implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener, Runnable
{
  private final Activity activityToFinish;

  public FinishListener(Activity paramActivity)
  {
    this.activityToFinish = paramActivity;
  }

  public void onCancel(DialogInterface paramDialogInterface)
  {
    run();
  }

  public void onClick(DialogInterface paramDialogInterface, int paramInt)
  {
    run();
  }

  public void run()
  {
    this.activityToFinish.finish();
  }
}

/* Location:           D:\Romain\Téléchargements\apkextracter\tools\classes-dex2jar.jar
 * Qualified Name:     com.rsegismont.androlife.noqr.FinishListener
 * JD-Core Version:    0.6.2
 */