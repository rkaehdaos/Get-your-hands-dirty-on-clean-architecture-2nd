package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.List;

public class ApplicationLayer extends ArchitectureElement {

    private List<String> incomingPortsPackages = new ArrayList<>();
    private List<String> outgoingPortsPackages = new ArrayList<>();
    private List<String> servicePackages = new ArrayList<>();


    public ApplicationLayer(String basePackage) {
        super(basePackage);
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

    public void doesNotDependOn(String packageName, JavaClasses classes) {
        denyDependency(this.basePackage, packageName, classes);
    }

    public void incomingAndOutgoingPortsDoNotDependOnEachOther(JavaClasses classes) {
        denyAnyDependency(this.incomingPortsPackages, this.outgoingPortsPackages, classes);
        denyAnyDependency(this.outgoingPortsPackages, this.incomingPortsPackages, classes);
    }

    private List<String> allPackages() {
        List<String> allPackages = new ArrayList<>();
        allPackages.addAll(incomingPortsPackages);
        allPackages.addAll(outgoingPortsPackages);
        allPackages.addAll(servicePackages);
        return allPackages;
    }
}
