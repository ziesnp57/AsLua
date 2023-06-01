
package com.luajava;

/**
 * LuaJava exception
 *  
 * @author Thiago Ponte
 *
 */
public class LuaException extends Exception
{
	private static final long serialVersionUID = 1L;

	public LuaException(String str)
	{
		super(str);
	}
	
	/**
	 * Will work only on Java 1.4 or later.
	 * To work with Java 1.3, comment the first line and uncomment the second one.
	 */
	public LuaException(Exception e)
	{
	   super((e.getCause() != null) ? e.getCause() : e);
	}
}
