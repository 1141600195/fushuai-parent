package com.manager.dao;

import com.kh.pojo.entity.RoleMenuInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleMenuDao extends JpaRepository<RoleMenuInfo, Long> {

    public void deleteByRoleId(Long roleId);
}
