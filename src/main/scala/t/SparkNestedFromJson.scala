package t

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object SparkNestedFromJson extends App {

  case class Nested(attr_int: Integer, attr_string: String, attr_float: Float, attr_timestamp: java.sql.Timestamp)

  case class Parent(a_str: String, a_long: Long, a_nested: Array[Nested])

  val sparkSession = SparkSession.builder()
    .master("local[*]")
    .appName("test")
    .getOrCreate()

  import sparkSession.implicits._

  //val jsonStr = """{"a_str":"string1"}"""
 // {"a_str":"Str","a_long":100,"a_nested":[{"attr_int":0,"attr_string":"nested_0","attr_float":0.0,"attr_timestamp":"2018-01-01T11:00:00.123321+02:00"},{"attr_int":1,"attr_string":"nested_1","attr_float":1.0,"attr_timestamp":"2018-02-02T12:01:01.023321+02:00"}]}

  import org.apache.spark.sql.Encoders
  val jsonSchema = Encoders.product[Parent].schema

  val df = sparkSession
      .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "testnested")
    .option("group.id", "testnested")
    //.option("enable.auto.commit", "true")
    //.option("auto.commit.interval.ms", "1000")
    //.option("auto.offset.reset", "earliest")
    .option("key.deserializer", "org.apache.kafka.common.serialization.StringSerializer")
    .option("value.deserializer", "org.apache.kafka.common.serialization.StringSerializer")
    .option("failOnDataLoss", "false") // trying to prevent error ava.lang.IllegalStateException: Cannot find earliest offsets of Set(LoyMemberRegistered-2, LoyMemberRegistered-1, LoyMemberRegistered-0). Some data may have been missed.
    .load()

  val ds = df
    .select($"value" cast "string" as "json")
    .select(from_json($"json", jsonSchema) as "data")
    .select("data.*")


//  val nestedDF = ds
    .withColumn("nested", explode($"a_nested"))
    //.withColumn("event_id", $"event_id")
    .select($"nested.*", $"a_str", $"a_long")
    //.as[Nested]


  ds
    .writeStream
    .format("console")
    .start()
    .awaitTermination()
}
