package me.lecoding.grpclearning.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,String> {
    User getOneByUsername(String username);
}
