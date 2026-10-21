package com.tradepost.procure.order.service;

public interface DispatchChannel {

    void dispatch(OrderApproved event);
}
