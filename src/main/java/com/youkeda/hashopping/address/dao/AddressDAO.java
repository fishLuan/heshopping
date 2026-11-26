package com.youkeda.hashopping.address.dao;

import com.youkeda.hashopping.address.model.AddressResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 地址数据库接口，所有访问都限定所属用户。 */
@Mapper
public interface AddressDAO {
    /** 查询用户地址，默认地址优先。 */
    List<AddressResponse> list(@Param("userId") long userId);
    /** 查询本人指定地址。 */
    AddressResponse find(@Param("userId") long userId,@Param("id") long id);
    /** 清除本人原默认地址标记。 */
    int clearDefault(@Param("userId") long userId);
    /** 写入完整地址并回填主键。 */
    int insert(@Param("userId") long userId,@Param("address") AddressResponse address);
    /** 更新本人地址。 */
    int update(@Param("userId") long userId,@Param("address") AddressResponse address);
    /** 删除本人地址。 */
    int delete(@Param("userId") long userId,@Param("id") long id);
    /** 将本人指定地址设为默认。 */
    int setDefault(@Param("userId") long userId,@Param("id") long id);
}
