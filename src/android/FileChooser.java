package com.megster.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;

    public static final String MIME = "mime";

    CallbackContext callback;

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            JSONObject filters = inputs.optJSONObject(0);
            chooseFile(filters, callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(JSONObject filter, CallbackContext callbackContext) {
        String uri_filter = filter.has(MIME) ? filter.optString(MIME) : "*/*";

        // type and title should be configurable

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(uri_filter);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooser = Intent.createChooser(intent, "Select File(s)");
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_FILE_REQUEST && callback != null) {

                if (resultCode == Activity.RESULT_OK) {
                    JSONArray filePaths = new JSONArray();
                    int itemCount = data.getClipData().getItemCount();
                    for (int i = 0; i < itemCount; i++) {
                        JSONObject file = new JSONObject();
                        file.put("path", data.getClipData().getItemAt(i).getUri().toString());
                        filePaths.put(file);
                    }

                    if (filePaths.length() > 0) {
                        Log.w(TAG, filePaths.toString());
                        callback.success(filePaths.toString());

                    } else {

                        callback.error("No file(s) selected");

                    }

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // keep this string the same as in iOS document picker plugin
                    // https://github.com/iampossible/Cordova-DocPicker
                    callback.error("User canceled.");
                } else {

                    callback.error(resultCode);
                }
            }
        } catch (JSONException je) {
            callback.error(resultCode);
        }
    }
}
