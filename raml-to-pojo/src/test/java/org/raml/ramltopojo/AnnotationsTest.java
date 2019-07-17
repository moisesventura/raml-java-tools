package org.raml.ramltopojo;

import amf.client.model.domain.Shape;
import org.junit.Test;
import org.raml.testutils.UnitTest;
import webapi.Raml10;
import webapi.WebApiDocument;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created. There, you have it.
 */
public class AnnotationsTest extends UnitTest{


    @Test
    public void apiAnnotationsReading() throws Exception {

        WebApiDocument api = getApi();

        List<PluginDef> defs = Annotations.PLUGINS.get(api);
        assertEquals(2, defs.size());
        assertEquals("core.one", defs.get(0).getPluginName());
        assertEquals(Arrays.asList("foo", "bar"), defs.get(0).getArguments());
        assertEquals("core.two", defs.get(1).getPluginName());
        assertEquals(Arrays.asList("alpha", "gamma"), defs.get(1).getArguments());
    }

    @Test
    public void typeAnnotationsReading() throws Exception {

        WebApiDocument api = getApi();
        Shape fooType = RamlLoader.findShape("foo", api.declares());

        List<PluginDef> defs = Annotations.PLUGINS.get(Collections.<PluginDef>emptyList(), api, fooType);
        assertEquals(3, defs.size());
        assertEquals("core.one", defs.get(0).getPluginName());
        assertEquals(Arrays.asList("foo", "bar"), defs.get(0).getArguments());
        assertEquals("core.two", defs.get(1).getPluginName());
        assertEquals(Arrays.asList("alpha", "gamma"), defs.get(1).getArguments());
        assertEquals("core.foo", defs.get(2).getPluginName());
        assertEquals(Arrays.asList("foo", "bar"), defs.get(2).getArguments());
    }


    @Test
    public void abstractAnnotationsReading() throws Exception {

        WebApiDocument api = getApi();
        Shape fooType = RamlLoader.findShape("foo", api.declares());

        boolean b  = Annotations.ABSTRACT.get(fooType);
        assertEquals(true, b);
    }

    @Test
    public void simplerTypeAnnotationsReading() throws Exception {

        WebApiDocument api = getApi();
        Shape fooType = RamlLoader.findShape("too", api.declares());

        List<PluginDef> defs = Annotations.PLUGINS.get(fooType);
        assertEquals(2, defs.size());
        assertEquals("core.too", defs.get(0).getPluginName());
        assertEquals("core.moo", defs.get(1).getPluginName());
    }

    protected WebApiDocument getApi() throws Exception  {
        URL url = this.getClass().getResource("annotations.raml");
        return (WebApiDocument) Raml10.parse(url.toString()).get();
    }

}