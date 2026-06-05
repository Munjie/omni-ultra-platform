package com.munjie.omni.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author mwj
 * @since 2026-06-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("acme_certificate_info")
public class AcmeCertificateInfoEntity implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请域名
     */
    private String domain;

    /**
     * ACME账户私钥(PEM)
     */
    @TableField("account_private_key")
    private String accountPrivateKey;

    /**
     * ACME账户公钥(PEM)
     */
    @TableField("account_public_key")
    private String accountPublicKey;

    /**
     * ACME账户注册URL
     */
    @TableField("account_url")
    private String accountUrl;

    /**
     * 域名私钥(PEM)
     */
    @TableField("domain_private_key")
    private String domainPrivateKey;

    /**
     * 域名公钥(PEM)
     */
    @TableField("domain_public_key")
    private String domainPublicKey;

    /**
     * ACME订单URL
     */
    @TableField("order_url")
    private String orderUrl;

    /**
     * 签发的证书内容(PEM)
     */
    @TableField("certificate_content")
    private String certificateContent;

    /**
     * 状态: PENDING, READY, VALID, EXPIRED
     */
    private String status;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    /**
     * TXT记录值
     */
    @TableField("dns_txt_value")
    private String dnsTxtValue;

    /**
     * dns 检测名
     */
    @TableField("dns_domain")
    private String dnsDomain;

    /**
     * 主机记录
     */
    @TableField("host_record")
    private String hostRecord;

    /**
     * 记录值
     */
    @TableField("record_value")
    private String recordValue;

    /**
     * 证书过期时间
     */
    @TableField("expiry_date")
    private Date expiryDate;

    /**
     * 证书签发时间
     */
    @TableField("issue_date")
    private Date issueDate;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    @TableField("create_by_id")
    private Integer createById;

    @TableField("update_by_id")
    private Integer updateById;

    /**
     * 专属同步部署Token
     */
    @TableField("sync_token")
    private String syncToken;

    @TableField("deploy_server_type")
    private String deployServerType;

    @TableField("deploy_cert_path")
    private String deployCertPath;

    @TableField("deploy_key_path")
    private String deployKeyPath;

    @TableField("deploy_reload_cmd")
    private String deployReloadCmd;

    /**
     * 是否开启自动续期：0-关闭，1-开启
     */
    @TableField("auto_renew")
    private Boolean autoRenew;

    /**
     * 最近一次续期状态：RENEWING, SUCCESS, FAILED
     */
    @TableField("renew_status")
    private String renewStatus;


}
