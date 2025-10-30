package com.asyncapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MessagePayload {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("source")
    private String source;

    public MessagePayload() {
    }

    public MessagePayload(String id, String content, Long timestamp, String source) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.source = source;
    }

    public static MessagePayloadBuilder builder() {
        return new MessagePayloadBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessagePayload that = (MessagePayload) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(content, that.content) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, timestamp, source);
    }

    @Override
    public String toString() {
        return "MessagePayload{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                '}';
    }

    public static class MessagePayloadBuilder {
        private String id;
        private String content;
        private Long timestamp;
        private String source;

        MessagePayloadBuilder() {
        }

        public MessagePayloadBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MessagePayloadBuilder content(String content) {
            this.content = content;
            return this;
        }

        public MessagePayloadBuilder timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MessagePayloadBuilder source(String source) {
            this.source = source;
            return this;
        }

        public MessagePayload build() {
            return new MessagePayload(id, content, timestamp, source);
        }
    }
}
