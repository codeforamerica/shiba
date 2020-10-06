package org.codeforamerica.shiba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class ShibaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShibaApplication.class, args);
	}

}
