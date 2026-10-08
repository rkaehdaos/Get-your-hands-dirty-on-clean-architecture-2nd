package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.archunit;

import java.util.ArrayList;
import java.util.List;

public class HexagonalArchitecture extends ArchitectureElement {

    private List<String> domainPackages = new ArrayList<>();

    public static HexagonalArchitecture basePackage(String basePackage) {
        return new HexagonalArchitecture(basePackage);
    }

    public HexagonalArchitecture(String basePackage) {
        super(basePackage);
    }

    public HexagonalArchitecture withDomainLayer(String domainPackage) {
        this.domainPackages.add(fullQualifiedPackage(domainPackage));
        return this;
    }
}
