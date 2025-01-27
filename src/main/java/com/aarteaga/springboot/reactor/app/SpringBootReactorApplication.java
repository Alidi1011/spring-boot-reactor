package com.aarteaga.springboot.reactor.app;

import com.aarteaga.springboot.reactor.app.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner{
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}
	public void run(String... args) throws Exception {
		//ejemploIterable();
		//ejemploFlatMap();
		//ejemploToString();
		ejemploCollectList();

	}

	public void ejemploCollectList() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Roxana", "Guzmán"));
		usuariosList.add(new Usuario("Amelia", "Fulano"));
		usuariosList.add(new Usuario("Alexa", "Sultano"));
		usuariosList.add(new Usuario("Diego", "Almaro"));
		usuariosList.add(new Usuario("Sebastian","Llosa"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.collectList()
				.subscribe(lista -> {
					lista.forEach(item -> log.info(item.toString()));
				});
	}

	public void ejemploToString() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Roxana", "Guzmán"));
		usuariosList.add(new Usuario("Amelia", "Fulano"));
		usuariosList.add(new Usuario("Alexa", "Sultano"));
		usuariosList.add(new Usuario("Diego", "Almaro"));
		usuariosList.add(new Usuario("Sebastian","Llosa"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" "). concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if(nombre.contains("bruce".toUpperCase())){
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(nombre -> {
					return nombre.toLowerCase();
				})
				.subscribe(u -> log.info(u.toString()));
	}

	public void ejemploFlatMap() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Roxana Guzmán");
		usuariosList.add("Amelia Fulano");
		usuariosList.add("Alexa Sultano");
		usuariosList.add("Diego Almaro");
		usuariosList.add("Sebastian Llosa");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[1], nombre.split(" ")[0].toUpperCase()))
				.flatMap(usuario -> {
					if(usuario.getNombre().equalsIgnoreCase("bruce")){
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
				.subscribe(u -> log.info(u.toString()));
	}

	public void ejemploIterable() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Roxana Guzmán");
		usuariosList.add("Amelia Fulano");
		usuariosList.add("Alexa Sultano");
		usuariosList.add("Diego Almaro");
		usuariosList.add("Sebastian Llosa");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux<String> nombres = Flux.fromIterable(usuariosList);


		//Flux<String> nombres = Flux.just("Roxana Guzmán", "Amelia Fulano", "Alexa Sultano", "Diego Almaro", "Sebastian Llosa", "Bruce Lee", "Bruce Willis");

		//Es un observable
		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[1], nombre.split(" ")[0].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce"))
				.doOnNext(usuario -> {
					if(usuario == null){
						throw new RuntimeException("Nombre no pueden ser vacíos");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				})
				//.doOnNext(System.out::println);
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		
		//nombres.subscribe(log::info);
		usuarios.subscribe(e -> log.info(e.toString()),
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecución del observable con éxito!");
					}
				});
	}

}
