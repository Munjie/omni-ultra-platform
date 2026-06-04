package com.munjie.omni.utils;


import com.munjie.omni.dto.CertDetailDTO;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CertParseUtils {

    /**
     * 解析 PEM 格式的证书，
     */
    public static CertDetailDTO parsePemCertificate(String pemContent) throws Exception {
        if (pemContent == null || !pemContent.contains("BEGIN CERTIFICATE")) {
            throw new IllegalArgumentException("无效的证书PEM内容");
        }

        // 1. 实例化证书工厂并生成 X509 证书对象
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(
                new ByteArrayInputStream(pemContent.getBytes(StandardCharsets.UTF_8))
        );
        CertDetailDTO dto = new CertDetailDTO();

        // 1. 提取 CA URL 和 OCSP URL (它们共同存在于 AIA 扩展域中)
        List<String> ocspUrls = getAiaUrls(cert, X509ObjectIdentifiers.id_ad_ocsp);
        List<String> caUrls = getAiaUrls(cert, X509ObjectIdentifiers.id_ad_caIssuers);

        dto.setOcspUrl(!ocspUrls.isEmpty() ? ocspUrls.get(0) : "-");
        dto.setCaUrl(!caUrls.isEmpty() ? caUrls.get(0) : "-");

        // 2. 提取 CRL 吊销列表 URL
        List<String> crlUrls = getCrlDistributionPoints(cert);
        dto.setCrlUrl(!crlUrls.isEmpty() ? crlUrls.get(0) : "-");


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 2. 基本信息与有效期
        dto.setDomain(getCommonName(cert.getSubjectX500Principal().getName()));
        dto.setStartDate(sdf.format(cert.getNotBefore()));
        dto.setExpiryDate(sdf.format(cert.getNotAfter()));
        
        // 3. 技术信息
        dto.setSerialNumber(cert.getSerialNumber().toString(16)); // 16进制序列号
        dto.setSignAlgorithm(cert.getSigAlgName());
        
        // 4. 密钥类型与强度 (以常见的 RSA 为例)
        if (cert.getPublicKey() instanceof RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) cert.getPublicKey();
            dto.setKeyType("RSA");
            dto.setKeyStrength(rsaPublicKey.getModulus().bitLength() + " bits");
        } else {
            dto.setKeyType(cert.getPublicKey().getAlgorithm());
            dto.setKeyStrength("Unknown");
        }

        // 5. 提取公钥 PEM 格式
        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getMimeEncoder().encodeToString(cert.getPublicKey().getEncoded()) +
                "\n-----END PUBLIC KEY-----";
        dto.setPublicKey(publicKeyPem);

        // 6. 利用 BouncyCastle 库精确提取签发者信息（Issuer）
        JcaX509CertificateHolder holder = new JcaX509CertificateHolder(cert);
        X500Name issuerX500Name = holder.getIssuer();
        
        dto.setIssuerCn(getDnAvaliable(issuerX500Name, BCStyle.CN));
        dto.setIssuerCountry(getDnAvaliable(issuerX500Name, BCStyle.C));
        dto.setIssuerProvince(getDnAvaliable(issuerX500Name, BCStyle.ST));
        dto.setIssuerCity(getDnAvaliable(issuerX500Name, BCStyle.L));
        dto.setIssuerOrg(getDnAvaliable(issuerX500Name, BCStyle.O));
        dto.setIssuerOu(getDnAvaliable(issuerX500Name, BCStyle.OU));

        // 7. 生成指纹 (SHA-1 和 SHA-256)
        dto.setSha1Fingerprint(getThumbprint(cert, "SHA-1"));
        dto.setSha256Fingerprint(getThumbprint(cert, "SHA-256"));

        // 8. 默认静态分类与扩展字段补充
        dto.setCertType("DV");
        dto.setCertCategory("服务器证书");
        dto.setKeyUsage("digitalSignature, keyEncipherment"); // 常见扩展定义

        return dto;
    }

    // 辅助方法：从 X500Name 提取指定标识的值
    private static String getDnAvaliable(X500Name x500Name, org.bouncycastle.asn1.ASN1ObjectIdentifier attribute) {
        if (x500Name.getRDNs(attribute).length > 0) {
            return IETFUtils.valueToString(x500Name.getRDNs(attribute)[0].getFirst().getValue());
        }
        return "-";
    }

    // 辅助方法：简单正则或切分获取 CN
    private static String getCommonName(String dn) {
        if (dn == null) return "-";
        for (String s : dn.split(",")) {
            if (s.trim().startsWith("CN=")) {
                return s.split("=")[1];
            }
        }
        return dn;
    }

    // 辅助方法：计算证书指纹
    private static String getThumbprint(X509Certificate cert, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] der = cert.getEncoded();
        byte[] digest = md.digest(der);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b)); // 转为16进制小写字符串
        }
        return sb.toString();
    }

    /**
     * 辅助方法：从 AIA (Authority Information Access) 中提取 OCSP 或 CA Issuers 的 URL
     */
    private static List<String> getAiaUrls(X509Certificate cert, ASN1ObjectIdentifier type) {
        List<String> urls = new ArrayList<>();
        try {
            // AIA 的扩展 OID 叫 "1.3.6.1.5.5.7.1.1"
            byte[] extValue = cert.getExtensionValue(Extension.authorityInfoAccess.getId());
            if (extValue == null) return urls;

            ASN1Primitive derObject = ASN1Primitive.fromByteArray(extValue);
            if (derObject instanceof DEROctetString) {
                byte[] aiaBytes = ((DEROctetString) derObject).getOctets();
                AuthorityInformationAccess aia = AuthorityInformationAccess.getInstance(ASN1Primitive.fromByteArray(aiaBytes));
                for (AccessDescription ad : aia.getAccessDescriptions()) {
                    if (ad.getAccessMethod().equals(type)) {
                        GeneralName gn = ad.getAccessLocation();
                        if (gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
                            urls.add(((DERIA5String) gn.getName()).getString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析 AIA (CA/OCSP) URL 失败: " + e.getMessage());
        }
        return urls;
    }

    /**
     * 辅助方法：从 CRL Distribution Points 中提取 CRL 吊销列表 URL
     */
    private static List<String> getCrlDistributionPoints(X509Certificate cert) {
        List<String> urls = new ArrayList<>();
        try {
            // CRL 的扩展 OID 叫 "2.5.29.31"
            byte[] extValue = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
            if (extValue == null) return urls;

            ASN1Primitive derObject = ASN1Primitive.fromByteArray(extValue);
            if (derObject instanceof DEROctetString) {
                byte[] crlBytes = ((DEROctetString) derObject).getOctets();
                CRLDistPoint distPoint = CRLDistPoint.getInstance(ASN1Primitive.fromByteArray(crlBytes));
                for (DistributionPoint dp : distPoint.getDistributionPoints()) {
                    DistributionPointName dpn = dp.getDistributionPoint();
                    if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                        GeneralNames gns = GeneralNames.getInstance(dpn.getName());
                        for (GeneralName gn : gns.getNames()) {
                            if (gn.getTagNo() == GeneralName.uniformResourceIdentifier) {
                                urls.add(((DERIA5String) gn.getName()).getString());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析 CRL URL 失败: " + e.getMessage());
        }
        return urls;
    }
}