package timbo.jenkins.plugins.lark;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

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
        if (this.larkNotifier.isNotifyStart()) {
            send(this.larkNotifier.getWebhook(), buildMsgBody(build, null));
        }
    }

    public void completed(AbstractBuild build) {
        Result result = build.getResult();
        if ((result == Result.ABORTED && this.larkNotifier.isNotifyAborted()) ||
                (result == Result.FAILURE && this.larkNotifier.isNotifyFailure()) ||
                (result == Result.SUCCESS && this.larkNotifier.isNotifySuccess())) {
            send(this.larkNotifier.getWebhook(), buildMsgBody(build, build.getResult()));
        }
    }

    private String buildMsgBody(AbstractBuild build, Result result) {
        MsgCardBuilder cardBuilder = new MsgCardBuilder().project(build.getProject().getFullDisplayName());
        StringBuilder content = new StringBuilder();

        // START
        if (result == null) {
            content.append("**Estimated duration**: ")
                    .append(readableEstimatedDuration(build.getProject().getEstimatedDuration()))
                    .append("\\n<a href=")
                    .append(buildLink(build.getUrl()))
                    .append(">build link</a>");
            cardBuilder.type(MsgCardBuilder.Type.START);
        }

        // ABORTED
        if (result == Result.ABORTED) {
            content.append("**Build aborted**");
            cardBuilder.type(MsgCardBuilder.Type.ABORTED);
        }

        // FAILED
        if (result == Result.FAILURE) {
            content.append("**Build failed**, click link for detail")
                    .append("\\n<a href=")
                    .append(buildLink(build.getUrl()))
                    .append(">build link</a>");
            cardBuilder.type(MsgCardBuilder.Type.FAILURE);
        }

        // SUCCESS
        if (result == Result.SUCCESS) {
            content.append("**Using time**: ")
                    .append(build.getTimestampString())
                    .append("\\n<a href=")
                    .append(buildLink(build.getUrl()))
                    .append(">build link</a>");
            cardBuilder.type(MsgCardBuilder.Type.SUCCESS);
        }

        return cardBuilder.content(content.toString()).toJsonString();
    }

    private void send(String webhook, String body) {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(webhook))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
        }
    }

    private String readableEstimatedDuration(long estimatedDuration) {
        if (estimatedDuration > 0) {
            long l = estimatedDuration / (1000 * 60);
            return l + " min";
        }
        return "no estimated duration";
    }

    private String buildLink(String buildUrl) {
        // null check
        Jenkins instance = Jenkins.getInstanceOrNull();
        if (instance == null) {
            return "";
        }
        String rootUrl = instance.getRootUrl();
        if (StringUtils.isBlank(rootUrl)) {
            return "";
        }

        StringBuilder linkBuilder = new StringBuilder(rootUrl);
        linkBuilder.append(rootUrl);
        if (!rootUrl.endsWith("/")) {
            linkBuilder.append("/");
        }
        linkBuilder.append(buildUrl);
        if (!buildUrl.endsWith("/")) {
            linkBuilder.append("/");
        }
        linkBuilder.append("console");
        return linkBuilder.toString();
    }
}
