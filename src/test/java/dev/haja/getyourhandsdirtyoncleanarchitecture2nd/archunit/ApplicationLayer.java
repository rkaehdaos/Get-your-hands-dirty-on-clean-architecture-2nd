package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.List;

public class ApplicationLayer extends ArchitectureElement {

    private final HexagonalArchitecture parentContext;
    final List<String> incomingPortsPackages = new ArrayList<>();
    final List<String> outgoingPortsPackages = new ArrayList<>();
    private List<String> servicePackages = new ArrayList<>();


    ApplicationLayer(String basePackage, HexagonalArchitecture parentContext) {
        super(basePackage);
        this.parentContext = parentContext;
    }

    public ApplicationLayer incomingPorts(String packageName) {
        this.incomingPortsPackages.add(fullQualifiedPackage(packageName));
        return this;
    }

    public ApplicationLayer outgoingPorts(String packageName) {
        this.outgoingPortsPackages.add(fullQualifiedPackage(packageName));
        return this;
    }

    public ApplicationLayer services(String packageName) {
        this.servicePackages.add(fullQualifiedPackage(packageName));
        return this;
    }

    public HexagonalArchitecture and() { return parentContext; }

    public void doesNotDependOn(String packageName, JavaClasses classes) {
        denyDependency(this.basePackage, packageName, classes);
    }

    public void incomingAndOutgoingPortsDoNotDependOnEachOther(JavaClasses classes) {
        denyAnyDependency(this.incomingPortsPackages, this.outgoingPortsPackages, classes);
        denyAnyDependency(this.outgoingPortsPackages, this.incomingPortsPackages, classes);
    }

    void doesNotContainEmptyPackages(JavaClasses classes) {
        denyEmptyPackages(allPackages(), classes);
    }

    private List<String> allPackages() {
        List<String> allPackages = new ArrayList<>();
        allPackages.addAll(incomingPortsPackages);
        allPackages.addAll(outgoingPortsPackages);
        allPackages.addAll(servicePackages);
        return allPackages;
    }

    List<String> missingRegistrations() {
        List<String> missing = new ArrayList<>();
        if (this.incomingPortsPackages.isEmpty()) {
            missing.add("withApplicationLayer().incomingPorts()");
        }
        if (this.outgoingPortsPackages.isEmpty()) {
            missing.add("withApplicationLayer().outgoingPorts()");
        }
        if (this.servicePackages.isEmpty()) {
            missing.add("withApplicationLayer().services()");
        }
        return missing;
    }
}
