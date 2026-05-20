package com.munjie.omni.service;

import com.munjie.omni.pojo.dto.LoginReqDTO;
import com.munjie.omni.pojo.entity.SysUserEntity;
import com.munjie.omni.pojo.vo.LoginResVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

public interface AuthService {

    /**
     * 获取登录小程序码code
     *
     * @return
     * @throws Exception
     */
    ResponseEntity<byte[]> getQrCode() throws Exception;

    /**
     * 获取 openid
     *
     * @param code
     * @return
     *
     */
    Map<String, String> getOpenId(String code);


    /**
     * 绑定扫码状态
     *
     * @param body
     * @return
     */
    boolean bindStatus(Map<String, String> body);


    /**
     * 确认登录
     *
     * @param body
     * @return
     */
    boolean confirmLogin(Map<String, String> body);


    /**
     * 根据小程序code 查询用户信息
     *
     * @param code
     * @return
     */
    SysUserEntity getUserbyCode(String code);


    /**
     * 用户名密码登录
     *
     * @param request
     * @return
     */
    LoginResVO login(LoginReqDTO request);

    /**
     * 退出
     * @return
     */
    String logout();

    /**
     * gitee登录回调
     * @param code
     * @param state
     * @return
     */
    RedirectView giteeCallback(String code, String state);

    /**
     * qq登录回调
     * @param code
     * @param state
     * @return
     */
    RedirectView qqCallback(String code, String state);


    /**
     * github登录回调
     * @param code
     * @param state
     * @return
     */
    RedirectView gitHubLogin(String code, String state, HttpServletResponse response);

    /**
     * 统一登录
     * @param platform
     * @param redirect
     * @param request
     * @return
     */
    RedirectView loginAuth(String platform, String redirect, HttpServletRequest request);

}
