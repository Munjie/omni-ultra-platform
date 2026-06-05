package com.munjie.omni.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.munjie.omni.dto.CertChallengeDTO;
import com.munjie.omni.dto.CertDetailDTO;
import com.munjie.omni.dto.DomainDTO;
import com.munjie.omni.pojo.dto.ExportLetsDTO;
import com.munjie.omni.pojo.dto.PageReq;
import com.munjie.omni.pojo.entity.AcmeCertificateInfoEntity;
import com.munjie.omni.service.AcmeCertificateInfoService;
import com.munjie.omni.service.LetsManageService;
import com.munjie.omni.vo.CertificateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author muwen
 */
@RestController
@RequestMapping("/lets")
@Tag(name = "Let'sEncrypt证书管理API")
@Slf4j
public class LetsManageController {

    @Resource
    private LetsManageService letsManageService;

    @Resource
    private AcmeCertificateInfoService infoService;

    @Operation(summary = "详细信息")
    @GetMapping("/get/{id}")
    public CertDetailDTO getById(@PathVariable("id") Long id) {
        return letsManageService.getById(id);
    }


    @Operation(summary = "DNS生效检测")
    @GetMapping("/check-dns/{id}")
    public Boolean checkDns(@PathVariable("id") Long id) {
        return letsManageService.checkDns(id);

    }

    @Operation(summary = "提交申请")
    @GetMapping("/confirm/{id}")
    public String confirm(@PathVariable("id") Long id) throws Exception {
        letsManageService.verifyAndIssue(id);
        return "证书签发任务已启动";

    }

    @GetMapping("/list")
    @Operation(summary = "查询")
    public List<CertificateVO> list() {
        return letsManageService.list();
    }

    @PostMapping("/create")
    @Operation(summary = "申请")
    public CertChallengeDTO createOrder(@RequestBody @Validated DomainDTO domain) throws Exception {
        return letsManageService.createOrder(domain.getDomain());

    }


    @PostMapping("/page-lets")
    @Operation(summary = "分页查询")
    public IPage<AcmeCertificateInfoEntity> pageLets(@RequestBody PageReq req) {
        return letsManageService.pageLets(req);
    }

    @DeleteMapping(value = "/delete")
    public String delete(@RequestParam("id") Long id) {
        return letsManageService.delete(id);
    }


    @PostMapping("/download")
    @Operation(summary = "下载证书")
    public ResponseEntity<byte[]> download(HttpServletRequest request, @RequestBody ExportLetsDTO req) throws Exception {
        return letsManageService.download(req.getId());

    }

    @PutMapping("/{id}/toggle-renew")
    public String toggleAutoRenew(@PathVariable Long id, @RequestParam Integer autoRenew) {
       /* CertRecord record = certMapper.selectById(id);
        if (record == null) {
            return Result.error(404, "未找到相关记录");
        }

        record.setAutoRenew(autoRenew);
        if (autoRenew == 0) {
            record.setRenewStatus(null); // 关闭时清空续期状态
        }
        certMapper.updateById(record);

        return Result.success(autoRenew == 1 ? "已开启自动续期" : "已关闭自动续期");*/
        return "已开启自动续期";
    }

    /**
     * 供 Linux 客户端脚本拉取最新证书的开放接口
     */
    @GetMapping("/download-latest/{token}")
    public ResponseEntity<?> downloadByToken(@PathVariable("token") String token) {
        AcmeCertificateInfoEntity info = infoService.getOne(
                new LambdaQueryWrapper<AcmeCertificateInfoEntity>().eq(AcmeCertificateInfoEntity::getSyncToken, token)
        );

        if (info == null || !"VALID".equals(info.getStatus())) {
            return ResponseEntity.status(403).body("Invalid Token or Certificate not ready");
        }
        Map<String, String> result = new HashMap<>();
        result.put("domain", info.getDomain());
        result.put("privateKey", info.getDomainPrivateKey());
        result.put("certificate", info.getCertificateContent());
        return ResponseEntity.ok(result);
    }


    /**
     * 1. 提交或保存用户的部署表单配置，并为其生成/返回专属一键命令
     */
    @PostMapping("/save-deploy")
    public Map<String, String> saveDeployConfig(@RequestBody AcmeCertificateInfoEntity req) {
        AcmeCertificateInfoEntity info = infoService.getById(req.getId());
        if (info == null) {
//            return thow (404, "未找到该证书申请记录");
        }

        // 更新部署配置项
        info.setDeployServerType(req.getDeployServerType());
        info.setDeployCertPath(req.getDeployCertPath());
        info.setDeployKeyPath(req.getDeployKeyPath());
        info.setDeployReloadCmd(req.getDeployReloadCmd());
        info.setAutoRenew(true);

        if (info.getSyncToken() == null || info.getSyncToken().isEmpty()) {
            info.setSyncToken(UUID.randomUUID().toString().replace("-", ""));
        }
        infoService.updateById(info);

        String rawToken = info.getSyncToken();
        String installCmd = String.format(
                "curl -sSO https://www.munjie.com/api/lets/shell/%s && " +
                        "chmod +x jcloud-ssl-sync.sh && " +
                        "(crontab -l 2>/dev/null; echo \"30 2 10,25 * * /bin/bash $(pwd)/jcloud-ssl-sync.sh -token=%s -cert_path=%s -key_path=%s -command=\\\"%s\\\" >> ./jcloud-cron.log 2>&1\") | crontab - && " +
                        "./jcloud-ssl-sync.sh -token=%s -cert_path=%s -key_path=%s -command=\"%s\"",
                rawToken, rawToken, info.getDeployCertPath(), info.getDeployKeyPath(), info.getDeployReloadCmd(),
                rawToken, info.getDeployCertPath(), info.getDeployKeyPath(), info.getDeployReloadCmd()
        );

        Map<String, String> responseData = new HashMap<>();
        responseData.put("command", installCmd);
        responseData.put("token", rawToken);
        return responseData;
    }

    @GetMapping("/shell/{token}")
    public void downloadShellScript(@PathVariable("token") String token, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        System.out.println("token = " + token);
        AcmeCertificateInfoEntity info = infoService.getOne(
                new LambdaQueryWrapper<AcmeCertificateInfoEntity>().eq(AcmeCertificateInfoEntity::getSyncToken, token)
        );
        String shellContent = "";
        response.setContentType("text/plain;charset=UTF-8");
        if (info == null || !"VALID".equals(info.getStatus())) {
            response.getWriter().write("无效token或者证书未生效");
        } else {
            shellContent = "#!/bin/bash\n" +
                    "TOKEN=\"\"\nCERT_PATH=\"\"\nKEY_PATH=\"\"\nRELOAD_CMD=\"\"\n\n" +
                    "while [ $# -gt 0 ]; do\n" +
                    "  case \"$1\" in\n" +
                    "    -token=*) TOKEN=\"${1#*=}\" ;;\n" +
                    "    -cert_path=*) CERT_PATH=\"${1#*=}\" ;;\n" +
                    "    -key_path=*) KEY_PATH=\"${1#*=}\" ;;\n" +
                    "    -command=*) RELOAD_CMD=\"${1#*=}\" ;;\n" +
                    "  esac\n" +
                    "  shift\n" +
                    "done\n\n" +
                    "echo \"[JCloud] 正在连接...\"\n" +
                    "RESPONSE=$(curl -sL \"https://www.munjie.com/api/lets/download-latest/${TOKEN}\")\n\n" +
                    "if [[ $RESPONSE != *\"BEGIN CERTIFICATE\"* ]]; then\n" +
                    "    echo \"[Error] 证书提取失败：凭证不正确或证书尚未验证成功\"\n" +
                    "    exit 1\n" +
                    "fi\n\n" +
                    "if ! command -v python3 &> /dev/null; then\n" +
                    "    echo \"[JCloud] 检测到当前服务器缺失 python3 环境，正在尝试自动构建轻量级依赖...\"\n" +
                    "    \n" +
                    "    # 检测包管理器\n" +
                    "    if command -v apt-get &> /dev/null; then\n" +
                    "        sudo apt-get update -y && sudo apt-get install -y python3\n" +
                    "    elif command -v yum &> /dev/null; then\n" +
                    "        sudo yum install -y python3\n" +
                    "    else\n" +
                    "        echo \"[Error] 无法自动为您安装 python3 (未找到常见的包管理器)，请手动执行安装python3环境后重新运行此脚本。\"\n" +
                    "        exit 1\n" +
                    "    fi\n" +
                    "fi"+
                    "PRIVATE_KEY=$(echo \"$RESPONSE\" | python3 -c \"import sys, json; print(json.load(sys.stdin)['privateKey'])\")\n" +
                    "CERTIFICATE=$(echo \"$RESPONSE\" | python3 -c \"import sys, json; print(json.load(sys.stdin)['certificate'])\")\n\n" +
                    "mkdir -p $(dirname \"$CERT_PATH\")\n" +
                    "mkdir -p $(dirname \"$KEY_PATH\")\n\n" +
                    "# 备份机制\n" +
                    "DATE_STR=$(date +%Y%m%d%H%M%S)\n" +
                    "[ -f \"$CERT_PATH\" ] && cp \"$CERT_PATH\" \"${CERT_PATH}.backup_${DATE_STR}\"\n" +
                    "[ -f \"$KEY_PATH\" ] && cp \"$KEY_PATH\" \"${KEY_PATH}.backup_${DATE_STR}\"\n\n" +
                    "echo \"$PRIVATE_KEY\" > \"$KEY_PATH\"\n" +
                    "echo \"$CERTIFICATE\" > \"$CERT_PATH\"\n\n" +
                    "echo \"[Success] 证书写入完毕，准备重载Web服务...\"\n" +
                    "if [ -n \"$RELOAD_CMD\" ]; then\n" +
                    "    eval \"$RELOAD_CMD\"\n" +
                    "fi\n" +
                    "echo \"[Success] 本轮自动化同步部署任务结束。\"\n";

        }
        response.getWriter().write(shellContent);
        response.getWriter().flush();
    }


}
