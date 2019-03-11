package com.github.leeonky;

import lombok.Getter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

class ConverterTest {
    private Converter converter = new Converter();

    @Test
    void box_class() {
        assertThat(Converter.boxedClass(int.class)).isEqualTo(Integer.class);
        assertThat(Converter.boxedClass(short.class)).isEqualTo(Short.class);
        assertThat(Converter.boxedClass(long.class)).isEqualTo(Long.class);
        assertThat(Converter.boxedClass(float.class)).isEqualTo(Float.class);
        assertThat(Converter.boxedClass(double.class)).isEqualTo(Double.class);
        assertThat(Converter.boxedClass(boolean.class)).isEqualTo(Boolean.class);
    }

    enum NameEnums {
        E1, E2
    }

    enum ValueEnums implements ValueEnum<Integer> {
        E1(0), E2(1);

        @Getter
        Integer value;

        ValueEnums(int i) {
            value = i;
        }
    }

    interface ValueEnum<V extends Number> {
        static <E extends ValueEnum<V>, V extends Number> E fromValue(Class<E> type, V value) {
            return Arrays.stream(type.getEnumConstants()).filter((v) -> v.getValue().equals(value))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported enum value '" + value + "'"));
        }

        static <E extends ValueEnum<V>, V extends Number> E fromNumber(Class<E> type, Number value) {
            return Arrays.stream(type.getEnumConstants()).filter((v) -> v.getValue().equals(value))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported enum value '" + value + "'"));
        }

        V getValue();
    }

    @Nested
    class TypeOperator {
        @Test
        void no_candidate_converter_should_return_original_value() {
            converter.addTypeConverter(Long.class, Bean.class, s -> null);

            assertThat(converter.tryConvert(Bean.class, "String")).isEqualTo("String");
        }

        @Test
        void no_defined_converter_for_target_type_should_return_original_value() {
            assertThat(converter.tryConvert(Bean.class, "String")).isEqualTo("String");
        }

        @Test
        void assign_sub_type_to_base_should_keep_original() {
            Bean.SubBean subBean = new Bean.SubBean();
            converter.addTypeConverter(Bean.SubBean.class, Bean.class, sb -> {
                throw new RuntimeException();
            });

            assertThat(converter.tryConvert(Bean.class, subBean)).isEqualTo(subBean);
        }

        @Test
        void candidate_converter() {
            converter.addTypeConverter(String.class, Integer.class, Integer::valueOf);

            assertThat(converter.tryConvert(Integer.class, "100")).isEqualTo(100);
        }

        @Test
        void candidate_converter_as_base_type() {
            converter.addTypeConverter(Object.class, String.class, o -> "Hello");

            assertThat(converter.tryConvert(String.class, new Bean())).isEqualTo("Hello");
        }
    }

    @Nested
    class EnumConvert {
        @Test
        void covert_to_enum_from_name() {
            assertThat(converter.tryConvert(NameEnums.class, "E2")).isEqualTo(NameEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_type() {
            converter.addEnumConverter(Integer.class, ValueEnums.class, ValueEnum::fromValue);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_type_auto_boxed() {
            converter.addEnumConverter(int.class, ValueEnums.class, ValueEnum::fromValue);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_type_and_sub_value_type() {
            converter.addEnumConverter(Number.class, ValueEnums.class, ValueEnum::fromNumber);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_base_type() {
            converter.addEnumConverter(Integer.class, Enum.class, (c, i) -> ValueEnums.E2);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }
    }

    @Nested
    class DefaultConvert {
        Converter converter = Converter.createDefaultConverter();

        @Test
        void parse_string() throws ParseException {
            assertConvert(long.class, "100", 100L);
            assertConvert(int.class, "100", 100);
            assertConvert(short.class, "100", (short) 100);
            assertConvert(byte.class, "100", (byte) 100);
            assertConvert(float.class, "100", (float) 100);
            assertConvert(double.class, "100", (double) 100);
            assertConvert(boolean.class, "true", true);

            assertConvert(Long.class, "100", 100L);
            assertConvert(Integer.class, "100", 100);
            assertConvert(Short.class, "100", (short) 100);
            assertConvert(Byte.class, "100", (byte) 100);
            assertConvert(Float.class, "100", (float) 100);
            assertConvert(Double.class, "100", (double) 100);
            assertConvert(Boolean.class, "true", true);

            assertConvert(BigDecimal.class, "100", BigDecimal.valueOf(100));
            assertConvert(BigInteger.class, "100", BigInteger.valueOf(100));

            assertConvert(UUID.class, "123e4567-e89b-12d3-a456-426655440000", UUID.fromString("123e4567-e89b-12d3-a456-426655440000"));
            assertConvert(Instant.class, "2001-10-12T12:00:01.123Z", Instant.parse("2001-10-12T12:00:01.123Z"));
            assertConvert(Date.class, "2001-10-12", new SimpleDateFormat("yyyy-MM-dd").parse("2001-10-12"));
            assertConvert(LocalTime.class, "00:00:01", LocalTime.parse("00:00:01"));
            assertConvert(LocalDate.class, "1996-01-24", LocalDate.parse("1996-01-24"));
            assertConvert(LocalDateTime.class, "1996-01-23T00:00:01", LocalDateTime.parse("1996-01-23T00:00:01"));
        }

        private void assertConvert(Class<?> type, Object value, Object toValue) {
            assertThat(converter.tryConvert(type, value)).isEqualTo(toValue);
        }

        @Nested
        class NumberConvert {

            @Test
            void to_big_decimal() {
                assertConvert(BigDecimal.class, 100L, BigDecimal.valueOf(100));
                assertConvert(BigDecimal.class, 100, BigDecimal.valueOf(100));
                assertConvert(BigDecimal.class, (short) 100, BigDecimal.valueOf(100));
                assertConvert(BigDecimal.class, (byte) 100, BigDecimal.valueOf(100));
                assertConvert(BigDecimal.class, (float) 100, BigDecimal.valueOf(100));
                assertConvert(BigDecimal.class, (double) 100, BigDecimal.valueOf(100));
            }
        }
    }
}
