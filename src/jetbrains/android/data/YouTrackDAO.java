package jetbrains.android.data;

import android.net.Uri;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

public class YouTrackDAO {
    private static final String LOGIN_PATH = "%s/rest/user/login?login=%s&password=%s";
    private static final String ISSUES_PATH = "%s/rest/project/issues/%s?filter=%s&after=%d&max=%d";
    private static final String ISSUE_PATH = "%s/rest/issue/%s";

    private HttpClient httpClient;
    private String baseUri;

    private Cache<List<Map<String, Object>>> cache = new CaseInsesitiveCache<List<Map<String, Object>>>(5 * 60 * 1000, 10);

    public YouTrackDAO() {
        httpClient = new DefaultHttpClient();
    }

    public void login(String baseUri, String login, String password) throws RequestFailedException {
        this.baseUri = baseUri;
        try {
            String uri = String.format(LOGIN_PATH, baseUri, quote(login), quote(password));
            HttpPost post = new HttpPost(uri);
            HttpResponse response = httpClient.execute(post);
            assertStatus(response);
            post.abort();
        } catch (IOException e) {
            throw new RequestFailedException(e);
        }
    }

    public Uri getIssueUri(String issueId) {
        return Uri.parse(String.format(ISSUE_PATH, baseUri, quote(issueId)));
    }

    public Map<String, String> getIssue(String issueUri) throws RequestFailedException {
        try {
            HttpGet get = new HttpGet(issueUri);
            HttpResponse response = httpClient.execute(get);
            assertStatus(response);
            final Map<String, String> issue = new LinkedHashMap<String, String>();

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(response.getEntity().getContent(), new DefaultHandler2() {
                private String fieldName;
                private StringBuilder multiValue;
                private StringBuilder value;
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if ("issue".equalsIgnoreCase(localName)) {
                        String issueId = attributes.getValue("id");
                        issue.put("id", issueId);
                    }
                    if ("field".equalsIgnoreCase(localName)) {
                        fieldName = attributes.getValue("name");
                        multiValue = new StringBuilder();
                    }
                    if ("value".equalsIgnoreCase(localName)) {
                        value = new StringBuilder();
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if ("field".equalsIgnoreCase(localName)) {
                        issue.put(fieldName, multiValue.toString());
                        fieldName = null;
                    }
                    if ("value".equalsIgnoreCase(localName)) {
                        if (multiValue.length() > 0) {
                            multiValue.append(", ");
                        }
                        multiValue.append(value);
                        value = null;
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    value.append(ch, start, length);
                }
            });
            return issue;
        } catch (Exception e) {
            throw new RequestFailedException(e);
        }
    }

    public List<Map<String, Object>> getIssues(String project, String query, int position, int max, boolean reload) throws RequestFailedException {
        String key = (project != null ? project : "") + query + "[" + position + ":" + max + "]";
        List<Map<String, Object>> issues = !reload ? cache.get(key) : null;
        if (issues == null) {
            issues = loadIssues(project, query, position, max);
            cache.put(key, issues);
        }
        return issues;
    }

    private List<Map<String, Object>> loadIssues(String project, String query, int position, int max) throws RequestFailedException {
        final ArrayList<Map<String, Object>> issues = new ArrayList<Map<String, Object>>();
        try {
            String uri = String.format(ISSUES_PATH, baseUri, quote(project), quote(query), position, max);
            Log.i(getClass().getSimpleName(), "request: " + uri);
            HttpGet get = new HttpGet(uri);
            HttpResponse response = httpClient.execute(get);
            assertStatus(response);

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(response.getEntity().getContent(), new DefaultHandler2() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if ("issue".equalsIgnoreCase(localName)) {
                        Map<String, Object> issue = new HashMap<String, Object>();
                        for (int i = 0; i < attributes.getLength(); i++) {
                            String name = attributes.getLocalName(i);
                            String value = attributes.getValue(i);
                            issue.put(name, value);
                        }
                        issues.add(issue);
                    }
                }
            });

            for (Map<String, Object> issue : issues) {
                issue.put("title", issue.get("id") + " " + issue.get("summary"));
            }
        } catch (Exception e) {
            throw new RequestFailedException(e);
        }
        return issues;
    }

    public void destroy() {
        httpClient.getConnectionManager().shutdown();
    }

    private void assertStatus(HttpResponse response) throws RequestFailedException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 300) {
            throw new RequestFailedException(status + ": " + response.getStatusLine().getReasonPhrase());
        }
    }

    private String quote(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
