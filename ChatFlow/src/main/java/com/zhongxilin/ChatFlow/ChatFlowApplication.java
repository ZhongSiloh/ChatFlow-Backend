package com.zhongxilin.ChatFlow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class ChatFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatFlowApplication.class, args);
	}

}
