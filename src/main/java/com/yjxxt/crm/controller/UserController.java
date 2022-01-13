package com.yjxxt.crm.controller;


import com.yjxxt.crm.base.BaseController;
import com.yjxxt.crm.base.ResultInfo;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.exceptions.ParamsException;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.service.UserService;
import com.yjxxt.crm.utils.LoginUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @RequestMapping("toPasswordPage")
    public String updatePwd(){
        return "user/password";
    }



    @RequestMapping("index")
    public String index(){
        return "user/user";
    }

    @RequestMapping("addOrUpdatePage")
    public String addOrUpdatePage(Integer id, Model model){
        if (id!=null){
            User user=userService.selectByPrimaryKey(id);
            model.addAttribute("user",user);
        }
        return "user/add_update";
    }

    @RequestMapping("toSettingPage")
    public String setting(HttpServletRequest req){
        // 获取用户 ID
        int userId=LoginUserUtil.releaseUserIdFromCookie(req);
        // 调用方法
        User user=userService.selectByPrimaryKey(userId);
        // 存储
        req.setAttribute("user",user);

        // 转发
        return "user/setting";
    }

    @RequestMapping("login")
    @ResponseBody
   public ResultInfo say(User user){
        ResultInfo resultInfo=new ResultInfo();
 //       try {
            UserModel userModel=userService.userLogin(user.getUserName(),user.getUserPwd());

            resultInfo.setResult(userModel);
/*        }catch (ParamsException e) { // 自定义异常
        e.printStackTrace();
        // 设置状态码和提示信息
            resultInfo.setCode(e.getCode());
            resultInfo.setMsg(e.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
            resultInfo.setCode(500);
            resultInfo.setMsg("操作失败！");
            }*/
       return resultInfo;
   }

   @RequestMapping("setting")
   @ResponseBody
   public ResultInfo sayUpdate(User user){
       ResultInfo resultInfo=new ResultInfo();
       // 修改信息

        userService.updateByPrimaryKeySelective(user);
       // 返回目标参数
       return resultInfo;
   }


   @RequestMapping("save")
   @ResponseBody
   public ResultInfo save(User user){
       ResultInfo resultInfo=new ResultInfo();
       // 添加用户
        userService.addUser(user);
       // 返回目标参数
       return success("添加用户成功");
   }

   @RequestMapping("update")
   @ResponseBody
   public ResultInfo update(User user){
       ResultInfo resultInfo=new ResultInfo();
       // 添加用户
        userService.changeUser(user);
       // 返回目标参数
       return success("修改用户成功");
   }



   @RequestMapping("delete")
   @ResponseBody
   public ResultInfo delete(Integer[] ids){
       // 添删除用户
        userService.removeUserIds(ids);
       // 返回目标参数
       return success("批量删除用户成功");
   }





   @PostMapping("updatePwd")
   @ResponseBody
   public ResultInfo updatePwd(HttpServletRequest req,String oldPassword,String newPassword,String confirmPwd){
        ResultInfo resultInfo=new ResultInfo();
        // 获取 Cookie 中的 userId
       int userId= LoginUserUtil.releaseUserIdFromCookie(req);
       // 修改密码操作
//       try {
           userService.changeUserPwd(userId,oldPassword,newPassword,confirmPwd);
/*       }catch (ParamsException pe){
           pe.printStackTrace();
           resultInfo.setCode(pe.getCode());
           resultInfo.setMsg(pe.getMsg());
       }catch (Exception ex){
           ex.printStackTrace();
           resultInfo.setCode(500);
           resultInfo.setMsg("操作失败！");
       }*/

       return resultInfo;
   }

   @RequestMapping("sales")
   @ResponseBody
   public List<Map<String,Object>> findSales(){
        List<Map<String,Object>> list=userService.querySales();
        return list;
   }

   @RequestMapping("list")
   @ResponseBody
   public Map<String,Object> list(UserQuery userQuery){
        return userService.findUserByParams(userQuery);

   }



}


















