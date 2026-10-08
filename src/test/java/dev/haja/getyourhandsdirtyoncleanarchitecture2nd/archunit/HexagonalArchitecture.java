package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HexagonalArchitecture extends ArchitectureElement {

    private Adapters adapters;
    private ApplicationLayer applicationLayer;
    private String configurationPackage;
    private List<String> domainPackages = new ArrayList<>();

    public static HexagonalArchitecture basePackage(String basePackage) {
        return new HexagonalArchitecture(basePackage);
    }

    public HexagonalArchitecture(String basePackage) {
        super(basePackage);
    }

    public Adapters withAdaptersLayer(String adaptersPackage) {
        this.adapters = new Adapters(this, fullQualifiedPackage(adaptersPackage));
        return this.adapters;
    }

    public HexagonalArchitecture withDomainLayer(String domainPackage) {
        this.domainPackages.add(fullQualifiedPackage(domainPackage));
        return this;
    }

    public ApplicationLayer withApplicationLayer(String applicationPackage) {
        this.applicationLayer = new ApplicationLayer(fullQualifiedPackage(applicationPackage),
            this);
        return this.applicationLayer;
    }

    public HexagonalArchitecture withConfiguration(String packageName) {
        this.configurationPackage = fullQualifiedPackage(packageName);
        return this;
    }

    private void domainDoesNotDependOnAdapters(JavaClasses classes) {
        denyAnyDependency(
            this.domainPackages, Collections.singletonList(adapters.basePackage), classes);
    }

    // 등록하지 않은 계층이 있으면 그 계층의 규칙이 아무것도 검사하지 않고 통과하거나
    // "null.." 패턴·NPE로 엉뚱하게 실패한다. 검사 전에 빠진 등록을 이름으로 알린다.
    private void requireAllLayersRegistered() {
        List<String> missing = new ArrayList<>();
        if (this.domainPackages.isEmpty()) {
            missing.add("withDomainLayer()");
        }
        if (this.adapters == null) {
            missing.add("withAdaptersLayer()");
        }
        if (this.applicationLayer == null) {
            missing.add("withApplicationLayer()");
        }
        if (this.configurationPackage == null) {
            missing.add("withConfiguration()");
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("등록하지 않은 계층이 있다: " + missing);
        }
    }

    public void check(JavaClasses classes) {
        requireAllLayersRegistered();
        denyEmptyPackage(this.configurationPackage, classes);
        this.adapters.doesNotContainEmptyPackages(classes);
        this.adapters.dontDependOnEachOther(classes);
        this.adapters.doesNotDependOn(this.configurationPackage, classes);
        this.applicationLayer.doesNotContainEmptyPackages(classes);
        this.applicationLayer.doesNotDependOn(this.adapters.getBasePackage(), classes);
        this.applicationLayer.doesNotDependOn(this.configurationPackage, classes);
        this.applicationLayer.incomingAndOutgoingPortsDoNotDependOnEachOther(classes);
        this.domainDoesNotDependOnAdapters(classes);
    }

}
