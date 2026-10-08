package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

abstract class ArchitectureElement {
  final String basePackage;

  public ArchitectureElement(String basePackage) {
    this.basePackage = basePackage;
  }

  String fullQualifiedPackage(String relativePackage) {
    return this.basePackage + "." + relativePackage;
  }

  private static String matchAllClassesInPackage(String packageName) {
    return packageName + "..";
  }
}
