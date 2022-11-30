package com.jhc.demo.service;

import com.jhc.demo.controller.dto.UserDTO;
import com.jhc.demo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jhc
 * @since 2022-11-16
 */
public interface IUserService extends IService<User> {

    UserDTO login(UserDTO userDTO);
}
