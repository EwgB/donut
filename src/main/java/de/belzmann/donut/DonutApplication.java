package de.belzmann.donut;

import de.belzmann.donut.model.Order;
import de.belzmann.donut.model.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.Timestamp;
import java.time.Instant;

@SpringBootApplication
public class DonutApplication {

	public static void main(String[] args) {
		SpringApplication.run(DonutApplication.class, args);
	}

	/**
	 * Pushes some test data to the database.
	 */
	@Bean
	CommandLineRunner initDatabase(OrderRepository repository) {

		long now = Instant.now().toEpochMilli();

		return args -> {
			repository.save(new Order(1, 5, new Timestamp(now - 60000)));
			repository.save(new Order(1042, 3, new Timestamp(now - 50000)));
			repository.save(new Order(5, 6, new Timestamp(now - 40000)));
			repository.save(new Order(42, 50, new Timestamp(now - 30000)));
			repository.save(new Order(1100, 3, new Timestamp(now - 20000)));
		};
	}
}
