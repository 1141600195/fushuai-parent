package com.manager.dao;

import com.kh.pojo.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 *
 */
public interface UserDao extends JpaRepository<UserInfo, Long> {
    @Query(value = "select * from base_user where loginName=?1", nativeQuery = true)
    public UserInfo findByLoginName(String loginName);


    @Query(value = "select bu.* from base_user_role bur inner  join base_user bu on bur.userId=bu.id where bur.roleId=?1", nativeQuery = true)
    public List<UserInfo> forUserInfoByUserId(Long roleId);

    public UserInfo findByEmail(String email);

    @Query(value = "select * from base_user where id=?1", nativeQuery = true)
    public UserInfo selectByUserinfoById(Long id);

}
