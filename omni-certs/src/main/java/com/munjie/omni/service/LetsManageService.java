package com.munjie.omni.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.munjie.omni.dto.CertChallengeDTO;
import com.munjie.omni.dto.CertDetailDTO;
import com.munjie.omni.pojo.dto.PageReq;
import com.munjie.omni.pojo.entity.AcmeCertificateInfoEntity;
import com.munjie.omni.vo.CertificateVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

public interface LetsManageService {

    List<CertificateVO> list();


    IPage<AcmeCertificateInfoEntity> pageLets(PageReq req);
    CertDetailDTO getById(Long id);

    String delete(Long id);


    CertChallengeDTO createOrder(String domain) throws Exception;

    boolean preCheckDns(String domain);
    boolean checkDns(Long id);

    void verifyAndIssue(Long id) throws Exception;

    ResponseEntity<byte[]> download(Long id) throws Exception;


    //    ResponseEntity<ByteArrayResource> exportReport(ExportRequestDTO requestDTO, HttpServletRequest request) throws IOException;
    ResponseEntity<ByteArrayResource> downloadLets(Long id, HttpServletRequest request) throws IOException;


}
