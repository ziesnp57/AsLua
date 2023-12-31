package com.aslua;

import com.aslua.util.TimerX;
import com.luajava.LuaError;
import com.luajava.LuaObject;

public class LuaTimer extends TimerX implements LuaGcable
{

	private boolean mGc;

	@Override
	public void gc() {
		// TODO: Implement this method
		stop();
		mGc=true;
	}

	@Override
	public boolean isGc() {
		return mGc;
	}

	private LuaTimerTask task;
	
	public LuaTimer(LuaContext main,String src) throws LuaError
	{
		this(main,src,null);
	}
	public LuaTimer(LuaContext main,String src,Object[] arg) throws LuaError
	{
		super("LuaTimer");
		main.regGc(this);
		task= new LuaTimerTask(main, src,arg);
	}
	
	public LuaTimer(LuaContext main,LuaObject func) throws LuaError
	{
		this(main,func,null);
	}
	public LuaTimer(LuaContext main,LuaObject func,Object[] arg) throws LuaError
	{
		super("LuaTimer");
		main.regGc(this);
		task= new LuaTimerTask(main, func, arg);
	}
	
	public void start(long delay, long period)
	{
		schedule(task,delay,period);
	}
	
	public void start(long delay)
	{
		schedule(task,delay);
	}
	
	public void stop()
	{
		task.cancel();
	}
	
	public void setEnabled(boolean enabled)
	{
		task.setEnabled(enabled);
	}

	public boolean isEnabled()
	{
		return task.isEnabled();
	}
	
	public boolean getEnabled()
	{
		return task.isEnabled();
	}
	
	public void setPeriod(long period)
	{
		task.setPeriod(period);
	}

	public long getPeriod()
	{
		return task.getPeriod();
	}
}
