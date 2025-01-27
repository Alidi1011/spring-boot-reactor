package com.aarteaga.springboot.reactor.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//Es un observable
		Flux<String> nombres = Flux.just("Roxana", "Amelia", "Diego", "Sebastian")
				//.doOnNext(elemento -> System.out.println(elemento));
				.doOnNext(System.out::println);

		
		nombres.subscribe();
		
				
		
		
	}

}
