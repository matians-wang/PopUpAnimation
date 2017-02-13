package plugins.common.com.popupwindows;

/**
 * Created by b916 on 17-2-7.
 */

public class ListItem {

    private int resId;
    private String resName;

    public int getResId() {
        return resId;
    }

    public ListItem setResId(int resId) {
        this.resId = resId;
        return this;
    }

    public String getResName() {
        return resName;
    }

    public ListItem setResName(String resName) {
        this.resName = resName;
        return this;
    }
}
