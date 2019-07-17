package org.raml.ramltopojo;

import org.raml.ramltopojo.plugin.PluginManager;
import webapi.WebApiDocument;

import java.util.Collections;
import java.util.List;

/**
 * Created. There, you have it.
 */
public class RamlToPojoBuilder {


    private final WebApiDocument api;
    private String packageName = "";
    private TypeFetcher typeFetcher;
    private TypeFinder typeFinder;

    public RamlToPojoBuilder(WebApiDocument api) {

        this.api = api;
    }

    public static RamlToPojoBuilder builder(WebApiDocument api) {

        return new RamlToPojoBuilder(api);
    }

    public RamlToPojoBuilder inPackage(String packageName) {

        this.packageName = packageName;
        return this;
    }

    public RamlToPojoBuilder fetchTypes(TypeFetcher typeFetcher) {

        this.typeFetcher = typeFetcher;
        return this;
    }

    public RamlToPojoBuilder findTypes(TypeFinder typeFinder) {

        this.typeFinder = typeFinder;
        return this;
    }

    public RamlToPojo build() {

        return build(Collections.<String>emptyList());
    }

    public RamlToPojo build(List<String> basePlugins) {

        return new RamlToPojoImpl(typeFinder, new GenerationContextImpl(PluginManager.createPluginManager(), api, typeFetcher, packageName, basePlugins));
    }


}
