package android.app;

import android.content.Context;

import com.aslua.LuaDialog;

public class AlertDialogBuilder extends LuaDialog {

    public AlertDialogBuilder(Context context) {
        super(context);
    }

    public AlertDialogBuilder(Context context, int theme) {
        super(context, theme);
    }


}
