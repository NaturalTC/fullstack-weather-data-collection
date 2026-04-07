package com.github.fullstackweatherdatacollectionplatform.repository;

import com.github.fullstackweatherdatacollectionplatform.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository — marks this as a data access bean, Spring manages it
// extends JpaRepository<City, Long> — free CRUD: save, findAll, findById, delete, count, etc.
@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    // Spring generates: SELECT * FROM city WHERE name = ? LIMIT 1
    // returns Optional<City> — caller must handle the case where the city doesn't exist (instead of returning null)
    Optional<City> findByName(String name);
}
