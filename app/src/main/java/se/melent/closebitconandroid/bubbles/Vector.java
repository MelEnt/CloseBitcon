package se.melent.closebitconandroid.bubbles;

public class Vector
{
	public final Motion motion;

	public Vector()
	{
		this(0, 0);
	}

	public Vector(double x, double y)
	{
		motion = new Motion(x, y);
	}

	public void addVector(Vector vector)
	{
		motion.add(vector.motion.x, vector.motion.y);
	}

	public void addDegreeVector(double degreeDirection, double length)
	{
		addVector(Math.toRadians(degreeDirection), length);
	}

	public void addVector(double radDirection, double length)
	{
		double x = Math.cos(radDirection) * length;
		double y = Math.sin(radDirection) * length;
		motion.add(x, y);
	}

	public void setDegreeVector(double degreeDirection, double length)
	{
		setVector(Math.toRadians(degreeDirection), length);
	}

	public void setVector(double radDirection, double length)
	{
		double x = Math.cos(radDirection) * length;
		double y = Math.sin(radDirection) * length;
		motion.set(x, y);
	}

	public double getDirection()
	{
		return Math.atan2(motion.y, motion.x);
	}

	public double getLength()
	{
		return Math.sqrt(motion.x * motion.x + motion.y * motion.y);
	}

	public void truncateLength(double max)
	{
		setVector(getDirection(), Math.min(max, getLength()));
	}

	/**
	 * multiplies the length of this vector by the value parameter
	 * 1.0 = no change
	 * 0.0 = length gets set to 0
	 */
	public void multiplyLength(double multiplier)
	{
		setVector(getDirection(), getLength() * multiplier);
	}

	// motion
	public class Motion
	{
		public double x = 0;
		public double y = 0;

		private Motion(double x, double y)
		{
			set(x, y);
		}

		public void add(double x, double y)
		{
			set(this.x + x, this.y + y);
		}

		public void set(double x, double y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString()
		{
			return "x=" + (motion.x) + ",y=" + (motion.y);
		}
	}

	@Override
	public String toString()
	{
		return getClass().getName() + "[" + motion + "]";
	}
}
