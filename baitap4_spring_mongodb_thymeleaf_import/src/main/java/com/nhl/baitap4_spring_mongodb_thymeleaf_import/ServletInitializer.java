package com.nhl.baitap4_spring_mongodb_thymeleaf_import;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Baitap4SpringMongodbThymeleafImportApplication.class);
	}

}
