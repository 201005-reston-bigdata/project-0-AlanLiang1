package food

import org.bson.types.ObjectId

// Class representing a Mongo totalCalorie document
// it needs an _id field and an apply function that generates that field.

case class Calorie(
                    _id: ObjectId,
                    current_date: String,
                    total_calories: Option[Int],
                    total_fat: Option[Int],
                    total_carb: Option[Int],
                    total_protein: Option[Int]
                  ) {}

object Calorie {
  def apply(  date: String,
              total_calories: Option[Int],
              total_fat: Option[Int],
              total_carb: Option[Int],
              total_protein: Option[Int]) :

  // every document has an objectId (for identification)
  Calorie = new Calorie(new ObjectId(), date, total_calories, total_fat, total_carb, total_protein)
}