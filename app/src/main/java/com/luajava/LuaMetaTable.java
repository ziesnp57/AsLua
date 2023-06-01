package com.luajava;

public interface LuaMetaTable
{
	Object __call(Object...arg) throws LuaError;
	
	Object __index(String key);
	
	void __newIndex(String key,Object value);
}
