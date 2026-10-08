package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import static lombok.AccessLevel.PROTECTED;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PROTECTED)
abstract class ArchitectureElement {

    final String basePackage;

    String fullQualifiedPackage(String relativePackage) {
        return this.basePackage + "." + relativePackage;
    }

    private static String matchAllClassesInPackage(String packageName) {
        return packageName + "..";
    }

    private JavaClasses classesInPackage(String packageName) {
        return new ClassFileImporter().importPackages(packageName);
    }

}
