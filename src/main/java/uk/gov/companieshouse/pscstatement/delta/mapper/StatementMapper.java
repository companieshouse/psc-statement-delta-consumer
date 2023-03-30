package uk.gov.companieshouse.pscstatement.delta.mapper;

import org.apache.commons.lang.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.delta.LinkedPsc;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.Statement.KindEnum;
import uk.gov.companieshouse.api.psc.StatementLinksType;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public abstract class StatementMapper {

    @Autowired
    protected MapperUtils mapperUtils;
    String SET_KIND = "setKind";
    String SET_STATEMENT = "setStatement";
    String SET_DESCRIPTION = "setDescription";

    @Mapping(target = "ceasedOn", source = "ceasedOn", dateFormat = "yyyyMMdd")
    @Mapping(target = "kind", source = "statement", ignore = true)
    @Mapping(target = "etag", source = "statement", ignore = true)
    @Mapping(target = "links", source = "companyNumber", ignore = true)
    @Mapping(target = "linkedPscName", source = "linkedPsc.surname", ignore = true)
    @Mapping(target = "notifiedOn", source = "submittedOn", dateFormat = "yyyyMMdd")
    @Mapping(target = "notificationId", source = "linkedPsc.notificationId", ignore = true)
    @Mapping(target = "statement", source = "statement")
    public abstract Statement pscStatementToStatement(PscStatement pscStatement);

    @AfterMapping
    public void mapEtag(@MappingTarget Statement target) {
        target.setEtag(GenerateEtagUtil.generateEtag());
    }

    /** Manually map restrictions notice withdrawal reasons. */

    @AfterMapping
    public void mapRestrictionsNoticeWithdrawalReason(@MappingTarget Statement target, PscStatement source) {
        if (!StringUtils.isBlank(source.getRestrictionsNoticeReason())) {
            Map<String, String> restrictionsNoticeWithdrawalReasons = Map.ofEntries(
                    Map.entry("1", "restrictions-notice-withdrawn-by-company"),
                    Map.entry("2", "restrictions-notice-withdrawn-by-court-order"),
                    Map.entry("3", "restrictions-notice-withdrawn-by-lp"),
                    Map.entry("4", "restrictions-notice-withdrawn-by-court-order-lp"),
                    Map.entry("5", "restrictions-notice-withdrawn-by-partnership"),
                    Map.entry("6", "restrictions-notice-withdrawn-by-court-order-p")
            );
            target.setRestrictionsNoticeWithdrawalReason(
                    restrictionsNoticeWithdrawalReasons.get(source.getRestrictionsNoticeReason()));
        }
    }

    /** Manually map linkedPsc. */

    @AfterMapping 
    public void mapLinks(@MappingTarget Statement target, PscStatement source) {
        String encodedId = mapperUtils.encode(source.getPscStatementId());
        StatementLinksType links = new StatementLinksType();
        links.setSelf(String
                .format("/company/%s/persons-with-significant-control-statements/%s", 
                source.getCompanyNumber(), 
                encodedId)); 

        if (source.getLinkedPsc() != null) {
            String encodedNotificationId = mapperUtils
                    .encode(source.getLinkedPsc().getNotificationId());
            links.setPersonWithSignificantControl(String
                    .format("/company/%s/persons-with-significant-control/%s/%s", 
                    source.getCompanyNumber(), 
                    source.getLinkedPsc().getPscKind(), 
                    encodedNotificationId));
        }
        target.setLinks(links);
    }

    /** Manually map name and id. */

    @AfterMapping 
    public void mapLinkedPscNameAndId(@MappingTarget Statement target, PscStatement source) {
        LinkedPsc linkedPsc = source.getLinkedPsc();
        if (linkedPsc != null) {
            String fullName = Stream
                    .of(linkedPsc.getTitle(), 
                            linkedPsc.getForename(), 
                            linkedPsc.getMiddleName(),
                            linkedPsc.getSurname(), 
                            linkedPsc.getHonours())
                    .filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining(" "));
            target.setLinkedPscName(fullName);
            target.setNotificationId(mapperUtils.encode(linkedPsc.getNotificationId()));
        }
    }

    @AfterMapping 
    public void mapEnums(@MappingTarget Statement target, PscStatement source) {
        target.setKind(KindEnum.valueOf("PERSONS_WITH_SIGNIFICANT_CONTROL_STATEMENT"));  
    }

}
