package timbo.jenkins.plugins.lark;

import org.apache.commons.lang.StringUtils;

public class MsgCardBuilder {

    //
    // RAW TEMPLATE
    // {
    //   "msg_type": "interactive",
    //   "card": {
    //     "config": {
    //       "enable_forward": false,
    //       "update_multi": false
    //     },
    //     "header": {
    //       "title": {
    //         "tag": "plain_text",
    //         "content": "[${project}] ${cause}"
    //       },
    //       "template": "${color}"
    //     },
    //     "elements": [
    //         {
    //           "tag": "markdown",
    //           "content": "${content}"
    //         },
    //         {
    //           "tag": "markdown",
    //           "content": "<at id=all></at>"
    //         }
    //     ]
    //   }
    // }
    //
    private static final String CARD_TEMPLATE = "{\"msg_type\":\"interactive\",\"card\":{\"config\":{\"enable_forward\":false,\"update_multi\":false},\"header\":{\"title\":{\"tag\":\"plain_text\",\"content\":\"[${project}] ${cause}\"},\"template\":\"${color}\"},\"elements\":[{\"tag\":\"markdown\",\"content\":\"${content}\"},{\"tag\":\"markdown\",\"content\":\"<at id=all></at>\"}]}}";

    private String project;
    private String content;
    private Type type;

    public MsgCardBuilder project(String project) {
        this.project = project;
        return this;
    }

    public MsgCardBuilder content(String content) {
        this.content = content;
        return this;
    }

    public MsgCardBuilder type(Type type) {
        this.type = type;
        return this;
    }

    public String toJsonString() {
        return CARD_TEMPLATE
                .replace("${project}", this.project)
                .replace("${cause}", this.type.cause)
                .replace("${color}", this.type.color)
                .replace("${content}", this.content);
    }

    public enum Type {
        START("START", "blue"),
        ABORTED("ABORTED", "orange"),
        SUCCESS("SUCCESS", "green"),
        FAILURE("FAILURE", "red");

        private final String cause;
        private final String color;

        Type(String cause, String color) {
            this.cause = cause;
            this.color = color;
        }
    }

}
