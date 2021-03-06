package org.datavec.spark.transform;

import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.writable.DoubleWritable;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;
import org.datavec.spark.transform.model.Base64NDArrayBody;
import org.datavec.spark.transform.model.BatchRecord;
import org.datavec.spark.transform.model.CSVRecord;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.serde.base64.Nd4jBase64;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by agibsonccc on 12/24/16.
 */
public class CSVSparkTransformerTest {
    @Test
    public void testTransformer() throws Exception {
        List<Writable> input = new ArrayList<>();
        input.add(new DoubleWritable(1.0));
        input.add(new DoubleWritable(2.0));

        Schema schema = new Schema.Builder().addColumnDouble("1.0").addColumnDouble("2.0").build();
        List<Writable> output = new ArrayList<>();
        output.add(new Text("1.0"));
        output.add(new Text("2.0"));

        TransformProcess transformProcess =
                        new TransformProcess.Builder(schema).convertToString("1.0").convertToString("2.0").build();
        CSVSparkTransform csvSparkTransform = new CSVSparkTransform(transformProcess);
        String[] values = new String[] {"1.0", "2.0"};
        CSVRecord record = csvSparkTransform.transform(new CSVRecord(values));
        Base64NDArrayBody body = csvSparkTransform.toArray(new CSVRecord(values));
        INDArray fromBase64 = Nd4jBase64.fromBase64(body.getNdarray());
        assertTrue(fromBase64.isVector());
        System.out.println("Base 64ed array " + fromBase64);

    }

    @Test
    public void testTransformerBatch() throws Exception {
        List<Writable> input = new ArrayList<>();
        input.add(new DoubleWritable(1.0));
        input.add(new DoubleWritable(2.0));

        Schema schema = new Schema.Builder().addColumnDouble("1.0").addColumnDouble("2.0").build();
        List<Writable> output = new ArrayList<>();
        output.add(new Text("1.0"));
        output.add(new Text("2.0"));

        TransformProcess transformProcess =
                        new TransformProcess.Builder(schema).convertToString("1.0").convertToString("2.0").build();
        CSVSparkTransform csvSparkTransform = new CSVSparkTransform(transformProcess);
        String[] values = new String[] {"1.0", "2.0"};
        CSVRecord record = csvSparkTransform.transform(new CSVRecord(values));
        BatchRecord batchRecord = new BatchRecord();
        for (int i = 0; i < 3; i++)
            batchRecord.add(record);
        BatchRecord batchRecord1 = csvSparkTransform.transform(batchRecord);
        Base64NDArrayBody body = csvSparkTransform.toArray(batchRecord1);
        INDArray fromBase64 = Nd4jBase64.fromBase64(body.getNdarray());
        assertTrue(fromBase64.isMatrix());
        System.out.println("Base 64ed array " + fromBase64);

    }


}
