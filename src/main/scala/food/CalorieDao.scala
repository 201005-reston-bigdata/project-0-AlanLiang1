package food

import com.mongodb.client.model.UpdateOptions
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.{MongoClient, MongoCollection, Observable}
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

class CalorieDao {
  val codecRegistry = fromRegistries(fromProviders(classOf[Calorie]), MongoClient.DEFAULT_CODEC_REGISTRY)
  val client = MongoClient()
  // get access to the database
  val db = client.getDatabase("foodDB").withCodecRegistry(codecRegistry)
  // get access to the collection
  val collection : MongoCollection[Calorie] = db.getCollection("totalCalorie")

  //helper functions for access and printing
  def getResults[T](obs: Observable[T]): Seq[T] = {
    Await.result(obs.toFuture(), Duration(10, SECONDS))
  }

  def printResults[T](obs: Observable[T]): Unit = {
    getResults(obs).foreach(println(_))
  }

  // UpdateOptions object that has upsert set to true, for convenience
  val upsertTrue :UpdateOptions = (new UpdateOptions()).upsert(true)

  /**
   * function to insert individual intake in the DB
   * @return Unit
   */
  def insertCalorie(date: String, calorie: Int, fat: Int, carb: Int, protein:Int) : Unit = {
    printResults(collection.insertMany(List(
      Calorie(date, Some(calorie), Some(fat), Some(carb), Some(protein))
    )))
  }

  /**
   * function to delete intake from the DB
   * @return Unit
   */
  def deleteAll() : Unit = {
    //  delete all documents with the field title "total_calories"
    printResults(collection.deleteMany(Filters.exists("total_calories")))
  }

  /**
   * function to retrieve data by current_date
   * @return Seq[Calorie]
   */
  def getByDate(date: String) : Seq[Calorie] = {
    getResults(collection.find(equal("current_date", date)))
  }

  /**
   * function to delete data by current_date
   * @return Unit
   */
  def deleteByDate(date: String) = {
    try {
      getResults(collection.deleteMany((equal("current_date", date))))
    } catch {
      case e: Exception => {
        e.printStackTrace()
        false
      }
    }
  }

  /**
   * function to close client when done
   * @return Unit
   */
  def closeClient(): Unit = {
    client.close()
  }

}
