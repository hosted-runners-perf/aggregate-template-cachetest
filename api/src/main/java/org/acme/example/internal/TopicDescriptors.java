/*
 * Copyright 2021-2022 Creek Contributors (https://github.com/creek-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.example.internal;

import static java.util.Objects.requireNonNull;
import static org.creekservice.api.kafka.metadata.SerializationFormat.serializationFormat;

import java.net.URI;
import java.util.Optional;
import org.creekservice.api.kafka.metadata.CreatableKafkaTopicInternal;
import org.creekservice.api.kafka.metadata.KafkaTopicConfig;
import org.creekservice.api.kafka.metadata.KafkaTopicDescriptor;
import org.creekservice.api.kafka.metadata.KafkaTopicDescriptor.PartDescriptor;
import org.creekservice.api.kafka.metadata.KafkaTopicInput;
import org.creekservice.api.kafka.metadata.KafkaTopicInternal;
import org.creekservice.api.kafka.metadata.KafkaTopicOutput;
import org.creekservice.api.kafka.metadata.OwnedKafkaTopicInput;
import org.creekservice.api.kafka.metadata.OwnedKafkaTopicOutput;
import org.creekservice.api.kafka.metadata.SerializationFormat;

/**
 * Helper for creating topic descriptors.
 *
 * <p>Wondering where the builds are for {@link org.creekservice.api.kafka.metadata.KafkaTopicInput}
 * or {@link org.creekservice.api.kafka.metadata.KafkaTopicOutput}? These should only be created by
 * calling {@link OwnedKafkaTopicInput#toOutput()} and {@link OwnedKafkaTopicOutput#toInput()} on an
 * owned topic descriptor, respectively.
 */
@SuppressWarnings("unused") // What is unused today may be used tomorrow...
public final class TopicDescriptors {

    public static final SerializationFormat KAFKA_FORMAT = serializationFormat("kafka");

    private TopicDescriptors() {}

    /**
     * Create an input Kafka topic descriptor.
     *
     * <p>Looking for a version that returns {@link
     * org.creekservice.api.kafka.metadata.KafkaTopicInput}? Get one of those by calling {@link
     * OwnedKafkaTopicOutput#toInput()} on the topic descriptor defined in the upstream component.
     *
     * @param topicName the name of the topic
     * @param keyType the type serialized into the Kafka record key.
     * @param valueType the type serialized into the Kafka record value.
     * @param config the config of the topic.
     * @param <K> the type serialized into the Kafka record key.
     * @param <V> the type serialized into the Kafka record value.
     * @return the input topic descriptor.
     */
    public static <K, V> OwnedKafkaTopicInput<K, V> inputTopic(
            final String topicName,
            final Class<K> keyType,
            final Class<V> valueType,
            final TopicConfigBuilder config) {
        return new InputTopicDescriptor<>(topicName, keyType, valueType, config);
    }

    /**
     * Create a Kafka topic descriptor for a topic that is implicitly created.
     *
     * <p>Most internal topics, e.g. Kafka Streams changelog and repartition topics, are implicitly
     * created, and this is the method to use to build a descriptor for them.
     *
     * <p>For an internal topic that you want Creek to create, use {@link #creatableInternalTopic}.
     *
     * @param topicName the name of the topic
     * @param keyType the type serialized into the Kafka record key.
     * @param valueType the type serialized into the Kafka record value.
     * @param <K> the type serialized into the Kafka record key.
     * @param <V> the type serialized into the Kafka record value.
     * @return the internal topic descriptor.
     */
    public static <K, V> KafkaTopicInternal<K, V> internalTopic(
            final String topicName, final Class<K> keyType, final Class<V> valueType) {
        return new InternalTopicDescriptor<>(topicName, keyType, valueType);
    }

    /**
     * Create a Kafka topic descriptor for a topic that is implicitly created.
     *
     * <p>Most internal topics, e.g. Kafka Streams changelog and repartition topics, are implicitly
     * created For such topics use {@link #internalTopic}
     *
     * <p>For an internal topic that you want Creek to create, use this method.
     *
     * @param topicName the name of the topic
     * @param keyType the type serialized into the Kafka record key.
     * @param valueType the type serialized into the Kafka record value.
     * @param config the config of the topic.
     * @param <K> the type serialized into the Kafka record key.
     * @param <V> the type serialized into the Kafka record value.
     * @return the internal topic descriptor.
     */
    public static <K, V> CreatableKafkaTopicInternal<K, V> creatableInternalTopic(
            final String topicName,
            final Class<K> keyType,
            final Class<V> valueType,
            final TopicConfigBuilder config) {
        return new CreatableInternalTopicDescriptor<>(topicName, keyType, valueType, config);
    }

    /**
     * Create an output Kafka topic descriptor.
     *
     * <p>Looking for a version that returns {@link
     * org.creekservice.api.kafka.metadata.KafkaTopicOutput}? Get one of those by calling {@link
     * OwnedKafkaTopicInput#toOutput()} on the topic descriptor defined in the downstream component.
     *
     * @param topicName the name of the topic
     * @param keyType the type serialized into the Kafka record key.
     * @param valueType the type serialized into the Kafka record value.
     * @param config the config of the topic.
     * @param <K> the type serialized into the Kafka record key.
     * @param <V> the type serialized into the Kafka record value.
     * @return the output topic descriptor.
     */
    public static <K, V> OwnedKafkaTopicOutput<K, V> outputTopic(
            final String topicName,
            final Class<K> keyType,
            final Class<V> valueType,
            final TopicConfigBuilder config) {
        return new OutputTopicDescriptor<>(topicName, keyType, valueType, config);
    }

    private static final class KafkaPart<T> implements PartDescriptor<T> {

        private final Class<T> type;

        KafkaPart(final Class<T> type) {
            this.type = requireNonNull(type, "type");
        }

        @Override
        public SerializationFormat format() {
            return KAFKA_FORMAT;
        }

        @Override
        public Class<T> type() {
            return type;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private abstract static class TopicDescriptor<K, V> implements KafkaTopicDescriptor<K, V> {

        private final URI id;
        private final String topicName;
        private final PartDescriptor<K> key;
        private final PartDescriptor<V> value;
        private final Optional<KafkaTopicConfig> config;

        TopicDescriptor(
                final String topicName,
                final Class<K> keyType,
                final Class<V> valueType,
                final Optional<TopicConfigBuilder> config) {
            this.topicName = requireNonNull(topicName, "topicName");
            this.key = new KafkaPart<>(keyType);
            this.value = new KafkaPart<>(valueType);
            this.config = requireNonNull(config, "config").map(TopicConfigBuilder::build);
            this.id = KafkaTopicDescriptor.super.id();
        }

        public URI id() {
            return id;
        }

        public String name() {
            return topicName;
        }

        public PartDescriptor<K> key() {
            return key;
        }

        public PartDescriptor<V> value() {
            return value;
        }

        public KafkaTopicConfig config() {
            return config.orElseThrow();
        }
    }

    private static final class OutputTopicDescriptor<K, V> extends TopicDescriptor<K, V>
            implements OwnedKafkaTopicOutput<K, V> {

        OutputTopicDescriptor(
                final String topicName,
                final Class<K> keyType,
                final Class<V> valueType,
                final TopicConfigBuilder config) {
            super(topicName, keyType, valueType, Optional.of(config));
        }

        @Override
        public KafkaTopicInput<K, V> toInput() {
            return new KafkaTopicInput<>() {
                @Override
                public URI id() {
                    return OutputTopicDescriptor.this.id();
                }

                @Override
                public String name() {
                    return OutputTopicDescriptor.this.name();
                }

                @Override
                public PartDescriptor<K> key() {
                    return OutputTopicDescriptor.this.key();
                }

                @Override
                public PartDescriptor<V> value() {
                    return OutputTopicDescriptor.this.value();
                }
            };
        }
    }

    private static final class InputTopicDescriptor<K, V> extends TopicDescriptor<K, V>
            implements OwnedKafkaTopicInput<K, V> {

        InputTopicDescriptor(
                final String topicName,
                final Class<K> keyType,
                final Class<V> valueType,
                final TopicConfigBuilder config) {
            super(topicName, keyType, valueType, Optional.of(config));
        }

        @Override
        public KafkaTopicOutput<K, V> toOutput() {
            return new KafkaTopicOutput<>() {
                @Override
                public URI id() {
                    return InputTopicDescriptor.this.id();
                }

                @Override
                public String name() {
                    return InputTopicDescriptor.this.name();
                }

                @Override
                public PartDescriptor<K> key() {
                    return InputTopicDescriptor.this.key();
                }

                @Override
                public PartDescriptor<V> value() {
                    return InputTopicDescriptor.this.value();
                }
            };
        }
    }

    private static final class InternalTopicDescriptor<K, V> extends TopicDescriptor<K, V>
            implements KafkaTopicInternal<K, V> {

        InternalTopicDescriptor(
                final String topicName, final Class<K> keyType, final Class<V> valueType) {
            super(topicName, keyType, valueType, Optional.empty());
        }
    }

    private static final class CreatableInternalTopicDescriptor<K, V> extends TopicDescriptor<K, V>
            implements CreatableKafkaTopicInternal<K, V> {

        CreatableInternalTopicDescriptor(
                final String topicName,
                final Class<K> keyType,
                final Class<V> valueType,
                final TopicConfigBuilder config) {
            super(topicName, keyType, valueType, Optional.of(config));
        }
    }
}
