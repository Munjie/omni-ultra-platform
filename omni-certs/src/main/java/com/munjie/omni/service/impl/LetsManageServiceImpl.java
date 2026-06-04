package com.munjie.omni.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.net.InternetDomainName;
import com.munjie.omni.dto.CertChallengeDTO;
import com.munjie.omni.exception.CustomException;
import com.munjie.omni.pojo.dto.PageReq;
import com.munjie.omni.pojo.entity.AcmeCertificateInfoEntity;
import com.munjie.omni.service.AcmeCertificateInfoService;
import com.munjie.omni.service.LetsManageService;
import com.munjie.omni.utils.DnsUtil;
import com.munjie.omni.utils.KeyPairConvertUtil;
import com.munjie.omni.vo.CertificateVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.toolbox.JSON;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.munjie.omni.constant.AcmeConstants.*;


@Service
@Slf4j
@Transactional
public class LetsManageServiceImpl implements LetsManageService {

    @Value("${acme.url}")
    private String acmeUrl;

    @Resource
    private AcmeCertificateInfoService infoService;

    @Override
    public CertChallengeDTO getById(Long id) {
        AcmeCertificateInfoEntity entity = infoService.getById(id);
        return CertChallengeDTO.builder()
                .domain(entity.getDomain())
                .hostRecord(entity.getHostRecord())
                .recordValue(entity.getDnsTxtValue())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    @Override
    public List<CertificateVO> list() {
        List<AcmeCertificateInfoEntity> list = infoService.list();
        return list.stream().map(m -> {
            return CertificateVO.builder()
                    .domain(m.getDomain()).status(m.getStatus())
                    .isWildcard(false)
                    .issuer("Let's Encrypt")
                    .expiryDate(DateUtil.format(m.getExpiryDate(), "yyyy-MM-dd HH:mm:ss"))
                    .id(m.getId()).build();
        }).toList();

    }

    @Override
    public IPage<AcmeCertificateInfoEntity> pageLets(PageReq req) {
        LambdaQueryWrapper<AcmeCertificateInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AcmeCertificateInfoEntity::getCreateTime);
        Page<AcmeCertificateInfoEntity> pageParam = new Page<>(req.getPageNum(), req.getPageSize());
        return infoService.page(pageParam, wrapper);
    }

    @Override
    public String delete(Long id) {
        infoService.removeById(id);
        return "删除成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CertChallengeDTO createOrder(String domain) throws Exception {
        AcmeCertificateInfoEntity info = infoService.getOne(new LambdaQueryWrapper<AcmeCertificateInfoEntity>().eq(AcmeCertificateInfoEntity::getDomain, domain));
        if (info != null && !INVALID.equals(info.getStatus())) {
            return buildDTOFromExisting(info);
        }
        String hostRecord = DnsUtil.calculateHostRecord(domain);
        String rootDomain = InternetDomainName.from(domain.replace("*.", ""))
                .topPrivateDomain().toString();
        String fullRecordPath = hostRecord + "." + rootDomain;
        Session session = new Session(acmeUrl);
        KeyPair accountKeyPair;
        URL accountUrl;
        if (info != null && info.getAccountUrl() != null) {
            accountKeyPair = KeyPairConvertUtil.stringToKeyPair(info.getAccountPrivateKey());
            accountUrl = new URL(info.getAccountUrl());
        } else {
            accountKeyPair = KeyPairUtils.createKeyPair(2048);
            Account account = new AccountBuilder().agreeToTermsOfService().useKeyPair(accountKeyPair).create(session);
            accountUrl = account.getLocation();
        }
        Login login = session.login(accountUrl, accountKeyPair);
        // 3. 开启新订单
        Order order = login.getAccount().newOrder().domains(domain).create();
        Optional<Dns01Challenge> challenge = Optional.empty();
        for (Authorization auth : order.getAuthorizations()) {
            challenge = auth.findChallenge(Dns01Challenge.class);
        }
        if (info == null) {
            info = new AcmeCertificateInfoEntity();
        }
        JSON json = challenge.get().getJSON();
        log.info("challenge value: " + json.toString());
        info.setDomain(domain);
        info.setAccountPrivateKey(KeyPairConvertUtil.keyPairToString(accountKeyPair));
        info.setAccountUrl(accountUrl.toString());
        info.setOrderUrl(order.getLocation().toString());
        // 每次重新申请，都要生成新的域名私钥，确保匹配
        KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);
        info.setDomainPrivateKey(KeyPairConvertUtil.keyPairToString(domainKeyPair));
        info.setStatus(PENDING_CONFIG);
        info.setDnsTxtValue(challenge.get().getDigest());
        info.setHostRecord(hostRecord);
        info.setRecordValue(challenge.get().getDigest());
        info.setDnsDomain(fullRecordPath);
        if (info.getId() == null) {
            infoService.save(info);
        } else {
            infoService.updateById(info);
        }
        return buildDTOFromExisting(info);
    }

    @Override
    public boolean checkDns(Long id) {
        AcmeCertificateInfoEntity info = infoService.getById(id);
        if (ObjectUtil.isNull(info)) {
            throw new CustomException("未找到该域名的申请记录");
        }
        try {
            String fullRecordName = info.getDnsDomain();
            String expectedValue = info.getDnsTxtValue();
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns://8.8.8.8");
            env.put("com.sun.jndi.dns.timeout.initial", "2000");
            env.put("com.sun.jndi.dns.timeout.retries", "1");
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(fullRecordName, new String[]{"TXT"});
            Attribute txtAttr = attrs.get("TXT");
            if (txtAttr != null) {
                for (int i = 0; i < txtAttr.size(); i++) {
                    String value = txtAttr.get(i).toString();
                    if (value.replace("\"", "").trim().equals(expectedValue)) {
                        info.setStatus(DNS_SUCCESS);
                        infoService.updateById(info);
                        return true;
                    }
                }
            }
        } catch (javax.naming.NameNotFoundException e) {
            throw new CustomException("预检错误：域名 [" + info.getDnsDomain() + "] 尚未在 8.8.8.8 上生效...");
        } catch (Exception e) {
            throw new CustomException("预检 IO 错误:" + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean preCheckDns(String domain) {
        AcmeCertificateInfoEntity info = infoService.getOne(
                new LambdaQueryWrapper<AcmeCertificateInfoEntity>().eq(AcmeCertificateInfoEntity::getDomain, domain)
        );
        if (info == null) {
            throw new CustomException("未找到该域名的申请记录");
        }
        try {
            String fullRecordName = info.getDnsDomain();
            String expectedValue = info.getDnsTxtValue();
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("java.naming.provider.url", "dns://8.8.8.8");
            // 设置超时，防止预检卡死
            env.put("com.sun.jndi.dns.timeout.initial", "2000");
            env.put("com.sun.jndi.dns.timeout.retries", "1");

            DirContext ctx = new InitialDirContext(env);

            // 关键点：这里可能会抛出 NameNotFoundException
            Attributes attrs = ctx.getAttributes(fullRecordName, new String[]{"TXT"});
            Attribute txtAttr = attrs.get("TXT");

            if (txtAttr != null) {
                for (int i = 0; i < txtAttr.size(); i++) {
                    String value = txtAttr.get(i).toString();
                    if (value.replace("\"", "").trim().equals(expectedValue)) {
                        return true;
                    }
                }
            }
        } catch (javax.naming.NameNotFoundException e) {
            System.out.println("预检：域名 [" + info.getDnsDomain() + "] 尚未在 8.8.8.8 上生效...");
        } catch (Exception e) {
            System.err.println("预检 IO 错误: " + e.getMessage());
        }
        return false;
    }

    @Override
    @Async("certTaskExecutor")
    public void verifyAndIssue(Long id) throws Exception {
        AcmeCertificateInfoEntity info = infoService.getById(id);
        if (info == null) {
            throw new CustomException("未找到该域名的申请记录");
        }
        try {
            info.setStatus(ISSUING);
            infoService.updateById(info);
            Session session = new Session(acmeUrl);
            // 2. 还原密钥和账户
            KeyPair accountKeyPair = KeyPairConvertUtil.stringToKeyPair(info.getAccountPrivateKey());
            URL accountUrl = new URL(info.getAccountUrl());
            Login login = session.login(accountUrl, accountKeyPair);
            // 3. 绑定订单并触发验证
            Order order = login.bindOrder(new URL(info.getOrderUrl()));
            order.fetch();
            for (Authorization auth : order.getAuthorizations()) {
                auth.fetch();
                Optional<Dns01Challenge> challenge = auth.findChallenge(Dns01Challenge.class);
                if (challenge.isPresent()) {
                    Status currentStatus = challenge.get().getStatus();
                    JSON json = challenge.get().getJSON();
                    log.info("Dns01Challenge 结果,{} ", json.toString());
                    if (currentStatus == Status.PENDING) {
                        challenge.get().trigger();
                        System.out.println("已触发 DNS 验证请求...");
                    } else if (currentStatus == Status.PROCESSING) {
                        System.out.println("验证正在处理中，跳过 trigger...");
                    } else if (currentStatus == Status.VALID) {
                        System.out.println("验证已经通过，无需重复触发...");
                    } else if (currentStatus == Status.INVALID) {
                        info.setStatus(INVALID);
                        infoService.updateById(info);
                        throw new CustomException("挑战已失效 (INVALID)，请尝试重新申请证书");
                    }

                }
            }
            // 4. 轮询状态
            int attempts = 20;
            while (order.getStatus() != Status.READY && attempts > 0) {
                Thread.sleep(5000);
                order.fetch();
                if (order.getStatus() == Status.INVALID) {
                    info.setStatus(INVALID);
                    infoService.updateById(info);
                    throw new CustomException("订单状态异常变为 INVALID");
                }
                attempts--;
            }

            if (order.getStatus() == Status.READY) {
                // 5. 提交 CSR
                KeyPair domainKeyPair = KeyPairConvertUtil.stringToKeyPair(info.getDomainPrivateKey());
                CSRBuilder csr = new CSRBuilder();
                csr.addDomain(info.getDomain());
                csr.sign(domainKeyPair);
                order.execute(csr.getEncoded());
                while (order.getStatus() != Status.VALID) {
                    Thread.sleep(2000);
                    order.fetch();
                }
                Certificate certificate = order.getCertificate();
                StringWriter sw = new StringWriter();
                certificate.writeCertificate(sw);
                info.setCertificateContent(sw.toString());
                info.setStatus(VALID);
                X509Certificate x509 = certificate.getCertificate();
                Date startDate = x509.getNotBefore();
                Date expiryDate = x509.getNotAfter();
                info.setIssueDate(startDate);
                info.setExpiryDate(expiryDate);
                infoService.updateById(info);
            }
        } catch (Exception e) {
            info.setStatus(ERROR);
            info.setErrorMessage(e.getMessage());
            infoService.updateById(info);
        }
    }

    @Override
    public ResponseEntity<byte[]> download(Long id) throws Exception {
        AcmeCertificateInfoEntity info = infoService.getById(id);
        String fileName = (info != null ? info.getDomain() : "certificate") + "_bundle.zip";
        byte[] zipBytes = downloadCertificateZip(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(zipBytes);
    }

    @Override
    public ResponseEntity<ByteArrayResource> downloadLets(Long id, HttpServletRequest request) throws IOException {
        return null;
    }

    private CertChallengeDTO buildDTOFromExisting(AcmeCertificateInfoEntity info) {
        return CertChallengeDTO.builder()
                .id(info.getId())
                .domain(info.getDomain())
                .hostRecord(info.getHostRecord())
                .recordValue(info.getRecordValue())
                .build();
    }

    public byte[] downloadCertificateZip(Long id) throws Exception {
        AcmeCertificateInfoEntity info = infoService.getById(id);
        if (info == null || !"VALID".equals(info.getStatus())) {
            throw new CustomException("证书不存在或尚未签发成功");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            String domain = info.getDomain();
            addToZip(zos, domain + ".key", info.getDomainPrivateKey());
            addToZip(zos, domain + ".crt", info.getCertificateContent());
            String readme = "证书说明：\n1. " + domain + ".key 为私钥文件，请妥善保管。\n"
                    + "2. " + domain + ".crt 为完整证书链文件，Nginx 配置时使用此文件。";
            addToZip(zos, "README.txt", readme);
        }
        return baos.toByteArray();
    }

    private void addToZip(ZipOutputStream zos, String fileName, String content) throws Exception {
        if (content == null) {
            return;
        }
        ZipEntry entry = new ZipEntry(fileName);
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
