package se.melent.closebitconandroid.bubbles;

public class Position
{
	public double x;
	public double y;

	public Position()
	{
		this(0.0, 0.0);
	}

	public Position(Position other)
	{
		this(other.x, other.y);
	}

	public Position(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	public Position getLocation()
	{
		return new Position(x, y);
	}

	public double distance(Position other)
	{
		return distance(other.x, other.y);
	}

	public double distance(double x, double y)
	{
		double x_diff = this.x - x;
		double y_diff = this.y - y;
		return Math.sqrt(x_diff * x_diff + y_diff * y_diff);
	}

	public double direction(Position other)
	{
		return direction(other.x, other.y);
	}

	public double direction(double x, double y)
	{
		return Math.atan2((y - this.y), (x - this.x));
	}

	public void add(Vector vector)
	{
		x += vector.motion.x;
		y += vector.motion.y;
	}

	public void add(double direction, double length)
	{
		//direction = Math.toRadians(direction);
		x += Math.cos(direction) * length;
		y += Math.sin(direction) * length;
	}

	public void translate(Position other)
	{
		translate(other.x, other.y);
	}

	public void translate(double x, double y)
	{
		this.x += x;
		this.y += y;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[x=" + x + ",y=" + y + "]";
	}

}
