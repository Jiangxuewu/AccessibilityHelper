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
     * 根据控件文字查找控件
     *
     * @param service AccessibilityService
     * @param txt     文字内容
     * @return 成功与失败
     */
    public static List<AccessibilityNodeInfo> getNodesForTxt(AccessibilityService service, String txt) {
        if (null == service || TextUtils.isEmpty(txt)) return null;
        AccessibilityNodeInfo eventSource = service.getRootInActiveWindow();
        if (null == eventSource) return null;
        return eventSource.findAccessibilityNodeInfosByText(txt);
    }

    /**
     * 根据控件ID查找控件， ListView中Item的控件id是重复的。
     *
     * @param service AccessibilityService
     * @param id      控件id
     * @return 成功与失败
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
     * @param id      控件id
     * @param txt     输入的文字内容
     * @return 成功与失败
     */
    public static boolean edittextInputById(AccessibilityService service, String id, String txt) {
        return edittextInputById(service, id, txt, 0);
    }

    /**
     * 给EditText控件输入文字
     *
     * @param service AccessibilityService
     * @param id      控件id
     * @param txt     输入的文字内容
     * @param index   相同id控件时， 第index个edittext输入文字； -1则表示给所有的edittext输入文字。
     * @return 成功与失败
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
     * 点击Button控件
     *
     * @param service AccessibilityService
     * @param id      控件ID
     * @return 成功与失败
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
     * 根据控件id，执行点击事件
     *
     * @param service AccessibilityService
     * @param id      控件id
     * @return 成功与失败
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
     * 给View的子View执行点击事件
     *
     * @param service AccessibilityService
     * @param id      View id
     * @return 成功与失败
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
     * 根据文字执行点击事件
     *
     * @param service AccessibilityService
     * @param txt     文字
     * @return 成功与失败
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
     * 执行系统事件
     *
     * @param service AccessibilityService
     * @param action  事件
     * @return 成功与失败
     */
    public static boolean systemEvent(AccessibilityService service, int action) {
        if (null == service) return false;
        return service.performGlobalAction(action);
    }

    /**
     * 根据View id执行任意事件
     *
     * @param service AccessibilityService
     * @param id      View id
     * @param action  事件
     * @return 成功与失败
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
     * 根据View的ID获取View的文字
     *
     * @param service AccessibilityService
     * @param id      View id
     * @return 文字结果
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
