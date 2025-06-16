package ch.so.agi.modelfinder;

public record ModelMetadata(
        String serverUrl,
        String name,
        String dispName,
        String shortDescription,
        String version,
        String file,
        String schemaLanguage,
        String issuer,
        String precursorVersion,
        String technicalContact,
        String furtherInformation,
        String md5,
        String tags,
        String idGeoIV,
        String organisationName,
        String organisationAbbreviation,
        String modelContent
        ) {}
