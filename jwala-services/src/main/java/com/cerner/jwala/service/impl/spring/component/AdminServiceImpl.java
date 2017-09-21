package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.dao.PersistenceHelper;
import com.cerner.jwala.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Jedd Cuison on 9/20/2017
 */
@Service
public class AdminServiceImpl implements AdminService {

    private final PersistenceHelper persistenceHelper;

    @Autowired
    public AdminServiceImpl(final PersistenceHelper persistenceHelper) {
        this.persistenceHelper = persistenceHelper;
    }

    @Override
    public void clearCache() {
        persistenceHelper.clearCache();
    }
}
