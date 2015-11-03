/**
 * Copyright 2015 Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package io.confluent.copycat.hdfs;

import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.copycat.data.Schema;
import org.apache.kafka.copycat.sink.SinkRecord;

import java.io.IOException;

import io.confluent.copycat.avro.AvroData;

public class AvroRecordWriterProvider implements RecordWriterProvider {

  @Override
  public RecordWriter<Long, SinkRecord> getRecordWriter(Configuration conf, final String fileName,
                                                        SinkRecord record, final AvroData avroData)
      throws IOException {
    DatumWriter<Object> datumWriter = new GenericDatumWriter<>();
    final DataFileWriter<Object> writer = new DataFileWriter<>(datumWriter);
    Path path = new Path(fileName);

    final Schema schema = record.valueSchema();
    final FSDataOutputStream out = path.getFileSystem(conf).create(path);
    writer.create(avroData.fromCopycatSchema(schema), out);

    return new RecordWriter<Long, SinkRecord>(){
      @Override
      public void write(Long key, SinkRecord record) throws IOException {
        Object value = avroData.fromCopycatData(schema, record.value());
        writer.append(value);
      }

      @Override
      public void close() throws IOException {
        writer.close();
      }
    };
  }
}
