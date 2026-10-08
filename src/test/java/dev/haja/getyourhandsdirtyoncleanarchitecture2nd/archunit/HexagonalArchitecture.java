package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.archunit;

public class HexagonalArchitecture extends ArchitectureElement {

    public static HexagonalArchitecture basePackage(String basePackage) {
        return new HexagonalArchitecture(basePackage);
    }

    public HexagonalArchitecture(String basePackage) {
        super(basePackage);
    }
}
