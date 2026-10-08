package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static lombok.AccessLevel.PROTECTED;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PROTECTED)
abstract class ArchitectureElement {

    final String basePackage;

    String fullQualifiedPackage(String relativePackage) {
        return this.basePackage + "." + relativePackage;
    }

    static void denyDependency(String fromPackageName, String toPackageName, JavaClasses classes) {
        noClasses()
            .that()
            .resideInAPackage(matchAllClassesInPackage(fromPackageName))
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(matchAllClassesInPackage(toPackageName))
            .check(classes);
    }

    static void denyAnyDependency(
        List<String> fromPackages, List<String> toPackages, JavaClasses classes) {
        for (String fromPackage : fromPackages) {
            for (String toPackage : toPackages) {
                denyDependency(fromPackage, toPackage, classes);
            }
        }
    }

    private static String matchAllClassesInPackage(String packageName) {
        return packageName + "..";
    }

    private JavaClasses classesInPackage(String packageName) {
        return new ClassFileImporter().importPackages(packageName);
    }

}
