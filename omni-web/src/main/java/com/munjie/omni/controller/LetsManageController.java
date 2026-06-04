package com.munjie.omni.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.munjie.omni.dto.CertChallengeDTO;
import com.munjie.omni.dto.DomainDTO;
import com.munjie.omni.pojo.dto.ExportLetsDTO;
import com.munjie.omni.pojo.dto.PageReq;
import com.munjie.omni.pojo.entity.AcmeCertificateInfoEntity;
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

import java.util.List;

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

    @Operation(summary ="详细信息")
    @GetMapping("/get/{id}")
    public CertChallengeDTO getById(@PathVariable("id") Long id) {
        return  letsManageService.getById(id);

    }

    @Operation(summary ="DNS生效检测")
    @GetMapping("/check-dns/{id}")
    public Boolean checkDns(@PathVariable("id") Long id) {
        return  letsManageService.checkDns(id);

    }

    @Operation(summary ="提交申请")
    @GetMapping("/confirm/{id}")
    public String confirm(@PathVariable("id") Long id) throws Exception {
          letsManageService.verifyAndIssue(id);
          return "证书签发任务已启动";

    }

    @GetMapping("/list")
    @Operation(summary ="查询")
    public List<CertificateVO> list() {
        return letsManageService.list();
    }

    @PostMapping("/create")
    @Operation(summary ="申请")
    public CertChallengeDTO createOrder(@RequestBody @Validated DomainDTO domain) throws Exception {
        return letsManageService.createOrder(domain.getDomain());

    }


    @PostMapping("/page-lets")
    @Operation(summary ="分页查询")
    public IPage<AcmeCertificateInfoEntity> pageLets(@RequestBody PageReq req) {
        return letsManageService.pageLets(req);
    }

    @DeleteMapping(value="/delete")
    public String delete(@RequestParam("id") Long id) {
        return  letsManageService.delete(id);
    }






    @PostMapping("/download")
    @Operation(summary ="下载证书")
    public ResponseEntity<byte[]> download(HttpServletRequest request, @RequestBody ExportLetsDTO req) throws Exception {
      return  letsManageService.download(req.getId());

    }














}
