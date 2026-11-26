package com.youkeda.hashopping.address.service;

import com.youkeda.hashopping.address.dao.AddressDAO;
import com.youkeda.hashopping.address.model.AddressResponse;
import com.youkeda.hashopping.address.param.AddressRequest;
import com.youkeda.hashopping.user.service.BuyerService;
import com.youkeda.hashopping.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** 地址归属校验与默认地址事务，同用户写操作通过用户行锁串行化。 */
@Service
public class AddressService {
    private final AddressDAO addresses;
    private final BuyerService buyers;
    /** 注入地址持久层和用户事务锁。 */
    public AddressService(AddressDAO addresses, BuyerService buyers) {
        this.addresses = addresses;
        this.buyers = buyers;
    }
    /** 查询本人地址。 */
    public List<AddressResponse> list(long userId) { return addresses.list(userId); }
    /** 获取本人地址，其他用户地址同样返回不存在。 */
    public AddressResponse get(long userId,long id) {
        AddressResponse address = addresses.find(userId,id);
        if (address == null) throw new BusinessException(HttpStatus.NOT_FOUND,"ADDRESS_NOT_FOUND","地址不存在");
        return address;
    }
    /** 保存完整地址，首条地址自动设为默认；显式取消默认允许暂时没有默认地址。 */
    @Transactional
    public AddressResponse save(long userId,Long id,AddressRequest request) {
        buyers.lockActive(userId);
        if (id != null) get(userId,id);
        AddressResponse address = validate(request);
        address.id = id;
        if (id == null && addresses.list(userId).isEmpty()) address.isDefault = true;
        if (address.isDefault) addresses.clearDefault(userId);
        if (id == null) addresses.insert(userId,address);
        else addresses.update(userId,address);
        return get(userId,address.id);
    }
    /** 删除地址；删除默认地址后，自动选择剩余地址中的第一条。 */
    @Transactional
    public void delete(long userId,long id) {
        buyers.lockActive(userId);
        AddressResponse old = get(userId,id);
        addresses.delete(userId,id);
        List<AddressResponse> remaining = addresses.list(userId);
        if (old.isDefault && !remaining.isEmpty()) addresses.setDefault(userId,remaining.get(0).id);
    }
    /** 原子切换默认地址。 */
    @Transactional
    public void setDefault(long userId,long id) {
        buyers.lockActive(userId);
        get(userId,id);
        addresses.clearDefault(userId);
        addresses.setDefault(userId,id);
    }
    /** 校验长度和联系电话，返回服务端清理后的字段。 */
    private AddressResponse validate(AddressRequest request) {
        if (request == null) throw new BusinessException(HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT","地址不能为空");
        AddressResponse result = new AddressResponse();
        result.recipientName = text(request.recipientName,50,"收货人");
        result.recipientPhone = text(request.recipientPhone,32,"联系电话");
        if (!result.recipientPhone.matches("[+0-9() -]{5,32}"))
            throw new BusinessException(HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT","联系电话格式不正确");
        result.province = text(request.province,50,"省");
        result.city = text(request.city,50,"市");
        result.district = text(request.district,50,"区县");
        result.detailAddress = text(request.detailAddress,255,"详细地址");
        result.isDefault = request.isDefault;
        return result;
    }
    /** 校验并清理必填文本，保持数据库字段长度约束。 */
    private String text(String value,int max,String label) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > max)
            throw new BusinessException(HttpStatus.BAD_REQUEST,"INVALID_ARGUMENT",label+"不能为空且不能超过"+max+"个字符");
        return value.trim();
    }
}
