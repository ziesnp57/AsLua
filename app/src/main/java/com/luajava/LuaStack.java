package com.luajava;

import java.util.HashMap;

public class LuaStack
{
	private final static HashMap<String,LuaState> luaStack= new HashMap<>();
	
	public static void put(String name,LuaState L){
		luaStack.put(name,L);
	}
	
	public static LuaState get(String name){
		return luaStack.get(name);
	}
	
	public static Object call(String name,String func,Object[] arg) throws LuaError{
		return new LuaFunction(get(name),func).call(arg);
	}
	
}
