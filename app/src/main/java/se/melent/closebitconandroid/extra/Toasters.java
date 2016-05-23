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

	public static void show(String message)
	{
		advToast(message, Toast.LENGTH_LONG);
	}

	public static void show(int stringID)
	{
		advToast(context.getString(stringID), Toast.LENGTH_LONG);
	}

	public static void showQuick(String message)
	{
		advToast(message, Toast.LENGTH_SHORT);
	}

	public static void showQuick(int stringID)
	{
		advToast(context.getString(stringID), Toast.LENGTH_SHORT);
	}

	public static void advToast(final String message, final int toastLength)
	{
		if (context == null)
		{
			throw new RuntimeException("Please use setContext to set context first");
		}
		if (Looper.getMainLooper().getThread() == Thread.currentThread())
		{
			Toast toast = Toast.makeText(context, message, toastLength);
			toast.show();
		}
		else
		{
			((Activity)context).runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast toast = Toast.makeText(context, message, toastLength);
					toast.show();
				}
			});
		}
	}
}
