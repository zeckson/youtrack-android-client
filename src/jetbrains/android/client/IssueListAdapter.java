package jetbrains.android.client;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

class IssueListAdapter extends SimpleAdapter {
    private static final String[] FROM = new String[]{"id", "summary"};
    private static final int[] TO = new int[]{R.id.issue_id, R.id.summary};
    private Resources resources;

    public IssueListAdapter(Context context, List<Map<String, Object>> data) {
        super(context, data, R.layout.issue_list_item, FROM, TO);
        resources = context.getResources();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView idView = (TextView) view.findViewById(R.id.issue_id);
        TextView summaryView = (TextView) view.findViewById(R.id.summary);
        TextView priorityView = (TextView) view.findViewById(R.id.priority);

        if (isResolved(position)) {
            idView.setPaintFlags(idView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            idView.setEnabled(false);
            summaryView.setEnabled(false);
        } else {
            idView.setPaintFlags(idView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            idView.setEnabled(true);
            summaryView.setEnabled(true);
        }

        String priority = getField(position, "priority");
        if ("0".equals(priority)) {
            priorityView.setText("M");
            priorityView.setTextColor(resources.getColor(R.color.priority_minor));
            priorityView.setBackgroundColor(resources.getColor(R.color.priority_background_minor));
        } else if ("2".equals(priority)) {
            priorityView.setText("M");
            priorityView.setTextColor(resources.getColor(R.color.priority_major));
            priorityView.setBackgroundColor(resources.getColor(R.color.priority_background_major));
        } else if ("3".equals(priority)) {
            priorityView.setText("C");
            priorityView.setTextColor(resources.getColor(R.color.priority_critical));
            priorityView.setBackgroundColor(resources.getColor(R.color.priority_background_critical));
        } else if ("4".equals(priority)) {
            priorityView.setText("S");
            priorityView.setTextColor(resources.getColor(R.color.priority_showStopper));
            priorityView.setBackgroundColor(resources.getColor(R.color.priority_background_showStopper));
        } else {
            priorityView.setText("N");
            priorityView.setTextColor(resources.getColor(R.color.priority_normal));
            priorityView.setBackgroundColor(resources.getColor(R.color.priority_background_normal));
        }
        return view;
    }

    public boolean isResolved(int position) {
        String state = getField(position, "state");
        // TODO: remove this hardcode when http://youtrack.jetbrains.net/issue/JT-5206 resolved
        return state != null &&
                (  "Can't Reproduce".equalsIgnoreCase(state)
                || "Duplicate".equalsIgnoreCase(state)
                || "Fixed".equalsIgnoreCase(state)
                || "Won't fix".equalsIgnoreCase(state)                
                || "Incomplete".equalsIgnoreCase(state)
                || "Obsolete".equalsIgnoreCase(state)
                || "Verified".equalsIgnoreCase(state));
    }

    public String getIssueId(int position) {
        return getField(position, "id");
    }

    @Override
    public Map<String, Object> getItem(int position) {
        return (Map<String, Object>) super.getItem(position);
    }

    public<T> T getField(int position, String fieldName) {
        Map<String, Object> item = getItem(position);
        Object field = null;
        if (item != null) {
            field = item.get(fieldName);
        }
        return (T) field;
    }
}
