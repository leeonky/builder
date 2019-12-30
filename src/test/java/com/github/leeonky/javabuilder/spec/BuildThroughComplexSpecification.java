package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanSpecification;
import com.github.leeonky.javabuilder.FactorySet;
import com.github.leeonky.javabuilder.SpecificationBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class BuildThroughComplexSpecification {
    private final FactorySet factorySet = new FactorySet();

    public static class Objects {

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Product {
            private int price, discount, tax, taxDiscount;
            private int minPriceWithoutTax;
        }

        public static class ProductWithDiscount extends BeanSpecification<Product> {

            @Override
            public void specifications(SpecificationBuilder<Product> specificationBuilder) {
                specificationBuilder.property("taxDiscount").dependsOn("tax", (tax) -> ((int) tax) / 100);
                specificationBuilder.property("tax").dependsOn("price", (price) -> ((int) price) / 10);
            }
        }

        public static class ProductWithMultiDependency extends BeanSpecification<Product> {

            @Override
            public void specifications(SpecificationBuilder<Product> specificationBuilder) {
                specificationBuilder.property("minPriceWithoutTax").dependsOn(asList("tax", "price"),
                        (args) -> (int) args.get(1) - (int) args.get(0));
            }
        }
    }

    @Nested
    class ValueDependent {

        @Test
        void should_support_build_property_value_from_dependency_chain_with_dependency_orders() {
            factorySet.define(Objects.ProductWithDiscount.class);

            assertThat(factorySet.toBuild(Objects.ProductWithDiscount.class).property("price", 10000).build())
                    .hasFieldOrPropertyWithValue("tax", 1000)
                    .hasFieldOrPropertyWithValue("taxDiscount", 10);
        }

        @Test
        void should_skip_dependency_logic_when_specify_value_in_property() {
            factorySet.define(Objects.ProductWithDiscount.class);

            assertThat(factorySet.toBuild(Objects.ProductWithDiscount.class).property("tax", 100).build())
                    .hasFieldOrPropertyWithValue("taxDiscount", 1)
            ;
        }

        @Test
        void should_support_build_property_value_from_multi_dependency() {
            factorySet.define(Objects.ProductWithMultiDependency.class);

            assertThat(factorySet.toBuild(Objects.ProductWithMultiDependency.class)
                    .property("tax", 100)
                    .property("price", 2000)
                    .build())
                    .hasFieldOrPropertyWithValue("minPriceWithoutTax", 1900)
            ;
        }
    }
}