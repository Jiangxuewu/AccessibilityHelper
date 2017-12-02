package com.bb_sz.accessibilityutils;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataOutputStream;
import java.util.List;

/**
 * Created by Administrator on 2017/8/1.
 * <p>The Helper for dev Accessibility function</p>
 */

public class FZHelper {
    private static final String TAG = FZHelper.class.getSimpleName();
    private static final boolean debug = true;

    /**
     * Control text lookup control based on control text
     *
     * @param service AccessibilityService
     * @param txt     Text content
     * @return Success and failure
     */
    public static List<AccessibilityNodeInfo> getNodesForTxt(AccessibilityService service, String txt) {
        if (null == service || TextUtils.isEmpty(txt)) return null;
        AccessibilityNodeInfo eventSource = service.getRootInActiveWindow();
        if (null == eventSource) return null;
        return eventSource.findAccessibilityNodeInfosByText(txt);
    }

    /**
     * According to the View's ID lookup control, The View's ID of Item in ListView is repeated.
     *
     * @param service AccessibilityService
     * @param id      View's id
     * @return Success and failure
     */
    public static List<AccessibilityNodeInfo> getNodesForId(AccessibilityService service, String id) {
        if (null == service || TextUtils.isEmpty(id)) return null;
        AccessibilityNodeInfo eventSource = service.getRootInActiveWindow();
        if (null == eventSource) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return eventSource.findAccessibilityNodeInfosByViewId(id);
        }
        return null;
    }

    /**
     * @param service AccessibilityService
     * @param id      View's id
     * @param txt     Input text content
     * @return Success and failure
     */
    public static boolean edittextInputById(AccessibilityService service, String id, String txt) {
        return edittextInputById(service, id, txt, 0);
    }

    /**
     * Input text to the EditText control
     *
     * @param service AccessibilityService
     * @param id      View's id
     * @param txt     Input text content
     * @param index   When the same ID control is used, the index EditText enters the text; the -1 represents all the EditText input text.
     * @return Success and failure
     */
    public static boolean edittextInputById(AccessibilityService service, String id, String txt, int index) {
        if (debug) Log.d(TAG, "edittextInputById(), id = " + id + ", txt = " + txt);
        List<AccessibilityNodeInfo> nodes = getNodesForId(service, id);
        if (null == nodes || nodes.isEmpty()) return false;
        int size = nodes.size();
        boolean res = false;
        for (int i = 0; i < size; i++) {
            AccessibilityNodeInfo nodeInfo = nodes.get(i);
            if (null != nodeInfo && (i == index || index == -1)) {
                String cls = (String) nodeInfo.getClassName();
                if (EditText.class.getName().equals(cls)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(
                                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, txt);
                        res = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            ClipboardManager clipboard = (ClipboardManager) service.getBaseContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("", txt);
                            clipboard.setPrimaryClip(clip);
                            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                            res = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                        }
                    }
                }
            }
        }
        return res;
    }

    /**
     * Click Button View.
     *
     * @param service AccessibilityService
     * @param id      View's id
     * @return Success and failure
     */
    public static boolean buttonClick(AccessibilityService service, String id) {
        if (debug) Log.d(TAG, "buttonClick(), id = " + id);
        List<AccessibilityNodeInfo> nodes = getNodesForId(service, id);
        if (null == nodes || nodes.isEmpty()) return false;
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
            AccessibilityNodeInfo nodeInfo = nodes.get(i);
            if (null != nodeInfo) {
                String cls = (String) nodeInfo.getClassName();
                if (Button.class.getName().equals(cls)) {
                    boolean res = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.i(TAG, "buttonClick(), cls = " + cls + ", res = " + res);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * According to the control ID, execute the click event
     *
     * @param service AccessibilityService
     * @param id      View's id
     * @return Success and failure
     */
    public static boolean viewClickById(AccessibilityService service, String id) {
        if (debug) Log.d(TAG, "viewClickById(), id = " + id);
        List<AccessibilityNodeInfo> nodes = getNodesForId(service, id);
        if (null == nodes || nodes.isEmpty()) return false;
        int size = nodes.size();
        boolean res = false;
        if (debug) Log.d(TAG, "viewClickById(), size = " + size);
        for (int i = 0; i < size; i++) {
            AccessibilityNodeInfo nodeInfo = nodes.get(i);
            String cls = nodeInfo.getClassName().toString();
            if (debug) Log.d(TAG, "viewClickById(), cls = " + cls);
            int count = 0;
            while (!res && count++ < 5 && null != nodeInfo) {
                res = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                nodeInfo = nodeInfo.getParent();
            }
            if (res) return true;
        }
        return false;
    }

    /**
     * Execute the click event for the child View of View
     *
     * @param service AccessibilityService
     * @param id      View id
     * @return Success and failure
     */
    public static boolean viewChildClickForId(AccessibilityService service, String id) {
        if (debug) Log.d(TAG, "viewChildClickForId(), id = " + id);
        List<AccessibilityNodeInfo> nodes = getNodesForId(service, id);
        if (null == nodes || nodes.isEmpty()) return false;
        int size = nodes.size();
        boolean res = false;
        if (debug) Log.d(TAG, "viewChildClickForId(), size = " + size);
        for (int i = 0; i < size; i++) {
            AccessibilityNodeInfo nodeInfo = nodes.get(i);
            String cls = nodeInfo.getClassName().toString();
            int childCount = nodeInfo.getChildCount();
            if (debug)
                Log.d(TAG, "viewChildClickForId(), cls = " + cls + ", childCount = " + childCount);
            int count = 0;
            while (!res && count++ < 5 && null != nodeInfo) {
                res = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                nodeInfo = nodeInfo.getParent();
            }
            if (res) return true;
        }
        return false;
    }

    /**
     * Execute click events based on text
     *
     * @param service AccessibilityService
     * @param txt     Text
     * @return Success and failure
     */
    public static boolean viewClickByTxt(AccessibilityService service, String txt) {
        if (debug) Log.d(TAG, "viewClickFroTxt(), txt = " + txt);
        List<AccessibilityNodeInfo> nodes = getNodesForTxt(service, txt);
        if (null == nodes || nodes.isEmpty()) return false;
        int size = nodes.size();
        boolean res = false;
        for (int i = 0; i < size; i++) {
            AccessibilityNodeInfo nodeInfo = nodes.get(i);
            int count = 0;
            while (!res && count++ < 5 && null != nodeInfo) {
                res = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                nodeInfo = nodeInfo.getParent();
            }
            if (res) return true;
        }
        return false;
    }


    /**
     * Execution of system events
     *
     * @param service AccessibilityService
     * @param action  events
     * @return Success and failure
     */
    public static boolean systemEvent(AccessibilityService service, int action) {
        if (null == service) return false;
        return service.performGlobalAction(action);
    }

    /**
     * Execute arbitrary events according to View ID
     *
     * @param service AccessibilityService
     * @param id      View id
     * @param action  事件
     * @return Success and failure
     */
    public static boolean viewActionById(AccessibilityService service, String id, int action) {
        List<AccessibilityNodeInfo> nodes = getNodesForId(service, id);
        if (null == nodes || nodes.isEmpty()) return false;
        if (debug) Log.e(TAG, "viewActionForId(), size = " + nodes.size());
        for (AccessibilityNodeInfo item : nodes) {
            if (item.performAction(action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the text of View based on the ID of View
     *
     * @param service AccessibilityService
     * @param id      View id
     * @return Text results
     */
    public static String viewTextById(AccessibilityService service, String id) {
        List<AccessibilityNodeInfo> nodes = getNodesForId(service, id);
        if (null == nodes || nodes.isEmpty()) return null;
        if (debug) Log.e(TAG, "viewTextForId(), size = " + nodes.size());
        for (AccessibilityNodeInfo item : nodes) {
            if (!TextUtils.isEmpty(item.getText().toString())) {
                return item.getText().toString();
            }
        }
        return null;
    }

    public static void doSuExec(String[] cmds) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            for (String cmd : cmds) {
                if (debug) Log.d("cmd", "cmd = " + cmd);
                os.writeBytes(cmd + "\n");
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doExec(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
