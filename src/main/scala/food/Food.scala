package food

import org.bson.types.ObjectId

// Class representing a Mongo food document
// it needs an _id field and an apply function that generates that field.

case class Food(
                 _id: ObjectId,
                 current_date: String,
                 foodName: String,
                 calories: Option[Int],
                 fat: Option[Int],
                 carb: Option[Int],
                 protein: Option[Int]
               ) {}

object Food {
  def apply(   date: String,
               foodName:String,
               calories: Option[Int],
               fat: Option[Int],
               carb: Option[Int],
               protein: Option[Int]) :

  // every document has an objectId (for identification)
  Food = new Food(new ObjectId(), date, foodName, calories, fat, carb, protein)
}