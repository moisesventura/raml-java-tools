package org.raml.ramltopojo;

import amf.client.model.domain.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.raml.ramltopojo.array.ArrayTypeHandler;
import org.raml.ramltopojo.enumeration.EnumerationTypeHandler;
import org.raml.ramltopojo.extensions.*;
import org.raml.ramltopojo.nulltype.NullTypeHandler;
import org.raml.ramltopojo.object.ObjectTypeHandler;
import org.raml.ramltopojo.references.ReferenceTypeHandler;
import org.raml.ramltopojo.union.UnionTypeHandler;
import org.raml.v2.api.model.v10.datamodel.*;
import webapi.WebApiDocument;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created. There, you have it.
 */
public enum TypeDeclarationType implements TypeHandlerFactory, TypeAnalyserFactory {

    /*
     private static Map<Class, Class<?>> ramlToType = ImmutableMap.<Class, Class<?>>builder()
      .put(IntegerTypeDeclaration.class, int.class)
      .put(BooleanTypeDeclaration.class, boolean.class)
      .put(DateTimeOnlyTypeDeclaration.class, Date.class)
      .put(TimeOnlyTypeDeclaration.class, Date.class)
      .put(DateTimeTypeDeclaration.class, Date.class).put(DateTypeDeclaration.class, Date.class)
      .put(NumberTypeDeclaration.class, BigDecimal.class)
      .put(StringTypeDeclaration.class, String.class).put(FileTypeDeclaration.class, File.class)
      .put(AnyTypeDeclaration.class, Object.class)
      .build();

  private static Map<String, Class<?>> stringScalarToType = ImmutableMap
      .<String, Class<?>>builder().put("integer", int.class).put("boolean", boolean.class)
      .put("date-time", Date.class).put("date", Date.class).put("number", BigDecimal.class)
      .put("string", String.class).put("file", File.class).build();

  // cheating: I know I only have one table for floats and ints, but the parser
  // should prevent problems.
*/

    NULL {
        @Override
        public boolean shouldCreateInlineType(Shape declaration) {

            return false;
        }

        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {
            return new NullTypeHandler(name, typeDeclaration);
        }
    },
    OBJECT {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {
            return new ObjectTypeHandler(name, null /* todo should fix */);
        }


        @Override
        public boolean shouldCreateInlineType(Shape declaration) {

            List<Shape> extended = declaration.inherits();

            if ( extended.size() > 1) {

                return true;
            }

            Set<String> allExtendedProps;

            // TODO certqinly we can do better here.
            if ( extended.size() == 1  && extended.get(0).name().equals("object")) {

                allExtendedProps = Collections.emptySet();
            } else {
                allExtendedProps =
                        extended.stream().filter(ObjectTypeDeclaration.class::isInstance).map(ObjectTypeDeclaration.class::cast)
                                .flatMap(TypeDeclarationType::pullNames).collect(Collectors.toSet());
            }

            Set<String> typePropertyNames = pullNames((ObjectTypeDeclaration) declaration).collect(Collectors.toSet());
            return !Sets.difference(typePropertyNames, allExtendedProps).isEmpty();
        }
    },
    ENUMERATION {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {

            return new EnumerationTypeHandler(name, typeDeclaration);
        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {
            return "string".equals(declaration.name().value()) || "number".equals(declaration.name().value()) || "integer".equals(declaration.name().value());
        }
    },
    ARRAY {
        @Override
        public TypeHandler createHandler(String name, final TypeDeclarationType type, final Shape typeDeclaration) {

            final ArrayShape arrayTypeDeclaration = (ArrayShape) typeDeclaration;

            return new ArrayTypeHandler(name, arrayTypeDeclaration);
        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {
            ArrayShape arrayTypeDeclaration = (ArrayShape) declaration;
            return Annotations.GENERATE_INLINE_ARRAY_TYPE.get(null); // TODO:  should be arrayTypeDeclaration
        }
    },
    UNION {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {

            return new UnionTypeHandler(name, (UnionShape) typeDeclaration);
        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {

            // this seems wrong.
            return declaration instanceof UnionShape;
        }
    },
    INTEGER {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {

            NumberTypeDeclaration integerTypeDeclaration = (NumberTypeDeclaration) typeDeclaration;
            if ( ! integerTypeDeclaration.enumValues().isEmpty() ) {
                return ENUMERATION.createHandler(name, type, typeDeclaration);
            } else {

                TypeName typeName = Optional.ofNullable(properType.get(integerTypeDeclaration.format())).orElse(TypeName.INT);
                return new ReferenceTypeHandler(typeDeclaration, Integer.class, typeName);
            }
        }

        @Override
        public boolean shouldCreateInlineType(Shape originalTypeDeclaration) {
            IntegerTypeDeclaration declaration = (IntegerTypeDeclaration) originalTypeDeclaration;

            if ( ! declaration.enumValues().isEmpty() ) {

                return ENUMERATION.shouldCreateInlineType(originalTypeDeclaration);
            } else {
                return false;
            }
        }
    },
    BOOLEAN {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {

            return new ReferenceTypeHandler(typeDeclaration, Boolean.class, TypeName.BOOLEAN);

        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {
            return false;
        }
    },
    DATE {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {

            return new ReferenceTypeHandler(typeDeclaration, Date.class, ClassName.get(Date.class));
        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {
            return false;
        }
    },
    DATETIME {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {
            return new ReferenceTypeHandler(typeDeclaration, Date.class, ClassName.get(Date.class));
        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {
            return false;
        }
    },
    TIME_ONLY {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {
            return new ReferenceTypeHandler(typeDeclaration, Date.class, ClassName.get(Date.class));
        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {
            return false;
        }
    },
    DATETIME_ONLY {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {
            return new ReferenceTypeHandler(typeDeclaration, Date.class, ClassName.get(Date.class));
        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {
            return false;
        }
    },
    NUMBER {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {

            NumberTypeDeclaration integerTypeDeclaration = (NumberTypeDeclaration) typeDeclaration;
            if ( ! integerTypeDeclaration.enumValues().isEmpty() ) {
                return ENUMERATION.createHandler(name, type, typeDeclaration);
            } else {

                TypeName typeName = Optional.ofNullable(properType.get(integerTypeDeclaration.format())).orElse(ClassName.get(Number.class));
                return new ReferenceTypeHandler(typeDeclaration, Number.class, typeName);
            }
        }

        @Override
        public boolean shouldCreateInlineType(Shape originalTypeDeclaration) {

            NumberTypeDeclaration declaration = (NumberTypeDeclaration) originalTypeDeclaration;

            if ( ! declaration.enumValues().isEmpty() ) {

                return ENUMERATION.shouldCreateInlineType(originalTypeDeclaration);
            } else {
                return false;
            }

        }
    },
    STRING {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {

            ScalarShape declaration = (ScalarShape) typeDeclaration;
            if ( false ) {
                return ENUMERATION.createHandler(name, type, typeDeclaration);
            } else {

                return new ReferenceTypeHandler(typeDeclaration, String.class, ClassName.get(String.class));
            }
        }

        @Override
        public boolean shouldCreateInlineType(Shape originalTypeDeclaration) {

            StringTypeDeclaration declaration = (StringTypeDeclaration) originalTypeDeclaration;

            if ( ! declaration.enumValues().isEmpty() ) {

                return ENUMERATION.shouldCreateInlineType(originalTypeDeclaration);
            } else {
                return false;
            }
        }
    },
    ANY {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {

            return new ReferenceTypeHandler(typeDeclaration, Object.class, ClassName.get(Object.class));
        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {
            return false;
        }
    },
    FILE {
        @Override
        public TypeHandler createHandler(String name, TypeDeclarationType type, Shape typeDeclaration) {
            return new ReferenceTypeHandler(typeDeclaration, File.class, ClassName.get(File.class));
        }

        @Override
        public boolean shouldCreateInlineType(Shape declaration) {
            return false;
        }
    };

    private static Stream<String> pullNames(ObjectTypeDeclaration extending) {

        return extending.properties().stream().map(TypeDeclaration::name);
    }

    private static Map<String, TypeName> properType = ImmutableMap.<String, TypeName>builder()
            .put("float", TypeName.FLOAT).put("double", TypeName.DOUBLE).put("int8", TypeName.BYTE)
            .put("int16", TypeName.SHORT).put("int32", TypeName.INT).put("int64", TypeName.LONG)
            .put("int", TypeName.INT).build();


    public abstract boolean shouldCreateInlineType(Shape declaration);

    private static Map<Class, TypeDeclarationType> ramlToType = ImmutableMap.<Class, TypeDeclarationType>builder()
            .put(NodeShape.class, OBJECT)
            .put(ArrayShape.class, ARRAY)
            .put(UnionShape.class, UNION)
//            .put(ScalarShape.class, DATETIME_ONLY)
//            .put(ScalarShape.class, INTEGER)
//            .put(ScalarShape.class, BOOLEAN)
//            .put(ScalarShape.class, TIME_ONLY)
//            .put(ScalarShape.class, DATETIME)
//            .put(ScalarShape.class, DATE)
//            .put(ScalarShape.class, NUMBER)
            .put(ScalarShape.class, STRING)
            .put(FileShape.class, FILE)
            .put(AnyShape.class, ANY)
            .put(NilShape.class, NULL)
            .build();

    /**
     * Create the actual type.
     *
     * @param typeDeclaration
     * @param context
     * @return
     */
    public static Optional<CreationResult> createType(Shape typeDeclaration, GenerationContext context) {

    /*    TypeDeclarationType typeDeclarationType = ramlToType.get(Utils.declarationType(typeDeclaration));

        TypeHandler handler = typeDeclarationType.createHandler(typeDeclaration.name(), typeDeclarationType, typeDeclaration);
        ClassName intf = handler.javaClassName(context, EventType.INTERFACE);
        ClassName impl = handler.javaClassName(context, EventType.IMPLEMENTATION);
        CreationResult creationResult = new CreationResult(context.defaultPackage(), intf, impl);
        context.newExpectedType(typeDeclaration.name(), creationResult);
        context.setupTypeHierarchy(typeDeclaration);
        return handler.create(context, creationResult);*/

        return Optional.empty();
    }

    /**
     * Create the actual type.
     *
     * @param typeDeclaration
     * @param context
     * @return
     */
    public static Optional<CreationResult> createNamedType(String name, TypeDeclaration typeDeclaration, GenerationContext context) {

/*
        TypeDeclarationType typeDeclarationType = ramlToType.get(Utils.declarationType(typeDeclaration));

        TypeHandler handler = typeDeclarationType.createHandler(name, typeDeclarationType, typeDeclaration);
        ClassName intf = handler.javaClassName(context, EventType.INTERFACE);
        ClassName impl = handler.javaClassName(context, EventType.IMPLEMENTATION);
        CreationResult creationResult = new CreationResult(context.defaultPackage(), intf, impl);
        context.newExpectedType(name, creationResult);
        context.setupTypeHierarchy(typeDeclaration);
        return handler.create(context, creationResult);
*/
        return Optional.empty();
    }

    /**
     * Create the actual type.
     *
     * @param typeDeclaration
     * @param context
     * @return
     */
    public static Optional<CreationResult> createInlineType(ClassName containingClassName, ClassName containingImplementation, String name, Shape typeDeclaration, final GenerationContext context) {

        TypeDeclarationType typeDeclarationType = ramlToType.get(null/*Utils.declarationType(typeDeclaration)*/);

        TypeHandler handler = typeDeclarationType.createHandler(name, typeDeclarationType, typeDeclaration);
        ClassName intf = handler.javaClassName(new InlineGenerationContext(containingClassName, containingClassName, context),  EventType.INTERFACE);
        ClassName impl = handler.javaClassName(new InlineGenerationContext(containingClassName, containingImplementation, context), EventType.IMPLEMENTATION);
        CreationResult preCreationResult = new CreationResult("", intf, impl);
        return handler.create(context, preCreationResult);
    }


    public static TypeName calculateTypeName(String name, Shape typeDeclaration, GenerationContext context, EventType eventType) {

        TypeDeclarationType typeDeclarationType = ramlToType.get(typeDeclaration.getClass());

        TypeHandler handler = typeDeclarationType.createHandler(name, typeDeclarationType, typeDeclaration);
        TypeName typeName = handler.javaClassReference(context, eventType);
        context.setupTypeHierarchy(typeDeclaration);
        return typeName;
    }

    public static boolean isNewInlineType(Shape declaration) {
        return ramlToType.get(Utils.declarationType(declaration)).shouldCreateInlineType(declaration);
    }

    private static class InlineGenerationContext implements GenerationContext {
        private final ClassName containingDeclaration;
        private final ClassName containingImplementation;
        private final GenerationContext context;

        public InlineGenerationContext(ClassName containingDeclaration, ClassName containingImplementation, GenerationContext context) {
            this.containingDeclaration = containingDeclaration;
            this.containingImplementation = containingImplementation;
            this.context = context;
        }

        @Override
        public void createSupportTypes(String rootDirectory) throws IOException {
            context.createSupportTypes(rootDirectory);
        }

        @Override
        public TypeName createSupportClass(TypeSpec.Builder newSupportType) {
            return context.createSupportClass(newSupportType);
        }

        @Override
        public CreationResult findCreatedType(String typeName, Shape ramlType) {
            return context.findCreatedType(typeName, ramlType);
        }

        @Override
        public String defaultPackage() {
            return "";
        }

        @Override
        public void newExpectedType(String name, CreationResult creationResult) {

        }

        @Override
        public void createTypes(String rootDirectory) throws IOException {

        }

        @Override
        public ObjectTypeHandlerPlugin pluginsForObjects(Shape... typeDeclarations) {
            return context.pluginsForObjects(typeDeclarations);
        }

        @Override
        public EnumerationTypeHandlerPlugin pluginsForEnumerations(Shape... typeDeclarations) {
            return context.pluginsForEnumerations(typeDeclarations);
        }

        @Override
        public UnionTypeHandlerPlugin pluginsForUnions(Shape... typeDeclarations) {
            return context.pluginsForUnions(typeDeclarations);
        }

        @Override
        public ArrayTypeHandlerPlugin pluginsForArrays(Shape... typeDeclarations) {
            return context.pluginsForArrays(typeDeclarations);
        }

        @Override
        public WebApiDocument api() {
            return context.api();
        }

        @Override
        public Set<String> childClasses(String ramlTypeName) {
            return context.childClasses(ramlTypeName);
        }

        @Override
        public ClassName buildDefaultClassName(String name, EventType eventType) {
            if ( eventType == EventType.INTERFACE ) {
                return containingDeclaration.nestedClass(name);
            } else {
                return containingImplementation.nestedClass(name);
            }
        }

        @Override
        public ReferenceTypeHandlerPlugin pluginsForReferences(Shape... typeDeclarations) {
            return context.pluginsForReferences(typeDeclarations);
        }

        @Override
        public void setupTypeHierarchy(Shape typeDeclaration) {
            context.setupTypeHierarchy(typeDeclaration);
        }
    }
}
