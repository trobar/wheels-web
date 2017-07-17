package com.doit.wheels.services.impl;

import com.doit.wheels.dao.entities.Order;
import com.doit.wheels.dao.entities.User;
import com.doit.wheels.dao.repositories.GenericRepository;
import com.doit.wheels.services.OrderService;
import com.doit.wheels.services.UserService;
import com.doit.wheels.utils.AccessLevelType;
import com.doit.wheels.utils.StatusTypeEnum;
import com.doit.wheels.utils.exceptions.NoPermissionsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends GenericServiceImpl<Order> implements OrderService{

    private final UserService userService;

    @Autowired
    public OrderServiceImpl(GenericRepository<Order> genericRepository, UserService userService) {
        super(genericRepository);
        this.userService = userService;
    }

    @Override
    public Order save(Order order) {
        if(order.getId() == null) {
            order.setStatus(StatusTypeEnum.CREATED);
        }
        return super.save(order);
    }

    @Override
    public void deleteOrder(Order order) throws NoPermissionsException {
        User currentUser = userService.findUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        boolean isHasAccess = currentUser.getAccesses().stream().anyMatch(dto -> dto.getAccessLevel() == AccessLevelType.DeleteOrder);
        if(isHasAccess)
            super.delete(order);
        else
            throw new NoPermissionsException("Permission for user + " + currentUser.getUsername() + " denied!");

    }

    @Override
    public boolean checkIfCurrentUserHasPermissions(AccessLevelType accessLevelType) {
        User currentUser = userService.findUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        return currentUser.getAccesses().stream().anyMatch(dto -> dto.getAccessLevel() == accessLevelType);
    }
}
