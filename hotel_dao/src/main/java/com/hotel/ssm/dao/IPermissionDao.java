package com.hotel.ssm.dao;

import com.hotel.ssm.domain.Permission;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IPermissionDao {

    @Select("select * from permission where id in (select permissionId from role_permission where roleId = #{id})")
    public List<Permission> findPermissionByRoleId(String id) throws Exception;

    @Select("select * from permission")
    public List<Permission> findAll() throws Exception;

    @Insert("insert into permission(permissionName, url) values (#{permissionName}, #{url})")
    void save(Permission permission);
}













