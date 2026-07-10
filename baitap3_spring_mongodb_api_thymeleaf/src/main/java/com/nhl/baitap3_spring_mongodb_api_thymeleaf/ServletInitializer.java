package com.nhl.baitap3_spring_mongodb_api_thymeleaf;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Baitap3SpringMongodbApiThymeleafApplication.class);
	}

}
