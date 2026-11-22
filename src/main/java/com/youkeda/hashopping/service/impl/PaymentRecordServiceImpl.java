package com.youkeda.hashopping.service.impl;

import com.youkeda.hashopping.dao.PaymentRecordDAO;
import com.youkeda.hashopping.dataobject.PaymentRecordDO;
import com.youkeda.hashopping.model.PaymentRecord;
import com.youkeda.hashopping.param.PaymentRecordQueryParam;
import com.youkeda.hashopping.service.PaymentRecordService;
import com.youkeda.hashopping.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentRecordServiceImpl implements PaymentRecordService {
    @Autowired
    private PaymentRecordDAO paymentRecordDAO;

    @Override
    public PaymentRecord save(PaymentRecord paymentRecord) {
        if (paymentRecord == null) {
            return null;
        }
        if (StringUtils.isEmpty(paymentRecord.getId())) {
            PaymentRecordDO paymentRecordDO = new PaymentRecordDO(paymentRecord);
            paymentRecordDO.setId(UUIDUtils.getUUID());
            int insertSize = paymentRecordDAO.insert(paymentRecordDO);
            if (insertSize < 1) {
                return null;
            }
            paymentRecord.setId(paymentRecordDO.getId());
            return paymentRecord;
        } else {
            PaymentRecordDO paymentRecordDO = new PaymentRecordDO(paymentRecord);
            int updateSize = paymentRecordDAO.update(paymentRecordDO);
            if (updateSize < 1) {
                return null;
            }
            return paymentRecord;
        }
    }

    @Override
    public List<PaymentRecord> query(PaymentRecordQueryParam queryParam) {
        List<PaymentRecord> paymentRecords = new ArrayList<>();
        List<PaymentRecordDO> paymentRecordDOS = paymentRecordDAO.select(queryParam);
        if (CollectionUtils.isEmpty(paymentRecordDOS)) {
            return paymentRecords;
        }
        paymentRecordDOS.forEach(paymentRecordDO -> {
            PaymentRecord paymentRecord = paymentRecordDO.convertToModel();
            paymentRecords.add(paymentRecord);
        });
        return paymentRecords;
    }

    @Override
    public PaymentRecord updateStatus(PaymentRecord paymentRecord) {
        PaymentRecordDO paymentRecordDO = new PaymentRecordDO(paymentRecord);
        int updateSize = paymentRecordDAO.update(paymentRecordDO);
        if (updateSize < 1) {
            return null;
        }
        return paymentRecord;
    }
}
