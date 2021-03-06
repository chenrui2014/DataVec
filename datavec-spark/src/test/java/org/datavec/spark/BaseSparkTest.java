/*-
 *  * Copyright 2016 Skymind, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 */

package org.datavec.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.After;
import org.junit.Before;

import java.io.Serializable;

public abstract class BaseSparkTest implements Serializable {
    protected static JavaSparkContext sc;

    @Before
    public void before() {
        sc = getContext();
    }

    @After
    public synchronized void after() {
        sc.close();
        //Wait until it's stopped, to avoid race conditions during tests
        for (int i = 0; i < 100; i++) {
            if (!sc.sc().stopped().get()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        if (!sc.sc().stopped().get()) {
            throw new RuntimeException("Spark context is not stopped after 10s");
        }


        sc = null;
    }

    public static synchronized JavaSparkContext getContext() {
        if (sc != null)
            return sc;

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").set("spark.driverEnv.SPARK_LOCAL_IP", "127.0.0.1")
                        .set("spark.executorEnv.SPARK_LOCAL_IP", "127.0.0.1").setAppName("sparktest");


        sc = new JavaSparkContext(sparkConf);

        return sc;
    }
}
