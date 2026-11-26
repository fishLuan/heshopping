package com.youkeda.hashopping.address.api;

import com.youkeda.hashopping.address.service.AddressService;
import com.youkeda.hashopping.address.model.AddressResponse;
import com.youkeda.hashopping.address.param.AddressRequest;
import com.youkeda.hashopping.security.context.CurrentUser;
import com.youkeda.hashopping.common.model.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** 本人收货地址管理接口，身份只从服务端认证上下文获取。 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressService addresses;
    private final CurrentUser current;
    /** 注入地址业务服务和当前用户入口。 */
    public AddressController(AddressService addresses,CurrentUser current) {
        this.addresses = addresses;
        this.current = current;
    }
    /** 查询本人全部地址。 */
    @GetMapping
    public Result<List<AddressResponse>> list() { return Result.success(addresses.list(current.requireUserId())); }
    /** 查询本人单个地址。 */
    @GetMapping("/{id}")
    public Result<AddressResponse> get(@PathVariable long id) { return Result.success(addresses.get(current.requireUserId(),id)); }
    /** 新增本人地址。 */
    @PostMapping
    public Result<AddressResponse> create(@RequestBody AddressRequest request) {
        return Result.success(addresses.save(current.requireUserId(),null,request));
    }
    /** 完整修改本人地址。 */
    @PutMapping("/{id}")
    public Result<AddressResponse> update(@PathVariable long id,@RequestBody AddressRequest request) {
        return Result.success(addresses.save(current.requireUserId(),id,request));
    }
    /** 删除本人地址。 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        addresses.delete(current.requireUserId(),id);
        return Result.success();
    }
    /** 将本人地址设为默认。 */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable long id) {
        addresses.setDefault(current.requireUserId(),id);
        return Result.success();
    }
}
