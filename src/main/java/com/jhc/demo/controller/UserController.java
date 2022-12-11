package com.jhc.demo.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhc.demo.common.Constants;
import com.jhc.demo.common.Result;
import com.jhc.demo.controller.dto.UserDTO;
import com.jhc.demo.exception.ServiceException;
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

    /**
     * 登录
     * @param userDTO
     * @return
     */
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
        // 隐藏用户密码，之后改为rsa加密
        dto.setPassword(null);
        return Result.success(dto);
    }

    /**
     * 检查password是否正确
     * @param userPassword
     * @return
     */
    @PostMapping("/checkPassword")
    public Result checkPassword(@RequestBody User userPassword) {
        Integer id = userPassword.getId();
        User byId = userService.getById(id);
        if(!StrUtil.equals(byId.getPassword(), userPassword.getPassword())) {
            throw new ServiceException(Constants.CODE_600, "原密码错误");
        }
        return Result.success();
    }

    /**
     * 新增或更新
     * @param user
     * @return
     */
    @PostMapping
    public Result save(@RequestBody User user) {
        userService.saveOrUpdate(user);
        return Result.success();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping("/del/batch")
    public Result deleteBatch(@PathVariable List<Integer> ids) {
        userService.removeByIds(ids);
        return Result.success();
    }


    /**
     * 分页查询
     * @param currentPage
     * @param size
     * @param username
     * @return
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer currentPage,
                           @RequestParam Integer size,
                           @RequestParam(defaultValue = "") String  username,
                           @RequestParam(defaultValue = "") String  nickname,
                           @RequestParam(defaultValue = "") String  phone) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if(!"".equals(username)) {
            queryWrapper.like("username", username);
        }
        if(!"".equals(nickname)) {
            queryWrapper.like("nickname", nickname);
        }
        if(!"".equals(phone)) {
            queryWrapper.like("phone", phone);
        }

//        User currentUser = TokenUtils.getCurrentUser();
//        System.out.println(currentUser.getUsername());
        return Result.success(userService.page(new Page<>(currentPage, size), queryWrapper));
    }
}

