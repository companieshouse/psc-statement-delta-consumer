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
import uk.gov.companieshouse.api.psc.StatementLinksType;

@Mapper(componentModel = "spring")
public interface StatementMapper {

    @Mapping(target = "etag", source = "statement", ignore=true)
    @Mapping(target = "kind", source = "statement")
    @Mapping(target = "linkedPscName", source = "linkedPsc.surname", ignore=true)
    @Mapping(target = "notificationId", source = "linkedPsc.notificationId", ignore=true)
    @Mapping(target = "links", source = "companyNumber", ignore=true)
    @Mapping(target = "notifiedOn", source = "submittedOn")
    @Mapping(target = "ceasedOn", source = "ceasedOn")
    @Mapping(target = "restrictionsNoticeWithdrawalReason", source = "statement")//unsure on source here. Think the CHIPS objects may be missing this property?
    Statement pscStatementToStatement(PscStatement pscStatement);

    /**
    * Invoked at the end of the auto-generated mapping methods.
    * @param target        the target object
    * @param source        the source object
    */
    @AfterMapping
    default void mapLinks(@MappingTarget Statement target, PscStatement source) {
        
        String encodedId = MapperUtils.encode(source.getPscStatementId());
        StatementLinksType links = new StatementLinksType();
        links.setSelf(encodedId); //needs to be set to correct self format

        if (source.getLinkedPsc() != null) {
            String encodedNotificationId = MapperUtils.encode(source.getLinkedPsc().getNotificationId());
            links.setPersonWithSignificantControl(String.format("links/personwithcontrol/%s/%s", source.getLinkedPsc().getPscKind(), encodedNotificationId)); //needs correcting
        }
        target.setLinks(links);
    }

    /**
    * Invoked at the end of the auto-generated mapping methods.
    * @param target        the target object
    */
    @AfterMapping
    default void mapEtag(@MappingTarget Statement target) {
        target.setEtag(GenerateEtagUtil.generateEtag());
    }

    /**
    * Invoked at the end of the auto-generated mapping methods.
    * @param target        the target object
    * @param source        the source object
    */
    @AfterMapping
    default void mapLinkedPscNameId(@MappingTarget Statement target, PscStatement source) {
        LinkedPsc linkedPsc = source.getLinkedPsc();
        if (linkedPsc != null) {
            String joinedNames = Stream.of(
                linkedPsc.getTitle(),
                linkedPsc.getForename(),
                linkedPsc.getMiddleName(),
                linkedPsc.getSurname(),
                linkedPsc.getHonours()
            ).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining(" "));
            target.setLinkedPscName(joinedNames);
            target.setNotificationId(MapperUtils.encode(linkedPsc.getNotificationId()));
        }
    }
}
