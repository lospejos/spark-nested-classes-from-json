# Processing JSON containing nested entities using Spark Structured Streaming

I want to read nested data from Kafka topics source using Spark Structured Streaming. 
My Scala code (case classes and Spark processing code):

```scala
  case class Nested(attr_int: Integer, attr_string: String, attr_float: Float, attr_timestamp: java.sql.Timestamp)

  case class Parent(a_str: String, a_long: Long, a_nested: Array[Nested])


  import org.apache.spark.sql.Encoders
  val jsonSchema = Encoders.product[Parent].schema

  val df = sparkSession
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "testnested")
    .option("group.id", "testnested")
    .option("key.deserializer", "org.apache.kafka.common.serialization.StringSerializer")
    .option("value.deserializer", "org.apache.kafka.common.serialization.StringSerializer")
    .load()
    .select($"value" cast "string" as "json")
    .select(from_json($"json", jsonSchema) as "data")
    .select("data.*")
    .withColumn("nested", explode($"a_nested"))
    .select("nested.*")
    .as[Nested]
    .writeStream
    .format("console")
    .start()
    .awaitTermination()
```

When I send to Kafka data:

```json
{"a_str":"Str","a_long":100,"a_nested":[{"attr_int":0,"attr_string":"nested_0","attr_float":0.0,"attr_timestamp":"2018-01-01T11:00:00.123321+02:00"},{"attr_int":1,"attr_string":"nested_1","attr_float":1.0,"attr_timestamp":"2018-02-02T12:01:01.023321+02:00"}]}
```

I get results:

```
+--------+-----------+----------+--------------------+
|attr_int|attr_string|attr_float|      attr_timestamp|
+--------+-----------+----------+--------------------+
|       0|   nested_0|       0.0|2018-01-01 13:02:...|
|       1|   nested_1|       1.0|2018-02-02 14:01:...|
+--------+-----------+----------+--------------------+
```

Now I want to get each nested item joined to parent data, f.e.:
```
+--------+-----------+----------+--------------------+-------+--------+
|attr_int|attr_string|attr_float|      attr_timestamp| a_str | a_long |
+--------+-----------+----------+--------------------+-------+--------+
|       0|   nested_0|       0.0|2018-01-01 13:02:...|   Str |    100 | 
|       1|   nested_1|       1.0|2018-02-02 14:01:...|   Str |    100 |
+--------+-----------+----------+--------------------+-------+--------+
```

Note that "a_str" and "a_long" are columns from the parent entity "Parent".
Since I'm not an expert in Spark Structured Streams processing, I want to know what is the most "idiomatic" approach to do it?
Currently I have assumptions:
1. Create custom Kafka value deserializer
2. Write some kind of join on structured streams (I stuck on it), but I suppose this will require changing json structure (f.e. specify in nested some key value pointing to a parent data)
3. Write custom method which will return denormalized data for joined entities and use flatMap with this method

Please advise.

Thanks