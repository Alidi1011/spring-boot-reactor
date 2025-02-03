package com.aarteaga.springboot.reactor.app;

import com.aarteaga.springboot.reactor.app.models.Comentarios;
import com.aarteaga.springboot.reactor.app.models.Usuario;
import com.aarteaga.springboot.reactor.app.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

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
		//ejemploCollectList();
		//ejemploUsuarioComentariosZipWithForma2();
		ejemploContraPresion();
	}

	public void ejemploContraPresion() {
		//Recibe del observable todos los elementos enviados
		/*Flux.range(1,10)
				.log()
				.subscribe( i -> log.info(i.toString()));*/

		//Sobreescribiendo el Susbscriber para que reciba los elementos de acuerdo al limite configurado
		/*Flux.range(1,10)
				.log()
				.subscribe(new Subscriber<Integer>() {

					private Subscription s;
					private Integer limite = 5;
					private Integer consumido = 0;

					@Override
					public void onSubscribe(Subscription s) {
						this.s = s;
						s.request(limite);
					}

					@Override
					public void onNext(Integer integer) {
						log.info(integer.toString());
						consumido++;
						if(consumido == limite){
							consumido = 0;
							s.request(limite);
						}
					}

					@Override
					public void onError(Throwable t) {

					}

					@Override
					public void onComplete() {

					}
				});*/

		Flux.range(1,10)
				.log()
				.limitRate(2)
				.subscribe( i -> log.info(i.toString()));
	}

	public void ejemploIntervalDesdeCreate() throws InterruptedException {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador = 0;
				@Override
				public void run() {
					emitter.next(++contador);
					if(contador == 10){
						timer.cancel();
						emitter.complete();
					}
					if(contador == 5){
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}
				}
			}, 1000, 1000);
		})
		/*		.doOnNext(next -> log.info(next.toString()))
				.doOnComplete(() -> log.info("Hemos terminado"))
				.subscribe();*/
				.subscribe(next -> log.info(next.toString()),
						error -> log.error(error.getMessage()),
						() -> log.info("Hemos terminado"));
	}

	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				//.doOnTerminate(() -> latch.countDown()) //se ejecuta falla o no falle el flujo.
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if(i >= 5){
						return Flux.error(new InterruptedException("Solo, hasta 5!"));
					}
					return Flux.just(i);
				})
				.map(i -> "Hola " + i)
				.retry(2)
				.subscribe(s -> log.info(s.toString()), e -> log.error(e.getMessage()));
		latch.await();
	}

	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rango = Flux.range(1,12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(r -> log.info(r.toString()));

		rango.subscribe();
		//rango.blockLast(); //Similar al subscribe pero bloquea hasta que se emita el ultimo item.

		Thread.sleep(13000);
	}

	public void ejemploInterval(){
		Flux<Integer> rango = Flux.range(1,12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(3));

		rango.zipWith(retraso, (ra, re) -> ra)
				.doOnNext(i -> log.info(i.toString()))
				//.subscribe();
				.blockLast();
	}

	public void ejemploZipWithRangos() {
		Flux<Integer> rangos = Flux.range(0,4);

		Flux.just(1,2,3,4)
				.map(i -> (i*2))
				.zipWith(rangos, (uno, dos) -> String.format("Primer FLux: %d, Segundo FLux: %d", uno, dos))
				.subscribe(texto -> log.info(texto));
	}


	public void ejemploUsuarioComentariosZipWithForma2(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
		Mono<Comentarios> comentariosMono  = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal");
			comentarios.addComentario("Mañana voy a la playa!!!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});


		Mono<UsuarioComentarios> uc = usuarioMono
				.zipWith(comentariosMono)
				.map(tuple -> {
							Usuario u = tuple.getT1();
							Comentarios c = tuple.getT2();
							return new UsuarioComentarios(u, c);
				});

		uc.subscribe(usuariocomentario -> log.info(usuariocomentario.toString()));
	}

	public void ejemploUsuarioComentariosZipWith(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
		Mono<Comentarios> comentariosMono  = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal");
			comentarios.addComentario("Mañana voy a la playa!!!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});


		Mono<UsuarioComentarios> uc = usuarioMono.zipWith(comentariosMono, (u, c) -> new UsuarioComentarios(u, c));

		uc.subscribe(usuariocomentario -> log.info(usuariocomentario.toString()));
	}

	public void ejemploUsuarioComentariosFlatMap(){
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));
		Mono<Comentarios> comentariosMono  = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal");
			comentarios.addComentario("Mañana voy a la playa!!!");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});


		Mono<UsuarioComentarios> uc = usuarioMono.flatMap(u -> comentariosMono.map(c -> new UsuarioComentarios(u, c)));

		uc.subscribe(usuariocomentario -> log.info(usuariocomentario.toString()));
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
