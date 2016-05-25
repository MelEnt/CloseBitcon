package se.melent.closebitconandroid.bubbles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import se.melent.closebitconandroid.extra.AutoLog;

/**
 * Created by EnderCrypt on 19/05/16.
 */
public class BubbleScreen extends View
{
	private Rect screenSize;
	private Paint bgPaint = new Paint();
	private Paint bubblePaint = new Paint();
	private Set<Bubble> bubbles = new HashSet<>();
	private Timer timer = new Timer();

	private static final double rate = 5;

	public BubbleScreen(Context context)
	{
		super(context);
		init();
	}

	public BubbleScreen(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public BubbleScreen(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	public void init()
	{
		bgPaint.setColor(Color.rgb(230, 230, 255));
		bubblePaint.setColor(Color.rgb(175, 175, 200));
		bubblePaint.setStyle(Paint.Style.STROKE);
		bubblePaint.setStrokeWidth(2.5f);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus)
	{
		super.onWindowFocusChanged(hasWindowFocus);
		screenSize = new Rect(0,0,getWidth(),getHeight());
	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		setUpdatingState(true);
	}

	@Override
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		setUpdatingState(false);
	}

	public void setUpdatingState(boolean enable)
	{
		timer.purge();
		if (enable)
		{
			AutoLog.debug("bubble screen drawing {ENABLED}");
			scheduleUpdateTimer();
		}
		else
		{
			AutoLog.debug("bubble screen drawing {DISABLED}");
		}
	}

	private void scheduleUpdateTimer()
	{
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				update();
				postInvalidate();
			}
		},0,50);
	}

	public void update()
	{
		synchronized (bubbles)
		{
			// create new
			double localRate = rate;
			double chance = (1.0 / 100.0) * Math.min(100.0, localRate);
			while (Math.random() <= chance)
			{
				bubbles.add(new Bubble(screenSize));
				localRate -= 100.0;
				chance = (1.0 / 100.0) * Math.min(100.0, localRate);
			}

			// update
			Iterator<Bubble> iterator = bubbles.iterator();
			while (iterator.hasNext())
			{
				Bubble bubble = iterator.next();
				if (bubble.update(bubbles) == false)
				{
					iterator.remove();
				}
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// background
		canvas.drawRect(0f,0f,getWidth(),getHeight(),bgPaint);
		// bubbles
		synchronized (bubbles)
		{
			for (Bubble bubble : bubbles)
			{
				bubble.draw(canvas, bubblePaint);
			}
		}
		// other stuff
		super.onDraw(canvas);
	}
}
