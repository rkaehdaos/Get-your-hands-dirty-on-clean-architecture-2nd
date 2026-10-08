package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.archunit;

import java.util.ArrayList;
import java.util.List;

public class Adapters extends ArchitectureElement {

    private List<String> incomingAdapterPackages = new ArrayList<>();
    private List<String> outgoingAdapterPackages = new ArrayList<>();

    Adapters(String basePackage) {
        super(basePackage);
    }

    public Adapters outgoing(String packageName) {
        this.outgoingAdapterPackages.add(fullQualifiedPackage(packageName));
        return this;
    }

    public Adapters incoming(String packageName) {
        this.incomingAdapterPackages.add(fullQualifiedPackage(packageName));
        return this;
    }

    List<String> allAdapterPackages() {
        List<String> allAdapters = new ArrayList<>();
        allAdapters.addAll(incomingAdapterPackages);
        allAdapters.addAll(outgoingAdapterPackages);
        return allAdapters;
    }

    String getBasePackage() {
        return basePackage;
    }
}
