package com.youkeda.hashopping.user.dao;

import com.youkeda.hashopping.user.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
/**
 * 用户登录、身份认证与用户管理：UserDAO。
 */
public interface UserDAO {
    /** 锁定有效买家，串行化同一买家的并发下单。 */
    Long lockActiveBuyer(@Param("id") long id);

    int batchAdd(@Param("list") List<UserDO> userDOs);

    List<UserDO> findByIds(@Param("ids") List<Long> ids);

    int add(UserDO userDO);

    int update(UserDO userDO);

    int recordLogin(@Param("id") long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    int delete(@Param("id") long id);

    List<UserDO> findAll();

    UserDO findByUserName(@Param("userName") String name);

    List<UserDO> query(@Param("keyWord") String keyWord);

    List<UserDO> search(@Param("keyWord") String keyWord, @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);
}
