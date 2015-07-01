package jetbrains.android.client;

import android.app.TabActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.*;
import jetbrains.android.data.RequestFailedException;

import java.util.*;

public class IssueActivity extends TabActivity {
    private static final Set<String> HIDDEN_PROPS = new HashSet<String>();

    static {
        HIDDEN_PROPS.add("id");
        HIDDEN_PROPS.add("summary");
        HIDDEN_PROPS.add("description");
        HIDDEN_PROPS.add("numberInProject");
        HIDDEN_PROPS.add("projectShortName");
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Uri issueUri = getIntent().getData();

        try {
            final Map<String,String> issue = YouTrackActivity.dao.getIssue(issueUri.toString());

            TabHost th = getTabHost();
            setTitle(issue.get("id") + " " + issue.get("summary"));
            LayoutInflater.from(this).inflate(R.layout.issue_properties, th.getTabContentView(), true);

            th.addTab(th.newTabSpec("Summary")
                    .setIndicator("Summary")                    
                    .setContent(R.id.issue_description));
            TextView summary = (TextView) th.findViewById(R.id.summary);
            summary.setText(issue.get("summary"));

            String description = issue.get("description");
            if (description != null && description.length() > 0) {
                TextView descriptionView = (TextView) th.findViewById(R.id.description);
                descriptionView.setText(description);
            }


            th.addTab(th.newTabSpec("Properties")
                    .setIndicator("Properties")
                    .setContent(R.id.issue_properties));
            ListView propertiesList = (ListView) th.findViewById(R.id.issue_properties);
            List<String> issueData = new ArrayList<String>();
            for (String fieldName: issue.keySet()) {
                String value = issue.get(fieldName);
                if (!HIDDEN_PROPS.contains(fieldName) && value != null && value.length() > 0) {
                    issueData.add(fieldName + ": " + value);
                }
            }
            propertiesList.setAdapter(new ArrayAdapter<String>(IssueActivity.this, R.layout.issue_field, issueData));

            th.addTab(th.newTabSpec("Comments")
                    .setIndicator("Comments")
                    .setContent(R.id.issue_comments));

        } catch (RequestFailedException e) {
//            TextView textView = new TextView(IssueActivity.this);
//            textView.setText("Failed to load issue: " + e.getMessage());
//            setContentView(textView);
        }

    }
}
