package com.sso.dao;

import com.kh.pojo.entity.MenuInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 获得目录
 */
public interface MenuDao extends JpaRepository<MenuInfo, Long> {

    @Query(value = "select bm.* from base_role_menu brm inner join base_menu bm on brm.menuId=bm.id where brm.roleId=?1 and bm.parentId=?2  ", nativeQuery = true)
    public List<MenuInfo> getMenuInfo(Long roleId, Long parentId);

    public List<MenuInfo>  findByParentId(Long mid);

    @Query(value = "select bm.* from base_role_menu brm INNER JOIN base_menu bm on brm.menuId=bm.id where brm.roleId=?1" ,nativeQuery = true)
    public List<MenuInfo> getRoleMenuInfo(Long roleId);



}
