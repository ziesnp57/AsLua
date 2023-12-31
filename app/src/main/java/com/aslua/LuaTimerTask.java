package com.aslua;

import com.aslua.util.TimerTaskX;
import com.luajava.JavaFunction;
import com.luajava.LuaError;
import com.luajava.LuaObject;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;

import java.io.IOException;
import java.util.regex.Pattern;

public class LuaTimerTask extends TimerTaskX
{
	private LuaState L;
	
	private LuaContext mLuaContext;

	private String mSrc;

	private Object[] mArg=new Object[0];

	private boolean mEnabled=true;

	private byte[] mBuffer;

	public LuaTimerTask(LuaContext luaContext, String src) throws LuaError
	{
		this(luaContext, src, null);
	}

	public LuaTimerTask(LuaContext luaContext, String src, Object[] arg) throws LuaError
	{
		mLuaContext =luaContext;
		mSrc = src;
		if (arg != null)
			mArg = arg;
	}

	public LuaTimerTask(LuaContext luaContext, LuaObject func) throws LuaError
	{
		this(luaContext, func, null);
	}

	public LuaTimerTask(LuaContext luaContext, LuaObject func, Object[] arg) throws LuaError
	{

		mLuaContext = luaContext;
		if (arg != null)
			mArg = arg;

		mBuffer = func.dump();
	}
	

	@Override
	public void run()
	{
		if (mEnabled == false)
			return;
		try
		{
			if (L == null)
			{
				initLua();

				if (mBuffer != null)
					newLuaThread(mBuffer, mArg);
				else
					newLuaThread(mSrc, mArg);
			}
			else
			{
				L.getGlobal("run");
				if (!L.isNil(-1))
					runFunc("run");
				else
				{
					if (mBuffer != null)
						newLuaThread(mBuffer, mArg);
					else
						newLuaThread(mSrc, mArg);
				}
			}
		}
		catch (LuaError e)
		{
			mLuaContext.sendError(this.toString(), e);
		}
		L.gc(LuaState.LUA_GCCOLLECT, 1);
		System.gc();
		
	}

	@Override
	public boolean cancel()
	{
		// TODO: Implement this method
		return super.cancel();
	}

	public void setArg(Object[] arg)
	{
		mArg=arg;
	}
	
	public void setArg(LuaObject arg) throws ArrayIndexOutOfBoundsException, LuaError, IllegalArgumentException
	{
		mArg=arg.asArray();
	}
	
	public void setEnabled(boolean enabled)
	{
		mEnabled = enabled;
	}

	public boolean isEnabled()
	{
		return mEnabled;
	}


	public void set(String key, Object value) throws LuaError
	{
		L.pushObjectValue(value);
		L.setGlobal(key);
	}

	public Object get(String key) throws LuaError
	{
		L.getGlobal(key);
		return L.toJavaObject(-1);
	}

	//生成错误信息
	private String errorReason(int error) {
		return switch (error) {
			case 6 -> "错误";
			case 5 -> "垃圾回收错误";
			case 4 -> "内存溢出";
			case 3 -> "语法错误";
			case 2 -> "运行错误";
			case 1 -> "Yield 错误";
			default -> "未知错误 " + error;
		};
	}
	
	private void initLua() throws LuaError
	{
		L = LuaStateFactory.newLuaState();
		L.openLibs();
		L.pushJavaObject(mLuaContext);
		if(mLuaContext instanceof LuaActivity)
		{
			L.setGlobal("activity");
		}
		else if(mLuaContext instanceof LuaService)
		{
			L.setGlobal("service");
		}
		L.pushJavaObject(this);
		L.setGlobal("this");
		
		L.pushContext(mLuaContext);
		
		JavaFunction print = new LuaPrint(mLuaContext,L);
		print.register("print");

		L.getGlobal("package"); 
		
		L.pushString(mLuaContext.getLuaLpath());
		L.setField(-2, "path");
		L.pushString(mLuaContext.getLuaCpath());
		L.setField(-2, "cpath");
		L.pop(1);          

		JavaFunction set = new JavaFunction(L) {
			@Override
			public int execute() throws LuaError
			{

				mLuaContext.set(L.toString(2), L.toJavaObject(3));
				return 0;
			}
		};
		set.register("set");

		JavaFunction call = new JavaFunction(L) {
			@Override
			public int execute() throws LuaError
			{

				int top=L.getTop();
				if (top > 2)
				{
					Object[] args = new Object[top - 2];
					for (int i=3;i <= top;i++)
					{
						args[i - 3] = L.toJavaObject(i);
					}				
					mLuaContext.call(L.toString(2), args);
				}
				else if (top == 2)
				{
					mLuaContext.call(L.toString(2));
				}
				return 0;
			}
		};
		call.register("call");
	}

	private void newLuaThread(String str, Object...args)
	{
		try
		{

			if (Pattern.matches("^\\w+$", str))
			{
				doAsset(str + ".lua", args);
			}
			else if (Pattern.matches("^[\\w\\.\\_/]+$", str))
			{
				L.getGlobal("luajava");
				L.pushString(mLuaContext.getLuaDir());
				L.setField(-2, "luadir"); 
				L.pushString(str);
				L.setField(-2, "luapath"); 
				L.pop(1);

				doFile(str, args);
			}
			else
			{
				doString(str, args);
			}

		}
		catch (Exception e)
		{
			mLuaContext.sendError(this.toString(), e);

		}

	}
	
	private void newLuaThread(byte[] buf, Object...args) throws LuaError 
	{
		int ok = 0;
		L.setTop(0);
		ok = L.LloadBuffer(buf, "TimerTask");

		if (ok == 0)
		{
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			int l=args.length;
			for (int i=0;i < l;i++)
			{
				L.pushObjectValue(args[i]);
			}
			ok = L.pcall(l, 0, -2 - l);
			if (ok == 0)
			{				
				return;
			}
		}
		throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
	}

	private void doFile(String filePath, Object...args) throws LuaError 
	{
		int ok = 0;
		L.setTop(0);
		ok = L.LloadFile(filePath);

		if (ok == 0)
		{
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			int l=args.length;
			for (int i=0;i < l;i++)
			{
				L.pushObjectValue(args[i]);
			}
			ok = L.pcall(l, 0, -2 - l);
			if (ok == 0)
			{				
				return;
			}
		}
		throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
	}

	public void doAsset(String name, Object...args) throws LuaError, IOException 
	{
		int ok = 0;
		byte[] bytes = LuaUtil.readAsset(mLuaContext.getContext(),name);
		L.setTop(0);
		ok = L.LloadBuffer(bytes, name);

		if (ok == 0)
		{
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			int l=args.length;
			for (int i=0;i < l;i++)
			{
				L.pushObjectValue(args[i]);
			}
			ok = L.pcall(l, 0, -2 - l);
			if (ok == 0)
			{				
				return;
			}
		}
		throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
	}

	private void doString(String src, Object...args) throws LuaError
	{			
		L.setTop(0);
		int ok = L.LloadString(src);

		if (ok == 0)
		{
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			int l=args.length;
			for (int i=0;i < l;i++)
			{
				L.pushObjectValue(args[i]);
			}
			ok = L.pcall(l, 0, -2 - l);
			if (ok == 0)
			{				

				return;
			}
		}
		throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
	}


	private void runFunc(String funcName, Object...args)
	{
		try
		{
			L.setTop(0);
			L.getGlobal(funcName);
			if (L.isFunction(-1))
			{
				L.getGlobal("debug");
				L.getField(-1, "traceback");
				L.remove(-2);
				L.insert(-2);

				int l=args.length;
				for (int i=0;i < l;i++)
				{
					L.pushObjectValue(args[i]);
				}

				int ok = L.pcall(l, 1, -2 - l);
				if (ok == 0)
				{				
					return ;
				}
				throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
			}
		}
		catch (LuaError e)
		{
			mLuaContext.sendError(this.toString()+" "+funcName, e);
		}

	}

	private void setField(String key, Object value)
	{
		try
		{
			L.pushObjectValue(value);
			L.setGlobal(key);
		}
		catch (LuaError e)
		{
			mLuaContext.sendError(this.toString(), e);
		}
	}

};
