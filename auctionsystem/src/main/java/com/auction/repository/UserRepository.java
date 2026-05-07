package com.auction.repository;

import com.auction.model.user.NormalUser;
import java.util.Optional;

/**
 * Interface định nghĩa các hành động với dữ liệu người dùng.
 */
public interface UserRepository {
    void save(NormalUser user);  //lưu dữ liệu vào postgreSQL
    Optional<NormalUser> findByUsername(String username);   
    boolean existsByUsername(String username);
}