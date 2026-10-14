package com.g7.crowdfunding.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.g7.crowdfunding", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..",
                    "com.fasterxml.jackson..", "..application..", "..infrastructure..")
            .because("the domain model must be pure Java");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "org.springframework..", "jakarta.persistence..",
                    "org.hibernate..")
            .because("use cases talk to the outside world only through ports");

    @ArchTest
    static final ArchRule queriesDoNotUseWriteRepositories = noClasses()
            .that().resideInAPackage("..application.query..")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("com.g7.crowdfunding.campaign.application.port.out.CampaignRepository")
            .orShould().dependOnClassesThat()
            .haveFullyQualifiedName("com.g7.crowdfunding.contribution.application.port.out.ContributionRepository")
            .orShould().dependOnClassesThat().resideInAPackage("..application.command..")
            .because("CQRS: the query side reads only from read-model ports");
}
