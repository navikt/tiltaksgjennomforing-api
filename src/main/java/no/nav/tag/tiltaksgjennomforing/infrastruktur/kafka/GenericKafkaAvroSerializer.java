package no.nav.tag.tiltaksgjennomforing.infrastruktur.kafka;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class GenericKafkaAvroSerializer<T> extends AbstractKafkaAvroSerializer implements Serializer<T> {
    private boolean isKey;

    public GenericKafkaAvroSerializer() {}

    public void configure(Map<String, ?> configs, boolean isKey) {
        this.isKey = isKey;
        this.configure(new KafkaAvroSerializerConfig(configs));
    }

    public byte[] serialize(String topic, Object record) {
        if (record == null) {
            return null;
        } else {
            AvroSchema schema = new AvroSchema(AvroSchemaUtils.getSchema(record, this.useSchemaReflection, this.avroReflectionAllowNull, this.removeJavaProperties));
            return this.serializeImpl(this.getSubjectName(topic, this.isKey, record, schema), record, schema);
        }
    }

    public void close() {}
}
