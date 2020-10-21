package food

import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Calendar

import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn
import com.mongodb.client.model.UpdateOptions
import org.bson.BsonNull
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{MongoClient, MongoCollection, Observable}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}
// make sure to start with imports in your own code
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}

/** CLI (command line interface) that interacts with the user in calorie tracker program */
class Cli {
  val dao = new FoodDao()
  val dao2 = new CalorieDao()

  // commandArgPattern is regex that will get us a command and arguments to that command from user input
  val commandArgPattern = "(\\w+)\\s*(.*)".r

  // all of the food that is listed for users to choose from
  var foodList = ArrayBuffer[String]()
  var totalCal = ArrayBuffer[Int]()
  var totalFat = ArrayBuffer[Int]()
  var totalCarb = ArrayBuffer[Int]()
  var totalProtein = ArrayBuffer[Int]()

  // this is the user's list of food consumed
  var myFoodList = ArrayBuffer[String]()
  var myTotalCal = ArrayBuffer[Int]()
  var myTotalFat = ArrayBuffer[Int]()
  var myTotalCarb = ArrayBuffer[Int]()
  var myTotalProtein = ArrayBuffer[Int]()
  var myId = ArrayBuffer[Int]()

  /**
   * prints a welcome message for user
   * @return Unit
   */
  def printWelcome() : Unit = {
    println("========================================")
    println("| Hi. Welcome to your Calorie Tracker  |")
    println("========================================")
  }

  /**
   * prints out the options with the welcome message
   * @return Unit
   */
  def printOptions() : Unit = {
    println("Please enter one of the following options:")
    println("-- file [filename] : import .csv file")
    println("-- exit : close CLI")
  }

  /**
   * prints out the entire food list
   * @return Unit
   */
  def printList() : Unit = {
    var num = 1
    var num2 = 0
    for(i <- foodList) {
      if (i == "big mac") println("============ BURGER MENU =============")
      if (i == "egg mcmuffin") println("============ BREAKFAST MENU =============")
      if (i == "four piece chicken mcnuggets") println("============ CHICKEN & SANDWICHES MENU =============")
      if (i == "kids fries") println("============ SNACKS & SIDES MENU =============")
      if (i == "small chocolate shake") println("============ DESSERTS & SHAKES MENU =============")

      println(s"#${num}. $i || ${totalCal(num2)} cal || fat: ${totalFat(num2)}g || carbs: ${totalCarb(num2)}g || protein: ${totalProtein(num2)}g")
      num += 1
      num2 += 1
    }
    println("")
  }

  /**
   * prints out the second list of options after the welcome message
   * @return Unit
   */
  def printOptions2() : Unit = {
    println("Please enter a number from the list (to add a food you ate) OR enter one of the following options:")
    println("-- delete [food#] : delete a food in your current list")
    println("-- clear current : clear current list")
    println("-- show current : show current list and intake")
    println("-- show all : show entire food list")
    println("-- save food: store current food list in DB")
    println("-- save calories: store current intake in DB")
    println("-- reset food: reset entire food list in DB")
    println("-- reset calories: reset current intake in DB")
    println("-- back : return to Main Menu")
    println("-- exit : close CLI")
  }


  /**
   * main function - runs the menu prompting + listening for user input
   * @return Unit
   */
  def menu():Unit = {

    var continueMenuLoop = true;
    var continueMenuLoop2 = true;

    // This loop here will repeatedly prompt, listen, run code, and repeat
    while(continueMenuLoop) {
      printWelcome()
      printOptions()
      // get user input with Stdin.readLine, read directly from StdIn
      StdIn.readLine() match {
        case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("file") => {
          try {
            println("")
            val bufferedSource = io.Source.fromFile(arg)
            var num = 1
            var num_of_food = 0
            println("Choose one of the following. Type in the corresponded number. \n")
            // parse each line from the .csv file and drop the header

            for (line <- bufferedSource.getLines.drop(1)) {
              // split the data with the "," delimiter
              val cols = line.split(",").map(_.trim)
              if (cols(0) == "big mac") println("============ BURGER MENU =============")
              if (cols(0) == "egg mcmuffin") println("============ BREAKFAST MENU =============")
              if (cols(0) == "four piece chicken mcnuggets") println("============ CHICKEN & SANDWICHES MENU =============")
              if (cols(0) == "kids fries") println("============ SNACKS & SIDES MENU =============")
              if (cols(0) == "small chocolate shake") println("============ DESSERTS & SHAKES MENU =============")
              // as we progress via each line, we send each data into its corresponding list
              foodList += cols(0)
              totalCal += cols(1).toInt
              totalFat += cols(2).toInt
              totalCarb += cols(3).toInt
              totalProtein += cols(4).toInt
              // then we print out each line onto the command line to show the user the info
              println(s"#${num}. ${cols(0)} || ${cols(1)} cal || fat: ${cols(2)}g || carbs: ${cols(3)}g || protein: ${cols(4)}g")
              num += 1
              num_of_food += 1
            }

            println("")

            do {
              printOptions2()
              // when the user enters a number to the corresponding food from the list, the program saves it
              StdIn.readLine() match {

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("1") => {
                  myFoodList += foodList(0)
                  myTotalCal += totalCal(0)
                  myTotalFat += totalFat(0)
                  myTotalCarb += totalCarb(0)
                  myTotalProtein += totalProtein(0)
                  myId += 1
                  println(s"${foodList(0)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("2") => {
                  myFoodList += foodList(1)
                  myTotalCal += totalCal(1)
                  myTotalFat += totalFat(1)
                  myTotalCarb += totalCarb(1)
                  myTotalProtein += totalProtein(1)
                  myId += 2
                  println(s"${foodList(1)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("3") => {
                  myFoodList += foodList(2)
                  myTotalCal += totalCal(2)
                  myTotalFat += totalFat(2)
                  myTotalCarb += totalCarb(2)
                  myTotalProtein += totalProtein(2)
                  myId += 3
                  println(s"${foodList(2)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("4") => {
                  myFoodList += foodList(3)
                  myTotalCal += totalCal(3)
                  myTotalFat += totalFat(3)
                  myTotalCarb += totalCarb(3)
                  myTotalProtein += totalProtein(3)
                  myId += 4
                  println(s"${foodList(3)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("5") => {
                  myFoodList += foodList(4)
                  myTotalCal += totalCal(4)
                  myTotalFat += totalFat(4)
                  myTotalCarb += totalCarb(4)
                  myTotalProtein += totalProtein(4)
                  myId += 5
                  println(s"${foodList(4)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("6") => {
                  myFoodList += foodList(5)
                  myTotalCal += totalCal(5)
                  myTotalFat += totalFat(5)
                  myTotalCarb += totalCarb(5)
                  myTotalProtein += totalProtein(5)
                  myId += 6
                  println(s"${foodList(5)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("7") => {
                  myFoodList += foodList(6)
                  myTotalCal += totalCal(6)
                  myTotalFat += totalFat(6)
                  myTotalCarb += totalCarb(6)
                  myTotalProtein += totalProtein(6)
                  myId += 7
                  println(s"${foodList(6)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("8") => {
                  myFoodList += foodList(7)
                  myTotalCal += totalCal(7)
                  myTotalFat += totalFat(7)
                  myTotalCarb += totalCarb(7)
                  myTotalProtein += totalProtein(7)
                  myId += 8
                  println(s"${foodList(7)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("9") => {
                  myFoodList += foodList(8)
                  myTotalCal += totalCal(8)
                  myTotalFat += totalFat(8)
                  myTotalCarb += totalCarb(8)
                  myTotalProtein += totalProtein(8)
                  myId += 9
                  println(s"${foodList(8)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("10") => {
                  myFoodList += foodList(9)
                  myTotalCal += totalCal(9)
                  myTotalFat += totalFat(9)
                  myTotalCarb += totalCarb(9)
                  myTotalProtein += totalProtein(9)
                  myId += 10
                  println(s"${foodList(9)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("11") => {
                  myFoodList += foodList(10)
                  myTotalCal += totalCal(10)
                  myTotalFat += totalFat(10)
                  myTotalCarb += totalCarb(10)
                  myTotalProtein += totalProtein(10)
                  myId += 11
                  println(s"${foodList(10)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("12") => {
                  myFoodList += foodList(11)
                  myTotalCal += totalCal(11)
                  myTotalFat += totalFat(11)
                  myTotalCarb += totalCarb(11)
                  myTotalProtein += totalProtein(11)
                  myId += 12
                  println(s"${foodList(11)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("13") => {
                  myFoodList += foodList(12)
                  myTotalCal += totalCal(12)
                  myTotalFat += totalFat(12)
                  myTotalCarb += totalCarb(12)
                  myTotalProtein += totalProtein(12)
                  myId += 13
                  println(s"${foodList(12)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("14") => {
                  myFoodList += foodList(13)
                  myTotalCal += totalCal(13)
                  myTotalFat += totalFat(13)
                  myTotalCarb += totalCarb(13)
                  myTotalProtein += totalProtein(13)
                  myId += 14
                  println(s"${foodList(13)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("15") => {
                  myFoodList += foodList(14)
                  myTotalCal += totalCal(14)
                  myTotalFat += totalFat(14)
                  myTotalCarb += totalCarb(14)
                  myTotalProtein += totalProtein(14)
                  myId += 15
                  println(s"${foodList(14)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("16") => {
                  myFoodList += foodList(15)
                  myTotalCal += totalCal(15)
                  myTotalFat += totalFat(15)
                  myTotalCarb += totalCarb(15)
                  myTotalProtein += totalProtein(15)
                  myId += 16
                  println(s"${foodList(15)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("17") => {
                  myFoodList += foodList(16)
                  myTotalCal += totalCal(16)
                  myTotalFat += totalFat(16)
                  myTotalCarb += totalCarb(16)
                  myTotalProtein += totalProtein(16)
                  myId += 17
                  println(s"${foodList(16)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("18") => {
                  myFoodList += foodList(17)
                  myTotalCal += totalCal(17)
                  myTotalFat += totalFat(17)
                  myTotalCarb += totalCarb(17)
                  myTotalProtein += totalProtein(17)
                  myId += 18
                  println(s"${foodList(17)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("19") => {
                  myFoodList += foodList(18)
                  myTotalCal += totalCal(18)
                  myTotalFat += totalFat(18)
                  myTotalCarb += totalCarb(18)
                  myTotalProtein += totalProtein(18)
                  myId += 19
                  println(s"${foodList(18)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("20") => {
                  myFoodList += foodList(19)
                  myTotalCal += totalCal(19)
                  myTotalFat += totalFat(19)
                  myTotalCarb += totalCarb(19)
                  myTotalProtein += totalProtein(19)
                  myId += 20
                  println(s"${foodList(19)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("21") => {
                  myFoodList += foodList(20)
                  myTotalCal += totalCal(20)
                  myTotalFat += totalFat(20)
                  myTotalCarb += totalCarb(20)
                  myTotalProtein += totalProtein(20)
                  myId += 21
                  println(s"${foodList(20)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("22") => {
                  myFoodList += foodList(21)
                  myTotalCal += totalCal(21)
                  myTotalFat += totalFat(21)
                  myTotalCarb += totalCarb(21)
                  myTotalProtein += totalProtein(21)
                  myId += 22
                  println(s"${foodList(21)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("23") => {
                  myFoodList += foodList(22)
                  myTotalCal += totalCal(22)
                  myTotalFat += totalFat(22)
                  myTotalCarb += totalCarb(22)
                  myTotalProtein += totalProtein(22)
                  myId += 23
                  println(s"${foodList(22)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("24") => {
                  myFoodList += foodList(23)
                  myTotalCal += totalCal(23)
                  myTotalFat += totalFat(23)
                  myTotalCarb += totalCarb(23)
                  myTotalProtein += totalProtein(23)
                  myId += 24
                  println(s"${foodList(23)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("25") => {
                  myFoodList += foodList(24)
                  myTotalCal += totalCal(24)
                  myTotalFat += totalFat(24)
                  myTotalCarb += totalCarb(24)
                  myTotalProtein += totalProtein(24)
                  myId += 25
                  println(s"${foodList(24)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("26") => {
                  myFoodList += foodList(25)
                  myTotalCal += totalCal(25)
                  myTotalFat += totalFat(25)
                  myTotalCarb += totalCarb(25)
                  myTotalProtein += totalProtein(25)
                  myId += 26
                  println(s"${foodList(25)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("27") => {
                  myFoodList += foodList(26)
                  myTotalCal += totalCal(26)
                  myTotalFat += totalFat(26)
                  myTotalCarb += totalCarb(26)
                  myTotalProtein += totalProtein(26)
                  myId += 27
                  println(s"${foodList(26)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("28") => {
                  myFoodList += foodList(27)
                  myTotalCal += totalCal(27)
                  myTotalFat += totalFat(27)
                  myTotalCarb += totalCarb(27)
                  myTotalProtein += totalProtein(27)
                  myId += 28
                  println(s"${foodList(27)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("29") => {
                  myFoodList += foodList(28)
                  myTotalCal += totalCal(28)
                  myTotalFat += totalFat(28)
                  myTotalCarb += totalCarb(28)
                  myTotalProtein += totalProtein(28)
                  myId += 29
                  println(s"${foodList(28)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("30") => {
                  myFoodList += foodList(29)
                  myTotalCal += totalCal(29)
                  myTotalFat += totalFat(29)
                  myTotalCarb += totalCarb(29)
                  myTotalProtein += totalProtein(29)
                  myId += 30
                  println(s"${foodList(29)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("31") => {
                  myFoodList += foodList(30)
                  myTotalCal += totalCal(30)
                  myTotalFat += totalFat(30)
                  myTotalCarb += totalCarb(30)
                  myTotalProtein += totalProtein(30)
                  myId += 31
                  println(s"${foodList(30)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("32") => {
                  myFoodList += foodList(31)
                  myTotalCal += totalCal(31)
                  myTotalFat += totalFat(31)
                  myTotalCarb += totalCarb(31)
                  myTotalProtein += totalProtein(31)
                  myId += 32
                  println(s"${foodList(31)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("33") => {
                  myFoodList += foodList(32)
                  myTotalCal += totalCal(32)
                  myTotalFat += totalFat(32)
                  myTotalCarb += totalCarb(32)
                  myTotalProtein += totalProtein(32)
                  myId += 33
                  println(s"${foodList(32)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("34") => {
                  myFoodList += foodList(33)
                  myTotalCal += totalCal(33)
                  myTotalFat += totalFat(33)
                  myTotalCarb += totalCarb(33)
                  myTotalProtein += totalProtein(33)
                  myId += 34
                  println(s"${foodList(33)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("35") => {
                  myFoodList += foodList(34)
                  myTotalCal += totalCal(34)
                  myTotalFat += totalFat(34)
                  myTotalCarb += totalCarb(34)
                  myTotalProtein += totalProtein(34)
                  myId += 35
                  println(s"${foodList(34)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("36") => {
                  myFoodList += foodList(35)
                  myTotalCal += totalCal(35)
                  myTotalFat += totalFat(35)
                  myTotalCarb += totalCarb(35)
                  myTotalProtein += totalProtein(35)
                  myId += 36
                  println(s"${foodList(35)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("37") => {
                  myFoodList += foodList(36)
                  myTotalCal += totalCal(36)
                  myTotalFat += totalFat(36)
                  myTotalCarb += totalCarb(36)
                  myTotalProtein += totalProtein(36)
                  myId += 37
                  println(s"${foodList(36)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("38") => {
                  myFoodList += foodList(37)
                  myTotalCal += totalCal(37)
                  myTotalFat += totalFat(37)
                  myTotalCarb += totalCarb(37)
                  myTotalProtein += totalProtein(37)
                  myId += 38
                  println(s"${foodList(37)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("39") => {
                  myFoodList += foodList(38)
                  myTotalCal += totalCal(38)
                  myTotalFat += totalFat(38)
                  myTotalCarb += totalCarb(38)
                  myTotalProtein += totalProtein(38)
                  myId += 39
                  println(s"${foodList(38)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("40") => {
                  myFoodList += foodList(39)
                  myTotalCal += totalCal(39)
                  myTotalFat += totalFat(39)
                  myTotalCarb += totalCarb(39)
                  myTotalProtein += totalProtein(39)
                  myId += 40
                  println(s"${foodList(39)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("41") => {
                  myFoodList += foodList(40)
                  myTotalCal += totalCal(40)
                  myTotalFat += totalFat(40)
                  myTotalCarb += totalCarb(40)
                  myTotalProtein += totalProtein(40)
                  myId += 41
                  println(s"${foodList(40)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("42") => {
                  myFoodList += foodList(41)
                  myTotalCal += totalCal(41)
                  myTotalFat += totalFat(41)
                  myTotalCarb += totalCarb(41)
                  myTotalProtein += totalProtein(41)
                  myId += 42
                  println(s"${foodList(41)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("43") => {
                  myFoodList += foodList(42)
                  myTotalCal += totalCal(42)
                  myTotalFat += totalFat(42)
                  myTotalCarb += totalCarb(42)
                  myTotalProtein += totalProtein(42)
                  myId += 43
                  println(s"${foodList(42)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("44") => {
                  myFoodList += foodList(43)
                  myTotalCal += totalCal(43)
                  myTotalFat += totalFat(43)
                  myTotalCarb += totalCarb(43)
                  myTotalProtein += totalProtein(43)
                  myId += 44
                  println(s"${foodList(43)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("45") => {
                  myFoodList += foodList(44)
                  myTotalCal += totalCal(44)
                  myTotalFat += totalFat(44)
                  myTotalCarb += totalCarb(44)
                  myTotalProtein += totalProtein(44)
                  myId += 45
                  println(s"${foodList(44)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("46") => {
                  myFoodList += foodList(45)
                  myTotalCal += totalCal(45)
                  myTotalFat += totalFat(45)
                  myTotalCarb += totalCarb(45)
                  myTotalProtein += totalProtein(45)
                  myId += 46
                  println(s"${foodList(45)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("47") => {
                  myFoodList += foodList(46)
                  myTotalCal += totalCal(46)
                  myTotalFat += totalFat(46)
                  myTotalCarb += totalCarb(46)
                  myTotalProtein += totalProtein(46)
                  myId += 47
                  println(s"${foodList(46)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("48") => {
                  myFoodList += foodList(47)
                  myTotalCal += totalCal(47)
                  myTotalFat += totalFat(47)
                  myTotalCarb += totalCarb(47)
                  myTotalProtein += totalProtein(47)
                  myId += 48
                  println(s"${foodList(47)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("49") => {
                  myFoodList += foodList(48)
                  myTotalCal += totalCal(48)
                  myTotalFat += totalFat(48)
                  myTotalCarb += totalCarb(48)
                  myTotalProtein += totalProtein(48)
                  myId += 49
                  println(s"${foodList(48)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("50") => {
                  myFoodList += foodList(49)
                  myTotalCal += totalCal(49)
                  myTotalFat += totalFat(49)
                  myTotalCarb += totalCarb(49)
                  myTotalProtein += totalProtein(49)
                  myId += 50
                  println(s"${foodList(49)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("51") => {
                  myFoodList += foodList(50)
                  myTotalCal += totalCal(50)
                  myTotalFat += totalFat(50)
                  myTotalCarb += totalCarb(50)
                  myTotalProtein += totalProtein(50)
                  myId += 51
                  println(s"${foodList(50)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("52") => {
                  myFoodList += foodList(51)
                  myTotalCal += totalCal(51)
                  myTotalFat += totalFat(51)
                  myTotalCarb += totalCarb(51)
                  myTotalProtein += totalProtein(51)
                  myId += 52
                  println(s"${foodList(51)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("53") => {
                  myFoodList += foodList(52)
                  myTotalCal += totalCal(52)
                  myTotalFat += totalFat(52)
                  myTotalCarb += totalCarb(52)
                  myTotalProtein += totalProtein(52)
                  myId += 53
                  println(s"${foodList(52)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("54") => {
                  myFoodList += foodList(53)
                  myTotalCal += totalCal(53)
                  myTotalFat += totalFat(53)
                  myTotalCarb += totalCarb(53)
                  myTotalProtein += totalProtein(53)
                  myId += 54
                  println(s"${foodList(53)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("55") => {
                  myFoodList += foodList(54)
                  myTotalCal += totalCal(54)
                  myTotalFat += totalFat(54)
                  myTotalCarb += totalCarb(54)
                  myTotalProtein += totalProtein(54)
                  myId += 55
                  println(s"${foodList(54)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("56") => {
                  myFoodList += foodList(55)
                  myTotalCal += totalCal(55)
                  myTotalFat += totalFat(55)
                  myTotalCarb += totalCarb(55)
                  myTotalProtein += totalProtein(55)
                  myId += 56
                  println(s"${foodList(55)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("57") => {
                  myFoodList += foodList(56)
                  myTotalCal += totalCal(56)
                  myTotalFat += totalFat(56)
                  myTotalCarb += totalCarb(56)
                  myTotalProtein += totalProtein(56)
                  myId += 57
                  println(s"${foodList(56)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("58") => {
                  myFoodList += foodList(57)
                  myTotalCal += totalCal(57)
                  myTotalFat += totalFat(57)
                  myTotalCarb += totalCarb(57)
                  myTotalProtein += totalProtein(57)
                  myId += 58
                  println(s"${foodList(57)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("59") => {
                  myFoodList += foodList(58)
                  myTotalCal += totalCal(58)
                  myTotalFat += totalFat(58)
                  myTotalCarb += totalCarb(58)
                  myTotalProtein += totalProtein(58)
                  myId += 59
                  println(s"${foodList(58)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("60") => {
                  myFoodList += foodList(59)
                  myTotalCal += totalCal(59)
                  myTotalFat += totalFat(59)
                  myTotalCarb += totalCarb(59)
                  myTotalProtein += totalProtein(59)
                  myId += 60
                  println(s"${foodList(59)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("61") => {
                  myFoodList += foodList(60)
                  myTotalCal += totalCal(60)
                  myTotalFat += totalFat(60)
                  myTotalCarb += totalCarb(60)
                  myTotalProtein += totalProtein(60)
                  myId += 61
                  println(s"${foodList(60)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("62") => {
                  myFoodList += foodList(61)
                  myTotalCal += totalCal(61)
                  myTotalFat += totalFat(61)
                  myTotalCarb += totalCarb(61)
                  myTotalProtein += totalProtein(61)
                  myId += 62
                  println(s"${foodList(61)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("63") => {
                  myFoodList += foodList(62)
                  myTotalCal += totalCal(62)
                  myTotalFat += totalFat(62)
                  myTotalCarb += totalCarb(62)
                  myTotalProtein += totalProtein(62)
                  myId += 63
                  println(s"${foodList(62)} stored in current list \n")
                }
                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("64") => {
                  myFoodList += foodList(63)
                  myTotalCal += totalCal(63)
                  myTotalFat += totalFat(63)
                  myTotalCarb += totalCarb(63)
                  myTotalProtein += totalProtein(63)
                  myId += 64
                  println(s"${foodList(63)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("65") => {
                  myFoodList += foodList(64)
                  myTotalCal += totalCal(64)
                  myTotalFat += totalFat(64)
                  myTotalCarb += totalCarb(64)
                  myTotalProtein += totalProtein(64)
                  myId += 65
                  println(s"${foodList(64)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("66") => {
                  myFoodList += foodList(65)
                  myTotalCal += totalCal(65)
                  myTotalFat += totalFat(65)
                  myTotalCarb += totalCarb(65)
                  myTotalProtein += totalProtein(65)
                  myId += 66
                  println(s"${foodList(65)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("67") => {
                  myFoodList += foodList(66)
                  myTotalCal += totalCal(66)
                  myTotalFat += totalFat(66)
                  myTotalCarb += totalCarb(66)
                  myTotalProtein += totalProtein(66)
                  myId += 67
                  println(s"${foodList(66)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("68") => {
                  myFoodList += foodList(67)
                  myTotalCal += totalCal(67)
                  myTotalFat += totalFat(67)
                  myTotalCarb += totalCarb(67)
                  myTotalProtein += totalProtein(67)
                  myId += 68
                  println(s"${foodList(67)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("69") => {
                  myFoodList += foodList(68)
                  myTotalCal += totalCal(68)
                  myTotalFat += totalFat(68)
                  myTotalCarb += totalCarb(68)
                  myTotalProtein += totalProtein(68)
                  myId += 69
                  println(s"${foodList(68)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("70") => {
                  myFoodList += foodList(69)
                  myTotalCal += totalCal(69)
                  myTotalFat += totalFat(69)
                  myTotalCarb += totalCarb(69)
                  myTotalProtein += totalProtein(69)
                  myId += 70
                  println(s"${foodList(69)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("71") => {
                  myFoodList += foodList(70)
                  myTotalCal += totalCal(70)
                  myTotalFat += totalFat(70)
                  myTotalCarb += totalCarb(70)
                  myTotalProtein += totalProtein(70)
                  myId += 71
                  println(s"${foodList(70)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("72") => {
                  myFoodList += foodList(71)
                  myTotalCal += totalCal(71)
                  myTotalFat += totalFat(71)
                  myTotalCarb += totalCarb(71)
                  myTotalProtein += totalProtein(71)
                  myId += 72
                  println(s"${foodList(71)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("73") => {
                  myFoodList += foodList(72)
                  myTotalCal += totalCal(72)
                  myTotalFat += totalFat(72)
                  myTotalCarb += totalCarb(72)
                  myTotalProtein += totalProtein(72)
                  myId += 73
                  println(s"${foodList(72)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("74") => {
                  myFoodList += foodList(73)
                  myTotalCal += totalCal(73)
                  myTotalFat += totalFat(73)
                  myTotalCarb += totalCarb(73)
                  myTotalProtein += totalProtein(73)
                  myId += 74
                  println(s"${foodList(73)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("75") => {
                  myFoodList += foodList(74)
                  myTotalCal += totalCal(74)
                  myTotalFat += totalFat(74)
                  myTotalCarb += totalCarb(74)
                  myTotalProtein += totalProtein(74)
                  myId += 75
                  println(s"${foodList(74)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("76") => {
                  myFoodList += foodList(75)
                  myTotalCal += totalCal(75)
                  myTotalFat += totalFat(75)
                  myTotalCarb += totalCarb(75)
                  myTotalProtein += totalProtein(75)
                  myId += 76
                  println(s"${foodList(75)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("77") => {
                  myFoodList += foodList(76)
                  myTotalCal += totalCal(76)
                  myTotalFat += totalFat(76)
                  myTotalCarb += totalCarb(76)
                  myTotalProtein += totalProtein(76)
                  myId += 77
                  println(s"${foodList(76)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("78") => {
                  myFoodList += foodList(77)
                  myTotalCal += totalCal(77)
                  myTotalFat += totalFat(77)
                  myTotalCarb += totalCarb(77)
                  myTotalProtein += totalProtein(77)
                  myId += 78
                  println(s"${foodList(77)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("79") => {
                  myFoodList += foodList(78)
                  myTotalCal += totalCal(78)
                  myTotalFat += totalFat(78)
                  myTotalCarb += totalCarb(78)
                  myTotalProtein += totalProtein(78)
                  myId += 79
                  println(s"${foodList(78)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("80") => {
                  myFoodList += foodList(79)
                  myTotalCal += totalCal(79)
                  myTotalFat += totalFat(79)
                  myTotalCarb += totalCarb(79)
                  myTotalProtein += totalProtein(79)
                  myId += 80
                  println(s"${foodList(79)} stored in current list \n")
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("81") => {
                  myFoodList += foodList(80)
                  myTotalCal += totalCal(80)
                  myTotalFat += totalFat(80)
                  myTotalCarb += totalCarb(80)
                  myTotalProtein += totalProtein(80)
                  myId += 81
                  println(s"${foodList(80)} stored in current list \n")
                }


                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("delete") => {
                  if (myFoodList.isEmpty) {
                    println("There is nothing to delete. \n")
                  }
                  else {
                    try {
                      myFoodList -= foodList(arg.toInt - 1)
                      myTotalCal -= totalCal(arg.toInt - 1)
                      myTotalFat -= totalFat(arg.toInt - 1)
                      myTotalCarb -= totalCarb(arg.toInt - 1)
                      myTotalProtein -= totalProtein(arg.toInt - 1)
                      myId -= arg.toInt

                      println(s"${foodList(arg.toInt - 1)} is removed from your current list \n")
                    }
                    catch {
                      case _: Throwable => println("Invalid command. Please try again \n")
                    }
                  }
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("clear") => {
                  if (arg.equals("current")) {
                    myFoodList.clear()
                    myTotalCal.clear()
                    myTotalProtein.clear()
                    myTotalCarb.clear()
                    myTotalFat.clear()
                    myId.clear()
                  }
                  else {
                    println("Invalid command. Please try again \n")
                  }
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("show") => {
                  if (arg.equals("current")) {
                    var num = 1
                    var index = 0
                    println("YOUR CURRENT LIST:")
                    if (myFoodList.isEmpty) {
                      println("")
                    }

                    for (element <- myFoodList) {
                      println(s"$num. $element (${myId(index)})")
                      index += 1
                      num += 1
                    }
                    var retCal, retFat, retCarbs, retProtein = 0
                    for (i <- myTotalCal) retCal += i
                    for (i <- myTotalFat) retFat += i
                    for (i <- myTotalCarb) retCarbs += i
                    for (i <- myTotalProtein) retProtein += i

                    println("==========")

                    println(s"Total calories intake: $retCal cal")
                    println(s"Total Fat intake: ${retFat}g")
                    println(s"Total Carbs intake: ${retCarbs}g")
                    println(s"Total Protein intake: ${retProtein}g")

                    println("========== \n")
                  }
                  else if(arg.equals("all")) {
                    printList()
                  }
                  else {
                    println("Invalid command. Please try again \n")
                  }

                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("save") => {
                  var num = 0
                  if (arg.equals("food")) {
                    for (i <- 1 to myFoodList.size) {
                      // getting the current date
                      val format = new SimpleDateFormat("M/dd/y")
                      var date = ""
                      date = format.format(Calendar.getInstance().getTime())

                      dao.insertFood(date, myFoodList(num) , myTotalCal(num), myTotalFat(num), myTotalCarb(num), myTotalProtein(num))
                      num += 1
                    }
                  }
                  else if (arg.equals("calories")) {
                    // getting the current date
                    val format = new SimpleDateFormat("M/dd/y")
                    var date = ""
                    date = format.format(Calendar.getInstance().getTime())

                    var retCal, retFat, retCarbs, retProtein = 0
                    for (i <- myTotalCal) retCal += i
                    for (i <- myTotalFat) retFat += i
                    for (i <- myTotalCarb) retCarbs += i
                    for (i <- myTotalProtein) retProtein += i

                    dao2.insertCalorie(date, retCal, retFat, retCarbs, retProtein)
                  }
                  else {
                    println("Invalid command. Please try again \n")
                  }

                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("reset") => {
                  if (arg.equals("food")) {
                    dao.deleteAll();
                  }
                  else if (arg.equals("calories")) {
                    dao2.deleteAll();
                  }
                  else {
                    println("Invalid command. Please try again")
                  }
                }

                case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("back") => continueMenuLoop2 = false
                case commandArgPattern(cmd, arg) => println(s"$cmd $arg is an invalid command. Please try again. \n")
                case _ =>
              }
            }
            while(continueMenuLoop2);
          } catch {
            case fnf : FileNotFoundException => println(s"Failed to find file $arg. Please enter a valid filename.\n")
          }
        }

        case commandArgPattern(cmd, arg) if cmd.equalsIgnoreCase("exit") => {
          println("exiting... ")
          println("goodbye")
          continueMenuLoop = false
        }
        case commandArgPattern(cmd, arg) => println(s"$cmd $arg is an invalid command. Please try again.\n")
      }
    }
  }
}
