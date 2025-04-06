import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Класс калькулятора
 */
public class Calculator {
  private static final String POWER = "^";
  private static final String MULTIPLICATION = "*";
  private static final String DIVIDE = "/";
  private static final String PLUS = "+";
  private static final String MINUS = "-";

  /**
   * Запустить калькулятор
   */
  public void run() {
    Scanner scan = new Scanner(System.in);

    System.out.println("1 - вветси пример\n" +
                       "2 - посмотреть историю\n" +
                       "3 - очистить историю");
    int userChoice = scan.nextInt();

    FileManager fileManager = new FileManager();

    switch (userChoice) {
      case 1: {
        System.out.println("Введи пример (между операциями должен быть пробел)");
        Scanner mathExampleInput = new Scanner(System.in);
        String mathExample = mathExampleInput.nextLine();
        calculate(mathExample);
        break;
      }
      case 2: {
        fileManager.readFile("history.txt");
        break;
      }
      case 3: {
        fileManager.clearFile("history.txt");
        break;
      }
      default: {
        System.out.println("Некорректный ввод");
        break;
      }
    }
  }

  /**
   * Вычислить
   *
   * @param mathExample пример
   */
  public void calculate(String mathExample) {
    ArrayList<String> splitMathExample = new ArrayList<>(Arrays.asList(mathExample.split(" "))); //Коневертация строки с примером в ArrayList, разделение по пробелам

    try {
      while (splitMathExample.stream().count() > 1) {
        if (splitMathExample.contains(POWER)) {
          splitMathExample = performOperation(splitMathExample, POWER);
        }
        else if (splitMathExample.contains(MULTIPLICATION) && !splitMathExample.contains(DIVIDE)) {
          splitMathExample = performOperation(splitMathExample, MULTIPLICATION);
        }
        else if (splitMathExample.contains(DIVIDE) && !splitMathExample.contains(MULTIPLICATION)) {
          splitMathExample = performOperation(splitMathExample, DIVIDE);
        }
        else if (splitMathExample.contains(MULTIPLICATION) && splitMathExample.contains(DIVIDE)) {
          if (splitMathExample.indexOf(MULTIPLICATION) < splitMathExample.indexOf(DIVIDE)) {
            splitMathExample = performOperation(splitMathExample, MULTIPLICATION);
          }
          else if (splitMathExample.indexOf(DIVIDE) < splitMathExample.indexOf(MULTIPLICATION)) {
            splitMathExample = performOperation(splitMathExample, DIVIDE);
          }
        }
        else if (splitMathExample.contains(PLUS) && !splitMathExample.contains(MINUS)) {
          splitMathExample = performOperation(splitMathExample, PLUS);
        }
        else if (splitMathExample.contains(MINUS) && !splitMathExample.contains(PLUS)) {
          splitMathExample = performOperation(splitMathExample, MINUS);
        }
        else if (splitMathExample.contains(PLUS) && splitMathExample.contains(MINUS)) {
          if (splitMathExample.indexOf(PLUS) < splitMathExample.indexOf(MINUS)) {
            splitMathExample = performOperation(splitMathExample, PLUS);
          }
          else if (splitMathExample.indexOf(MINUS) < splitMathExample.indexOf(PLUS)) {
            splitMathExample = performOperation(splitMathExample, MINUS);
          }
        }
      }

      String mathExampleResult = mathExample + " = " + splitMathExample.get(0);

      FileManager fileManager = new FileManager();
      fileManager.writeToFile("history.txt", mathExampleResult);

      System.out.println(mathExampleResult);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Выполнить операцию
   * @param mathExample пример, тип ArrayList
   * @param mathOperation математическая операция
   * @return
   */
  public ArrayList<String> performOperation(ArrayList<String> mathExample, String mathOperation) {
    int index = mathExample.indexOf(mathOperation);

    double leftNumber = Double.parseDouble(mathExample.get(index - 1));
    double rightNumber = Double.parseDouble(mathExample.get(index + 1));
    double operationResult = 0;

    switch (mathOperation) {
      case POWER:
        operationResult = Math.pow(leftNumber, rightNumber);
        modifyMathExample(mathExample, operationResult, index);
        break;
      case MULTIPLICATION:
        operationResult = leftNumber * rightNumber;
        modifyMathExample(mathExample, operationResult, index);
        break;
      case DIVIDE:
        if (rightNumber == 0) {
          throw new IllegalArgumentException("Деление на 0");
        }
        operationResult = leftNumber / rightNumber;
        modifyMathExample(mathExample, operationResult, index);
        break;
      case PLUS:
        operationResult = leftNumber + rightNumber;
        modifyMathExample(mathExample, operationResult, index);
        break;
      case MINUS:
        operationResult = leftNumber - rightNumber;
        modifyMathExample(mathExample, operationResult, index);
        break;
    }
    return mathExample;
  }

  /**
   * Изменить ArrayList в соответствии
   * @param mathExample математический пример, тип ArrayList
   * @param operationResult результат математической операции
   * @param index индекс математической операции
   */
  private void modifyMathExample(ArrayList<String> mathExample, double operationResult, int index) {
    mathExample.set(index, String.valueOf(operationResult));
    mathExample.remove(index + 1);
    mathExample.remove(index - 1);
  }
}

/**
 * Класс для работы с файлами
 */
class FileManager {
  /**
   * Записать данные в файл
   * @param fileName имя файла
   * @param content содержимое файла
   */
  public void writeToFile(String fileName, String content) {
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(fileName, true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    PrintWriter printWriter = new PrintWriter(fileWriter);

    String currentDate = getFormatCurrentDate();

    printWriter.println(currentDate + '|' + content);
    printWriter.close();
  }

  /**
   * Очистить файл
   * @param fileName имя файла
   */
  public void clearFile(String fileName) {
    try {
      FileWriter fileWriter = new FileWriter(fileName, false);
      PrintWriter printWriter = new PrintWriter(fileWriter);

      printWriter.print("");

      printWriter.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * получить текущую дату в формате dd-MM-yyyy HH:mm:ss
   * @return текуущая дата, тип String
   */
  private String getFormatCurrentDate() {
    LocalDateTime date = LocalDateTime.now();
    DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    return date.format(formatDate);
  }

  /**
   * Прочитать данные из файла
   * @param fileName имя файла
   */
  public void readFile(String fileName) {
    try {
      File file = new File(fileName);

      Scanner reader = new Scanner(file);

      while (reader.hasNextLine()) {
        String data = reader.nextLine();
        System.out.println(data);
      }
      reader.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }
}
