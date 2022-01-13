package com.yjxxt.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.bean.UserRole;
import com.yjxxt.crm.mapper.UserMapper;
import com.yjxxt.crm.mapper.UserRoleMapper;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.utils.AssertUtil;
import com.yjxxt.crm.utils.Md5Util;
import com.yjxxt.crm.utils.PhoneUtil;
import com.yjxxt.crm.utils.UserIDBase64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService extends BaseService<User,Integer> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    public UserModel userLogin(String userName,String userPwd){

        checkUserLoginParam(userName,userPwd);
        // 用户是否存在
        User temp=userMapper.selectUserByName(userName);
        AssertUtil.isTrue(temp==null,"用户不存在");
        // 用户密码是否正确
        checkUserPwd(userPwd,temp.getUserPwd());
        // 构建返回对象
        return builderUserInfo(temp);
    }

    /*
    * 构建返回目标对象 */
    private UserModel builderUserInfo(User user) {
        // 实例化目标对象
        UserModel userModel=new UserModel();

        // 加密
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        // 返回目标对象
        return userModel;
    }

    /* 校验用户名密码*/
    private void checkUserLoginParam(String userName, String userPwd) {
        // 用户名非空
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户不能为空");
        // 用户密码非空
        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"用户密码不能为空");
    }

    /*验证密码的*/
    private void checkUserPwd(String userPwd, String userPwd1) {
        // 对输入的密码加密
        userPwd =Md5Util.encode(userPwd);
        // 加密的密码比对数据输入的密码
        AssertUtil.isTrue(!userPwd.equals(userPwd1),"用户密码不正确");
    }




    /*
    * 修改密码*/











    public void changeUserPwd(Integer userId,String oldPassword,String newPassword,String confirmPwd){
        // 用户登录了修改，userId
        User user=userMapper.selectByPrimaryKey(userId);
        // 密码验证
        checkPasswordParams(user,oldPassword,newPassword,confirmPwd);

        // 修改密码
        user.setUserPwd(Md5Util.encode(newPassword));
        // 确认密码修改是否成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user)<1,"修改失败了");
    }

    /**
     * 修改密码的验证
     * */
    private void checkPasswordParams(User user, String oldPassword, String newPassword, String confirmPwd) {
        AssertUtil.isTrue(user==null,"用户未登录或者用户不存在");
        // 原始密码非空
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword),"请输入原始密码");
        // 原始密码是否正确
        AssertUtil.isTrue(!(user.getUserPwd().equals(Md5Util.encode(oldPassword))),"原始密码不正确");
        // 新密码非空
        AssertUtil.isTrue(StringUtils.isBlank(newPassword),"新密码不能为空");
        // 新密码不能和原始密码一致
        AssertUtil.isTrue(newPassword.equals(oldPassword),"新密码和原始密码不能相同");
        // 确认密码非空
        AssertUtil.isTrue(StringUtils.isBlank(confirmPwd),"确认密码不能为空");
        // 确认密码和新密码一致
        AssertUtil.isTrue(!confirmPwd.equals(newPassword),"确认密码和新密码要一致");
    }








    /* 查询所有销售人员 */
    public List<Map<String,Object>>querySales(){

        return userMapper.selectSales();
    }










    /* 用户模块的列表查询 */
    public Map<String,Object> findUserByParams(UserQuery userQuery){
        // 实例化 map
        Map<String,Object> map=new HashMap<String,Object>();
        // 初始化分页单位
        PageHelper.startPage(userQuery.getPage(),userQuery.getLimit());
        // 开始分页
        PageInfo<User> plist=new PageInfo<User>(selectByParams(userQuery));

        // 准备数据
        map.put("code",0);
        map.put("msg","success");
        map.put("count",plist.getTotal());
        map.put("data",plist.getList());
        // 返回目标 map
        return map;

    }


    /**
     * 一，验证
     * 1：用户非空，且唯一
     * 2：邮箱非空
     * 3：手机非空，格式正确
     * 二，设定默认值
     * is_valid=1
     * createDate 系统时间
     * updateDate 系统时间
     * 密码
     *          123456---加密
     * 三，添加是否成功
     * @param user
     */
    @Transactional (propagation = Propagation.REQUIRED)
    public void addUser(User user){

        // 验证
        checkUser(user.getUserName(),user.getEmail(),user.getPhone());
        //---------用户名唯一  ---------------------------------------
        User temp=userMapper.selectUserByName(user.getUserName());
        AssertUtil.isTrue(temp!=null,"用户名已经存在");
        //----------------------------------------------------------
        // 设定默认值
        user.setIsValid(1);
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        // 密码加密
        user.setUserPwd(Md5Util.encode("123456"));
        // 验证密码是否成功
        AssertUtil.isTrue(insertSelective(user)<1,"添加失败了");
        System.out.println(user.getId()+"<<<"+user.getRoleIds());
        //********
        relationUserRole(user.getId(),user.getRoleIds());

    }


    /**
     *
     * @param userId 用户id
     * @param roleIds 角色 id
     */
    private void relationUserRole(Integer userId, String roleIds) {
        // 准备集合存储对象
        List<UserRole> urlist=new ArrayList<UserRole>();
        // userId,roleId;
        AssertUtil.isTrue(StringUtils.isBlank(roleIds),"请选择角色信息");
        // 统计当前用户有多少个角色
        int count=userRoleMapper.countUserRoleNum(userId);
        // 删除当前用户的角色
        if (count>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"删除用户角色失败");
        }
        // 删除原来的角色
        String[] RoleStrId=roleIds.split(",");

        // 遍历
        for (String rid:RoleStrId){

            // 准备对象
            UserRole userRole=new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(Integer.parseInt(rid));
            userRole.setCreateDate(new Date());
            userRole.setUpdateDate(new Date());
            // 存放到集合
            urlist.add(userRole);
        }

        // 批量添加
        AssertUtil.isTrue(userRoleMapper.insertBatch(urlist)!=urlist.size(),"用户角色分配失败");

    }


    /**
     *
     * @param userName
     * @param email
     * @param phone
     */
    private void checkUser(String userName, String email, String phone) {
        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空");
        // 用户名唯一
        User temp=userMapper.selectUserByName(userName);
        AssertUtil.isTrue(temp!=null,"用户名已经存在");
        AssertUtil.isTrue(StringUtils.isBlank(email),"用户邮箱不能为空");
        AssertUtil.isTrue(StringUtils.isBlank(phone),"用户手机号不能为空");
        AssertUtil.isTrue(!PhoneUtil.isMobile(phone),"必须输入合法的手机号");

    }


    /**
     * 一，验证
     * 1：用户非空，且唯一
     * 2：邮箱非空
     * 3：手机非空，格式正确
     * 二，设定默认值
     * is_valid=1
     * createDate 系统时间
     * updateDate 系统时间
     * 密码
     *          123456---加密
     * 三，添加是否成功
     *
     */
    @Transactional (propagation = Propagation.REQUIRED)
    public void changeUser(User user){
        // 根据ID获取 查询是否有 id
        User temp=userMapper.selectByPrimaryKey(user.getId());
        // 判断 ID 是否存在
        AssertUtil.isTrue(temp==null,"待修改的用户不存在");

        User temp2=userMapper.selectUserByName(user.getUserName());
        AssertUtil.isTrue(temp2!=null&& !(temp2.getId().equals(user.getId())),"用户名已经存在");
        // 验证
        checkUser(user.getUserName(),user.getEmail(),user.getPhone());
        // 设定默认值
        user.setUpdateDate(new Date());
        // 判断修改是否成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(user)<1,"修改失败了");
        //
        relationUserRole(user.getId(),user.getRoleIds());


    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void removeUserIds(Integer[] ids){
        // 验证
        AssertUtil.isTrue(ids==null || ids.length==0,"请选择删除的数据");

        // 遍历对象
        for(Integer userId:ids){
            // 统计当前用户有多少个角色
            int count=userRoleMapper.countUserRoleNum(userId);
            // 删除当前用户的角色
            if (count>0){
                AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"删除用户角色失败");
            }
        }

        // 判断删除是否成功
        AssertUtil.isTrue(userMapper.deleteBatch(ids)<1,"删除失败了");
    }
}












































