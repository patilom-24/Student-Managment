package com.studentmanagement.repository;

import com.studentmanagement.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {

    // ── Derived: find ───────────────────────────────────────────────────────
    Optional<Address> findByCityAndState(String city, String state);

    Optional<Address> findByStreetAndCityAndPostalCode(String street, String city, String postalCode);

    List<Address> findByCountryIgnoreCase(String country);

    List<Address> findByCityContainingIgnoreCase(String cityPart);

    List<Address> findByPostalCodeStartingWith(String prefix);

    List<Address> findByStateOrderByCityAsc(String state);



    // ── Derived: count & exists ─────────────────────────────────────────────
    long countByCountry(String country);

    boolean existsByCityAndPostalCode(String city, String postalCode);

    boolean existsByStreetAndCity(String street, String city);

    // ── Derived: pagination ───────────────────────────────────────────────────
    Page<Address> findByState(String state, Pageable pageable);

    // ── JPQL ──────────────────────────────────────────────────────────────────
    @Query("SELECT a FROM Address a WHERE a.postalCode = :code")
    Optional<Address> findByPostalCode(@Param("code") String postalCode);

    @Query("SELECT a FROM Address a WHERE a.city = :city AND a.country = :country ORDER BY a.street ASC")
    List<Address> findByCityAndCountryOrderByStreet(
            @Param("city") String city,
            @Param("country") String country);

    @Query("SELECT a FROM Address a WHERE a.country = :country AND a.state IN :states")
    List<Address> findByCountryAndStates(
            @Param("country") String country,
            @Param("states") List<String> states);

    // ── Native SQL ────────────────────────────────────────────────────────────
    @Query(value = "SELECT * FROM addresses WHERE state = :state", nativeQuery = true)
    List<Address> findByStateNative(@Param("state") String state);

    @Query(value = "SELECT * FROM addresses WHERE city LIKE CONCAT('%', :keyword, '%') OR street LIKE CONCAT('%', :keyword, '%')", nativeQuery = true)
    List<Address> searchByCityOrStreetNative(@Param("keyword") String keyword);

    // ── Modifying ─────────────────────────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.country = :country WHERE a.id = :id")
    int updateCountryById(@Param("id") Long id, @Param("country") String country);

    @Modifying
    @Transactional
    @Query("DELETE FROM Address a WHERE a.city = :city AND a.state = :state")
    int deleteByCityAndState(@Param("city") String city, @Param("state") String state);
}
