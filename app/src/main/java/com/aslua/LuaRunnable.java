package com.aslua;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.luajava.JavaFunction;
import com.luajava.LuaError;
import com.luajava.LuaMetaTable;
import com.luajava.LuaObject;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Pattern;

public class LuaRunnable extends Thread implements Runnable,LuaMetaTable,LuaGcable {

	private boolean mGc;

	@Override
	public void gc() {
		// TODO: Implement this method
		quit();
		mGc=true;
	}

	@Override
	public boolean isGc() {
		return mGc;
	}


	@Override
	public Object __call(Object[] arg) {
		// TODO: Implement this method
		return null;
	}

	@Override
	public Object __index(final String key) {
		// TODO: Implement this method
		return new LuaMetaTable(){
			@Override
			public Object __call(Object[] arg) {
				// TODO: Implement this method
				call(key, arg);
				return null;
			}

			@Override
			public Object __index(String key) {
				// TODO: Implement this method
				return null;
			}

			@Override
			public void __newIndex(String key, Object value) {
				// TODO: Implement this method
			}
		};
	}

	@Override
	public void __newIndex(String key, Object value) {
		// TODO: Implement this method
		set(key, value);
	}

	private LuaState L;
	private Handler thandler;
	public boolean isRun = false;
	private LuaContext mLuaContext;

	private boolean mIsLoop;

	private String mSrc;

	private Object[] mArg=new Object[0];

	private byte[] mBuffer;

	public LuaRunnable(LuaContext luaContext, String src) throws LuaError {
		this(luaContext, src, false, null);
	}

	public LuaRunnable(LuaContext luaContext, String src, Object[] arg) throws LuaError {
		this(luaContext, src, false, arg);
	}

	public LuaRunnable(LuaContext luaContext, String src, boolean isLoop) throws LuaError {
		this(luaContext, src, isLoop, null);
	}

	public LuaRunnable(LuaContext luaContext, String src, boolean isLoop, Object[] arg) throws LuaError {
		luaContext.regGc(this);
		mLuaContext = luaContext;
		mSrc = src;
		mIsLoop = isLoop;
		if (arg != null)
			mArg = arg;
	}
	public LuaRunnable(LuaContext luaContext, LuaObject func) throws LuaError {
		this(luaContext, func, false, null);
	}
	public LuaRunnable(LuaContext luaContext, LuaObject func, Object[] arg) throws LuaError {
		this(luaContext, func, false, arg);
	}
	public LuaRunnable(LuaContext luaContext, LuaObject func, boolean isLoop) throws LuaError {
		this(luaContext, func, isLoop, null);
	}

	public LuaRunnable(LuaContext luaContext, LuaObject func, boolean isLoop, Object[] arg) throws LuaError {
		mLuaContext = luaContext;
		if (arg != null)
			mArg = arg;
		mIsLoop = isLoop;
		mBuffer = func.dump();
	}

	@Override
	public void run() {
		try {

			if (L == null) {
				initLua();

				if (mBuffer != null)
					newLuaRunnable(mBuffer, mArg);
				else
					newLuaRunnable(mSrc, mArg);
			}
		}
		catch (LuaError e) {
			mLuaContext.sendMsg(e.getMessage());
			return;
		}
		if (mIsLoop) {
			Looper.prepare();
			thandler = new ThreadHandler();
			isRun = true;
			L.getGlobal("run");
			if (!L.isNil(-1)) {
				L.pop(1);
				runFunc("run");
			}

			Looper.loop();
		}
		isRun = false;
		L.gc(LuaState.LUA_GCCOLLECT, 1);
		System.gc();
	}

	public void call(String func) {
		push(3, func);
	}

	public void call(String func, Object[] args) {
		if (args.length == 0)
			push(3, func);
		else
			push(1, func, args);
	}

	public void set(String key, Object value) {
		push(4, key, new Object[]{ value});
	}

	public Object get(String key) throws LuaError {
		L.getGlobal(key);
		return L.toJavaObject(-1);
	}

	public void quit() {
		if (isRun) {
			isRun = false;
			thandler.getLooper().quit();
		}
	}

	public void push(int what, String s) {
		if (!isRun) {
			mLuaContext.sendMsg("thread is not running");
			return;
		}

		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("data", s);
		message.setData(bundle);
		message.what = what;

		thandler.sendMessage(message);

	}

	public void push(int what, String s, Object[] args) {
		if (!isRun) {
			mLuaContext.sendMsg("thread is not running");
			return;
		}

		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("data", s);
		bundle.putSerializable("args", args);
		message.setData(bundle);
		message.what = what;

		thandler.sendMessage(message);

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

	private void initLua() throws LuaError {
		L = LuaStateFactory.newLuaState();
		L.openLibs();
		L.pushJavaObject(mLuaContext.getContext());
		if (mLuaContext instanceof LuaActivity) {
			L.setGlobal("activity");
		}
		else if (mLuaContext instanceof LuaService) {
			L.setGlobal("service");
		}
		L.pushJavaObject(this);
		L.setGlobal("this");
		L.pushContext(mLuaContext);

		JavaFunction print = new LuaPrint(mLuaContext, L);
		print.register("print");

		L.getGlobal("package");

		L.pushString(mLuaContext.getLuaLpath());
		L.setField(-2, "path");
		L.pushString(mLuaContext.getLuaCpath());
		L.setField(-2, "cpath");
		L.pop(1);

		JavaFunction set = new JavaFunction(L) {
			@Override
			public int execute() throws LuaError {

				mLuaContext.set(L.toString(2), L.toJavaObject(3));
				return 0;
			}
		};
		set.register("set");

		JavaFunction call = new JavaFunction(L) {
			@Override
			public int execute() throws LuaError {

				int top=L.getTop();
				if (top > 2) {
					Object[] args = new Object[top - 2];
					for (int i=3;i <= top;i++) {
						args[i - 3] = L.toJavaObject(i);
					}
					mLuaContext.call(L.toString(2), args);
				}
				else if (top == 2) {
					mLuaContext.call(L.toString(2));
				}
				return 0;
			}
		};
		call.register("call");
	}

	private void newLuaRunnable(String str, Object...args) {
		try {

			if (Pattern.matches("^\\w+$", str)) {
				doAsset(str + ".lua", args);
			}
			else if (Pattern.matches("^[\\w\\.\\_/]+$", str)) {
				L.getGlobal("luajava");
				L.pushString(mLuaContext.getLuaDir());
				L.setField(-2, "luadir");
				L.pushString(str);
				L.setField(-2, "luapath");
				L.pop(1);

				doFile(str, args);
			}
			else {
				doString(str, args);
			}

		}
		catch (Exception e) {
			mLuaContext.sendMsg(this + " " + e.getMessage());
			quit();
		}

	}

	private void newLuaRunnable(byte[] buf, Object...args) {
		try {
			int ok;
			L.setTop(0);
			ok = L.LloadBuffer(buf, "TimerTask");

			if (ok == 0) {
				L.getGlobal("debug");
				L.getField(-1, "traceback");
				L.remove(-2);
				L.insert(-2);
				int l=args.length;
				for (Object arg : args) {
					L.pushObjectValue(arg);
				}
				ok = L.pcall(l, 0, -2 - l);
				if (ok == 0) {
					return;
				}
			}
			throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
		}
		catch (Exception e) {
			mLuaContext.sendMsg(this + " " + e.getMessage());
			quit();
		}
	}

	private void doFile(String filePath, Object...args) throws LuaError {
		int ok;
		L.setTop(0);
		ok = L.LloadFile(filePath);

		if (ok == 0) {
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			int l=args.length;
			for (Object arg : args) {
				L.pushObjectValue(arg);
			}
			ok = L.pcall(l, 0, -2 - l);
			if (ok == 0) {
				return;
			}
		}
		throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
	}


	public void doAsset(String name, Object...args) throws LuaError, IOException {
		int ok;
		byte[] bytes = LuaUtil.readAsset(mLuaContext.getContext(), name);
		L.setTop(0);
		ok = L.LloadBuffer(bytes, name);

		if (ok == 0) {
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			int l=args.length;
			for (Object arg : args) {
				L.pushObjectValue(arg);
			}
			ok = L.pcall(l, 0, -2 - l);
			if (ok == 0) {
				return;
			}
		}
		throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
	}

	private void doString(String src, Object...args) throws LuaError {
		L.setTop(0);
		int ok = L.LloadString(src);

		if (ok == 0) {
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			int l=args.length;
			for (Object arg : args) {
				L.pushObjectValue(arg);
			}
			ok = L.pcall(l, 0, -2 - l);
			if (ok == 0) {

				return;
			}
		}
		throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
	}


	private void runFunc(String funcName, Object...args) {
		try {
			L.setTop(0);
			L.getGlobal(funcName);
			if (L.isFunction(-1)) {
				L.getGlobal("debug");
				L.getField(-1, "traceback");
				L.remove(-2);
				L.insert(-2);

				int l=args.length;
				for (Object arg : args) {
					L.pushObjectValue(arg);
				}

				int ok = L.pcall(l, 1, -2 - l);
				if (ok == 0) {
					return ;
				}
				throw new LuaError(errorReason(ok) + ": " + L.toString(-1));
			}
		}
		catch (LuaError e) {
			mLuaContext.sendMsg(funcName + " " + e.getMessage());
		}

	}

	private void setField(String key, Object value) {
		try {
			L.pushObjectValue(value);
			L.setGlobal(key);
		}
		catch (LuaError e) {
			mLuaContext.sendMsg(e.getMessage());
		}
	}

	private class ThreadHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data=msg.getData();
			switch (msg.what) {
				case 0 ->
						newLuaRunnable(data.getString("data"), (Object[]) data.getSerializable("args"));
				case 1 -> runFunc(data.getString("data"), (Object[]) data.getSerializable("args"));
				case 2 -> newLuaRunnable(data.getString("data"));
				case 3 -> runFunc(data.getString("data"));
				case 4 ->
						setField(data.getString("data"), ((Object[]) Objects.requireNonNull(data.getSerializable("args")))[0]);
			}
		}
	}

}
