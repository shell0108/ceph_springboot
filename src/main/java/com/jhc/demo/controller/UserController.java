package com.jhc.demo.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhc.demo.common.Constants;
import com.jhc.demo.common.Result;
import com.jhc.demo.controller.dto.UserDTO;
import com.jhc.demo.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

import com.jhc.demo.service.IUserService;
import com.jhc.demo.entity.User;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jhc
 * @since 2022-11-16
 */
@RestController
@RequestMapping("/user")

public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("/login")
    //@RequestBody 可将前端传来的json转为java对象
    public Result login(@RequestBody UserDTO userDTO) {
        String userName = userDTO.getUsername();
        String password = userDTO.getPassword();
        if(StrUtil.isBlank(userName) || StrUtil.isBlank(password)){
            return Result.error(Constants.CODE_400, "参数错误");
        }
        // ctrl+alt+b 进入服务实现类login函数
        UserDTO dto = userService.login(userDTO);
        return Result.success(dto);
    }

    //新增或更新
    @PostMapping
    public Boolean save(@RequestBody User user) {
        return userService.saveOrUpdate(user);
    }

    //删除
    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Integer id) {
        return userService.removeById(id);
    }
    //删除所有
    @DeleteMapping("/del/batch")
    public Boolean deleteBatch(@PathVariable List<Integer> ids) {
        return userService.removeByIds(ids);
    }

    //查询所有
    @GetMapping
    public List<User> findAll() {
        return userService.list();
    }

    @GetMapping("/{id}")
    public List<User> findOne(@PathVariable Integer id) {
        return userService.list();
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer currentPage,
                           @RequestParam Integer size,
                           @RequestParam(defaultValue = "") String  username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if(!"".equals(username)) {
            queryWrapper.like("username", username);
        }

        User currentUser = TokenUtils.getCurrentUser();
        System.out.println(currentUser.getUsername());
        return Result.success(userService.page(new Page<>(currentPage, size), queryWrapper));
    }
}

