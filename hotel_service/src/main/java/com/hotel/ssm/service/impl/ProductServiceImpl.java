package com.hotel.ssm.service.impl;

import com.hotel.ssm.dao.IProductDao;
import com.hotel.ssm.domain.Product;
import com.hotel.ssm.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements IProductService {

    @Autowired
    private IProductDao productDao;

    @Override
    public List<Product> findAll() throws Exception {
        return productDao.findAll();
    }

    @Override
    public void save(Product product) throws Exception {
        System.out.println(product);
        productDao.save(product);
    }
}
