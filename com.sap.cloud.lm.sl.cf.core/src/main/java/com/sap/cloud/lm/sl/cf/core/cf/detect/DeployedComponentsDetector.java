package com.sap.cloud.lm.sl.cf.core.cf.detect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Set;
import java.util.stream.Collectors;

import org.cloudfoundry.client.lib.domain.CloudApplication;

import com.sap.cloud.lm.sl.cf.core.model.ApplicationMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.model.DeployedComponents;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;

public class DeployedComponentsDetector {

    /**
     * Detects all deployed components on this platform.
     * 
     */
    public DeployedComponents detectAllDeployedComponents(Collection<CloudApplication> apps) {
        Map<DeployedMtaMetadata, Set<String>> servicesMap = new HashMap<>();
        Map<DeployedMtaMetadata, List<DeployedMtaModule>> modulesMap = new HashMap<>();
        List<String> standaloneApps = new ArrayList<>();

        for (CloudApplication app : apps) {
            String appName = app.getName();

            ApplicationMtaMetadata appMetadata = ApplicationMtaMetadataParser.parseAppMetadata(app);

            if (appMetadata != null) {
                // This application is an MTA module.
                String moduleName = (appMetadata.getModuleName() != null) ? appMetadata.getModuleName() : appName;
                List<String> providedDependencies = (appMetadata.getProvidedDependencyNames() != null)
                    ? appMetadata.getProvidedDependencyNames()
                    : new ArrayList<>();
                List<String> appServices = (appMetadata.getServices() != null) ? appMetadata.getServices() : new ArrayList<>();

                DeployedMtaMetadata mtaMetadata = appMetadata.getMtaMetadata();

                List<DeployedMtaModule> modules = modulesMap.getOrDefault(mtaMetadata, new ArrayList<>());
                Date createdOn = app.getMetadata()
                                    .getCreatedAt();
                Date updatedOn = app.getMetadata()
                                    .getUpdatedAt();
                DeployedMtaModule module = new DeployedMtaModule(moduleName,
                                                                 appName,
                                                                 createdOn,
                                                                 updatedOn,
                                                                 appServices,
                                                                 providedDependencies,
                                                                 app.getUris());
                modules.add(module);
                modulesMap.put(mtaMetadata, modules);

                Set<String> services = servicesMap.getOrDefault(mtaMetadata, new HashSet<>());
                services.addAll(appServices);
                servicesMap.put(mtaMetadata, services);
            } else {
                // This is a standalone application.
                standaloneApps.add(appName);
            }
        }

        return createComponents(modulesMap, servicesMap, standaloneApps);
    }

    private DeployedComponents createComponents(Map<DeployedMtaMetadata, List<DeployedMtaModule>> modulesMap,
                                                Map<DeployedMtaMetadata, Set<String>> servicesMap, List<String> standaloneApps) {
        List<DeployedMta> mtas = modulesMap.entrySet()
                                           .stream()
                                           .map(entry -> createDeployedMta(entry, servicesMap))
                                           .collect(Collectors.collectingAndThen(Collectors.toList(),
                                                                                 this::mergeDifferentVersionsOfMtasWithSameId));
        return new DeployedComponents(mtas, standaloneApps);
    }

    private DeployedMta createDeployedMta(Entry<DeployedMtaMetadata, List<DeployedMtaModule>> entry,
                                          Map<DeployedMtaMetadata, Set<String>> servicesMap) {
        List<DeployedMtaModule> modules = entry.getValue();
        DeployedMtaMetadata mtaMetadata = entry.getKey();
        return new DeployedMta(mtaMetadata, modules, servicesMap.get(mtaMetadata));
    }

    private List<DeployedMta> mergeDifferentVersionsOfMtasWithSameId(List<DeployedMta> mtas) {
        Set<DeployedMtaMetadata> uniqueMtasMetadata = getUniqueMtasMetadata(mtas);
        List<DeployedMta> result = new ArrayList<>();
        for (DeployedMtaMetadata metadata : uniqueMtasMetadata) {
            List<DeployedMta> mtasWithSameId = getMtasWithSameId(mtas, metadata.getQualifiedId());
            if (mtasWithSameId.size() > 1) {
                result.add(mergeMtas(metadata, mtasWithSameId));
            } else {
                result.add(mtasWithSameId.get(0));
            }
        }
        return result;
    }

    private Set<DeployedMtaMetadata> getUniqueMtasMetadata(List<DeployedMta> mtas) {
        return mtas.stream()
                   .map(mta -> mta.getMetadata())
                   .filter(distinctByKey(DeployedMtaMetadata::getQualifiedId))
                   .map(metadata -> new DeployedMtaMetadata(metadata.getId(), metadata.getNamespace()))
                   .collect(Collectors.toSet());
    }
    
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private List<DeployedMta> getMtasWithSameId(List<DeployedMta> mtas, String qualifiedId) {
        return mtas.stream()
                   .filter(mta -> mta.getMetadata()
                                     .getQualifiedId()
                                     .equals(qualifiedId))
                   .collect(Collectors.toList());
    }

    private DeployedMta mergeMtas(DeployedMtaMetadata metadata, List<DeployedMta> mtas) {
        List<DeployedMtaModule> modules = new ArrayList<>();
        Set<String> services = new HashSet<>();
        for (DeployedMta mta : mtas) {
            services.addAll(mta.getServices());
            modules.addAll(mta.getModules());
        }
        return new DeployedMta(metadata, modules, services);
    }

}
