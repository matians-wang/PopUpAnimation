package plugins.common.com.popupwindows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import plugins.common.com.popupwindows.view.PopViewBase;

/**
 * Created by b916 on 17-2-7.
 */

public class ListPopView extends PopViewBase {

    private LinearLayout ll;
    private List<ListItem> list = new ArrayList<>();
    private View item;

    public ListPopView(Context context) {
        super(context);
    }

    public ListPopView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListPopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setList(List<ListItem> list) {
        this.list.clear();
        this.list.addAll(list);
    }

    @Override
    protected View getContentView() {
        ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        for (int i =0; i< list.size(); i++){
            item = View.inflate(getContext(), R.layout.item, null);
            View rootView = item.findViewById(R.id.root_ll);
            //rootView.setTag(i);
            rootView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            ImageView logo = (ImageView)item.findViewById(R.id.content_list_item_iv);
            TextView text = (TextView)item.findViewById(R.id.content_list_item_tv);
            logo.setImageResource(list.get(i).getResId());
            text.setText(list.get(i).getResName());
            ll.addView(item);
        }
        return ll;
    }
}
