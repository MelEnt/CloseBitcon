package se.melent.closebitconandroid.bubbles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Size;

import java.util.Set;

/**
 * Created by EnderCrypt on 19/05/16.
 */
public class Bubble
{
	private Rect screenSize;
	private double size = 1.0;
	private double squareSize = 0.0;
	private Position position;
	private Vector vector = new Vector();
	private boolean isBottom = true;

	public Bubble(Rect screenSize)
	{
		this.screenSize = screenSize;
		position = new Position(Math.random() * screenSize.width(), screenSize.height());
	}

	public boolean update(Set<Bubble> bubbles)
	{
		// grow if on bottom
		if (isBottom)
		{
			addSize(Math.random() * 10);
			stickToBottom();
			if (size >= (Math.random()*16000))
			{
				isBottom = false;
			}
		}
		// check if dead (no size)
		if (size < 1.0)
		{
			return false;
		}
		// collide
		for (Bubble other : bubbles)
		{
			if (this.size > other.size)//if (this != other)
			{
				double dist = position.distance(other.position);
				double collideDist = (squareSize / 2) + (other.squareSize / 2);
				if (dist <= collideDist)
				{
					stealSize(other, dist);
				}
			}
		}
		// update
		updateMovement();
		if (isFailingBoundry())
		{
			return false;
		}
		return true;
	}

	public boolean isFailingBoundry()
	{
		if (position.y < -(squareSize / 2)) // top of screen
			return true;
		return false;
	}

	public void stealSize(Bubble other, double distance)
	{
		boolean colliding = true;
		while (colliding)
		{
			addSize(1);
			other.addSize(-1);
			double collideDist = (squareSize / 2) + (other.squareSize / 2);
			colliding = (collideDist > distance);
		}
	}

	public void addSize(double size)
	{
		this.size += size;
		squareSize = Math.sqrt(this.size)*5;
	}

	public void stickToBottom()
	{
		position.y = screenSize.height() - (squareSize / 2);
	}

	public void updateMovement()
	{
		// hit left wall
		double radius = (squareSize / 2);
		if (position.x < radius)
		{
			double into = (position.x + radius);
			vector.motion.x += into/16;
		}
		// hit right wall
		if (position.x > screenSize.width() - radius)
		{
			double into = (position.x + radius) - screenSize.width();
			vector.motion.x -= into/16;
		}
		// move
		vector.motion.y -= (size / 3000);
		vector.multiplyLength(0.95);
		position.add(vector);
		if (isBottom)
		{
			position.y = screenSize.height() - (squareSize / 2);
		}
		// prevent from going below screen
		if (position.y > screenSize.height() - (squareSize / 2) + 1)
			position.y = screenSize.height() - (squareSize / 2);
	}

	public void push(double direction, double force)
	{
		vector.addVector(direction, force);
	}

	public void draw(Canvas canvas, Paint paint)
	{
		canvas.drawCircle((float)position.x,(float)position.y, (float)squareSize/2 , paint);
	}

	public Position getPosition()
	{
		return new Position(position);
	}
}
