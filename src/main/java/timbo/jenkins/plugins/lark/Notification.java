package timbo.jenkins.plugins.lark;

import hudson.model.AbstractBuild;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Notification {

    private LarkNotifier larkNotifier;

    public Notification(LarkNotifier larkNotifier) {
        this.larkNotifier = larkNotifier;
    }

    public void started(AbstractBuild build) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(this.larkNotifier.getWebhook()))
                    .POST(HttpRequest.BodyPublishers.ofString(buildMsgBody(build)))
                    .header("Content-Type", "application/json")
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
        }
    }

    private String buildMsgBody(AbstractBuild build) {
        String template = "{\"msg_type\":\"post\",\"content\":{\"post\":{\"zh_cn\":{%content%}}}}";

        JSONArray contentArray = new JSONArray();
        JSONArray contentDetailArray = new JSONArray();
        JSONObject o = new JSONObject();
        o.put("tag", "text");
        o.put("text", "build start");
        contentDetailArray.add(o);
        contentArray.add(contentDetailArray);

        JSONObject body = new JSONObject();
        body.put("title", build.getProject().getFullDisplayName());
        body.put("content", contentArray);

        return template.replace("{%content%}", body.toString());
    }


}
