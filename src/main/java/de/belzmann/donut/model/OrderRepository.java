package de.belzmann.donut.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM Order o ORDER BY isPriority DESC, orderTime ASC")
    Stream<Order> findAllOrdersByPriority();

    boolean existsByClientId(int clientId);

    void deleteByClientId(int clientId);
}
