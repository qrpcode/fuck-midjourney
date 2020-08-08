package com.hotel.ssm.service;

import com.hotel.ssm.domain.Orders;

import java.util.List;

public interface IOrderService {

    List<Orders> findAll(int page, int size) throws Exception;

    Orders findById(String ordersId) throws Exception;
}
