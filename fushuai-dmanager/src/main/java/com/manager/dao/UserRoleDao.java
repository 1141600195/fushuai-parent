package com.manager.dao;

import com.kh.pojo.entity.UserRoleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRoleDao extends JpaRepository<UserRoleInfo, Long> {

    @Query(value = "select * from base_user_role where userId =?1", nativeQuery = true)
    public UserRoleInfo selectUserByUserId(Long userid);
}
