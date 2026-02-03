package com.pjs.roomreservation.repository;

import com.pjs.roomreservation.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndActiveTrue(String email);

    Optional<User> findByIdAndActiveTrue(Long id);
    boolean existsByEmail(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.isAdmin = true where u.email = :email")
    int promoteAdminByEmail(@Param("email") String email);
}
