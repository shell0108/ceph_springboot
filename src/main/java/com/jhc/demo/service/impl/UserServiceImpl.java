package com.jhc.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jhc.demo.common.Constants;
import com.jhc.demo.controller.dto.UserDTO;
import com.jhc.demo.entity.User;
import com.jhc.demo.exception.ServiceException;
import com.jhc.demo.mapper.UserMapper;
import com.jhc.demo.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhc.demo.utils.TokenUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jhc
 * @since 2022-11-16
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    //private static final Log LOG = Log.get();
    @Override
    public UserDTO login(UserDTO userDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername());
        queryWrapper.eq("password", userDTO.getPassword());
        //alt+enter 生成变量
        User one;
        try{
            one = getOne(queryWrapper); //从数据库查询用户信息
        } catch (Exception e){
            //防止脏数据，多个相同的账户
            //LOG.error(e);
            throw new ServiceException(Constants.CODE_500, "系统错误");
        }
        if(one != null) {
            BeanUtil.copyProperties(one, userDTO, true);
            String token = TokenUtils.getToken(one.getId().toString(), one.getPassword());
            userDTO.setToken(token);
            return userDTO;
        }else {
            throw new ServiceException(Constants.CODE_600, "用户名或密码错误");
        }
    }
//    @Override
//    public UserDTO register(UserDTO userDTO) {
//
//    }
}
