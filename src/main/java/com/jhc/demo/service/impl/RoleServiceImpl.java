package com.jhc.demo.service.impl;

import com.jhc.demo.entity.Role;
import com.jhc.demo.mapper.RoleMapper;
import com.jhc.demo.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author jhc
 * @since 2022-12-10
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

}
