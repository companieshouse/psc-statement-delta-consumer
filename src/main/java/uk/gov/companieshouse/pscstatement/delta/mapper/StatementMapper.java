package uk.gov.companieshouse.pscstatement.delta.mapper;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import uk.gov.companieshouse.GenerateEtagUtil;
import uk.gov.companieshouse.api.delta.LinkedPsc;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.api.psc.Statement.KindEnum;
import uk.gov.companieshouse.api.psc.StatementLinksType;

@Mapper(componentModel = "spring")
public interface StatementMapper {

    MapperUtils mapperUtils = new MapperUtils();
    String SET_KIND = "setKind";
    String SET_STATEMENT = "setStatement";
    String SET_DESCRIPTION = "setDescription";

    @Mapping(target = "ceasedOn", source = "ceasedOn", dateFormat = "yyyyMMdd")
    @Mapping(target = "kind", source = "statement", ignore = true)
    @Mapping(target = "etag", source = "statement", ignore = true)
    @Mapping(target = "links", source = "companyNumber", ignore = true)
    @Mapping(target = "linkedPscName", source = "linkedPsc.surname", ignore = true)
    @Mapping(target = "notifiedOn", source = "submittedOn", dateFormat = "yyyyMMdd")
    @Mapping(target = "restrictionsNoticeWithdrawalReason", source = "restrictionsNoticeReason")
    @Mapping(target = "notificationId", source = "linkedPsc.notificationId", ignore = true)
    @Mapping(target = "statement", source = "statement")
    Statement pscStatementToStatement(PscStatement pscStatement);

    @AfterMapping
    default void mapEtag(@MappingTarget Statement target) {
        target.setEtag(GenerateEtagUtil.generateEtag());
    }

    /** Manually map linkedPsc. */

    @AfterMapping 
    default void mapLinks(@MappingTarget Statement target, PscStatement source) {
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
    default void mapLinkedPscNameAndId(@MappingTarget Statement target, PscStatement source) {
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
    default void mapEnums(@MappingTarget Statement target, PscStatement source) {
        target.setKind(KindEnum.valueOf("PERSONS_WITH_SIGNIFICANT_CONTROL_STATEMENT"));  
    }

}
