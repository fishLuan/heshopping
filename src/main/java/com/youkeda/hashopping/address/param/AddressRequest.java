package com.youkeda.hashopping.address.param;

/** 新增或完整修改地址的输入，不接受客户端指定所属用户。 */
public class AddressRequest {
    public String recipientName;
    public String recipientPhone;
    public String province;
    public String city;
    public String district;
    public String detailAddress;
    public boolean isDefault;
}
