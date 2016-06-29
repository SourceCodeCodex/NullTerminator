package utils;

public class Pair <K,V>
{
	private K key;
	private V value;
	
	public Pair(K key, V value) 
	{
		this.key = key;
		this.value = value;
	}

	public K getKey() 
	{
		return key;
	}

	public V getValue() 
	{
		return value;
	}
	
	@Override
	public boolean equals (Object o)
	{
		Pair pair = (Pair)o;
		if (pair.getKey().equals(key) && pair.getValue().equals(value))
			return true;
		return false;
	}
}
