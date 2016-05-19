package se.melent.closebitconandroid.extra;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by EnderCrypt on 19/05/16.
 */
public class Toasters
{
	private static Context context;

	public static void setContext(Context context)
	{
		Toasters.context = context;
	}

	public static Toast show(String message)
	{
		return advToast(message, Toast.LENGTH_LONG);
	}

	public static Toast show(int stringID)
	{
		return advToast(context.getString(stringID), Toast.LENGTH_LONG);
	}

	public static Toast showQuick(String message)
	{
		return advToast(message, Toast.LENGTH_SHORT);
	}

	public static Toast showQuick(int stringID)
	{
		return advToast(context.getString(stringID), Toast.LENGTH_SHORT);
	}

	public static Toast advToast(String message, int toastLength)
	{
		if (context == null)
		{
			throw new RuntimeException("Please use setContext to set context first");
		}
		final Toast toast = Toast.makeText(context, message, toastLength);
		if (Looper.getMainLooper().getThread() == Thread.currentThread())
		{
			toast.show();
		}
		else
		{
			((Activity)context).runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					toast.show();
				}
			});
		}
		return toast;
	}
}
