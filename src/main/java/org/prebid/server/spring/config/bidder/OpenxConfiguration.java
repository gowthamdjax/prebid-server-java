package org.prebid.server.spring.config.bidder;

import org.prebid.server.bidder.BidderDeps;
import org.prebid.server.bidder.Usersyncer;
import org.prebid.server.bidder.openx.OpenxBidder;
import org.prebid.server.proto.response.BidderInfo;
import org.prebid.server.spring.config.bidder.model.BidderConfigurationProperties;
import org.prebid.server.spring.config.bidder.model.MetaInfo;
import org.prebid.server.spring.config.bidder.model.UsersyncConfigurationProperties;
import org.prebid.server.spring.env.YamlPropertySourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.validation.constraints.NotBlank;

@Configuration
@PropertySource(value = "classpath:/bidder-config/openx.yaml", factory = YamlPropertySourceFactory.class)
public class OpenxConfiguration {

    private static final String BIDDER_NAME = "openx";

    @Value("${external-url}")
    @NotBlank
    private String externalUrl;

    @Autowired
    @Qualifier("openxConfigurationProperties")
    private BidderConfigurationProperties configProperties;

    @Bean("openxConfigurationProperties")
    @ConfigurationProperties("adapters.openx")
    BidderConfigurationProperties openxProperties() {
        return new BidderConfigurationProperties();
    }

    @Bean
    BidderDeps openxBidderDeps() {
        final MetaInfo metaInfo = configProperties.getMetaInfo();
        final BidderInfo bidderInfo = BidderInfo.create(configProperties.getEnabled(), metaInfo.getMaintainerEmail(),
                metaInfo.getAppMediaTypes(), metaInfo.getSiteMediaTypes(), metaInfo.getSupportedVendors(),
                metaInfo.getVendorId(), configProperties.getPbsEnforcesGdpr());

        final UsersyncConfigurationProperties usersync = configProperties.getUsersync();

        return BidderDepsAssembler.forBidder(BIDDER_NAME)
                .withConfig(configProperties)
                .bidderInfo(bidderInfo)
                .usersyncerCreator(() -> new Usersyncer(usersync.getCookieFamilyName(), usersync.getUrl(),
                        usersync.getRedirectUrl(), externalUrl, usersync.getType(), usersync.getSupportCors()))
                .bidderCreator(() -> new OpenxBidder(configProperties.getEndpoint()))
                .assemble();
    }
}
