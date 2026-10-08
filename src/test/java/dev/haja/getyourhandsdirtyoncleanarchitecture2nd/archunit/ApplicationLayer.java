package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.archunit;

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
}
