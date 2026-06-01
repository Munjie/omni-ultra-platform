package com.munjie.omni.utils;


import com.google.common.net.InternetDomainName;
import com.munjie.omni.constant.AcmeConstants;
import com.munjie.omni.exception.CustomException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DnsUtil {

    public static String calculateHostRecord(String domain) {
        try {
            String cleanDomain = domain.startsWith("*.") ? domain.substring(2) : domain;
            InternetDomainName fullDomain = InternetDomainName.from(cleanDomain);
            String rootDomain = fullDomain.topPrivateDomain().toString();
            String acmePrefix = AcmeConstants.DNS_CHALLENGE_PREFIX;
            if (cleanDomain.equalsIgnoreCase(rootDomain)) {
                return acmePrefix;
            }
            String subPart = cleanDomain.substring(0, cleanDomain.length() - rootDomain.length() - 1);
            return acmePrefix + "." + subPart;
        }catch (Exception e){
            log.error("域名计算异常",e);
            throw new CustomException("请检查申请域名是否合法");
        }
    }
}
