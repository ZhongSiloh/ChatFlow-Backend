package com.zhongxilin.ChatFlow.pojo;

public class HelloMessage {

    private String name;
    private Long userId;

    public HelloMessage() {
    }
    public HelloMessage(String name) {
        this.name = name;
    }

    public HelloMessage(String name, Long userId) {
        this.name = name;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
