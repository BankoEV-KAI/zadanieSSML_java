import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.*;
import java.util.Set;
import java.util.ArrayList;




public class LogicFunctionEvaluator {

    private static int[] eSet;
    private static int[] truthTable;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int G = 4211; int N = 4;
        System.out.printf("Банько Е. В. ");
        System.out.printf("g = {%d}\n", G);
        System.out.printf("n = {%d}\n", N);

        System.out.println("1: 3, отрицание Лукасевича ~x");
        System.out.println("2: 1, конъюнкция x&y");
        System.out.println("3: 3, аналог СКНФ");



        //TODO: Зациклить?

        // Запрос у пользователя значения k и n
        System.out.print("Введите размерность логики k (k > 1): ");
        int k = scanner.nextInt();

        System.out.print("Введите количество переменных n (1 или 2): ");
        int n = scanner.nextInt();

        // Проверка правильности ввода k и n
        if (k <= 1 || n < 1 || n > 2) {
            System.out.println("Ошибка ввода. Пожалуйста, введите правильные значения для k и n.");
            return;
        }

        scanner.nextLine(); // очистка буфера

        // Запрос у пользователя функции
        System.out.println("Введите логическую функцию:");
        String inputFunction = scanner.nextLine();

        // Проверка правильности ввода функции
        if (!isValidFunction(inputFunction, n, k)) {
            System.out.println("Ошибка ввода. Пожалуйста, введите правильную логическую функцию.");
            return;
        }

        int rowCount = (int) Math.pow(k, n);
        truthTable = new int[rowCount];

        // Построение таблицы значений функции
        System.out.println("Таблица значений функции:");
        printTruthTable(inputFunction, n, k);

        // Вычисление и вывод на экран СКНФ функции
        System.out.println("аналог СКНФ функции:");
        String sknf = getSKNF(inputFunction, n, k);
        System.out.println(sknf);




        System.out.println("Ввод множества Е: ");
        String input = scanner.nextLine();
        eSet = new int[k]; // Создаем массив для хранения элементов множества Е
        String[] tokens = input.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            try {
                int number = Integer.parseInt(tokens[i]);
                if (number >= k) {
                    System.out.println("Ошибка ввода. Введите число, меньшее чем k.");
                    return;
                }
                eSet[i] = number;
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Введите целые числа.");
                return;
            }
        }

        saveE(eSet, n, k);


        scanner.close();
    }

    // Проверка правильности ввода функции
    public static boolean isValidFunction(String function, int n, int k) {
        // Проверка количества переменных и допустимых символов
        String validChars = "~&*0123456789";
        if (n == 1) {
            validChars += "x";
        } else {
            validChars += "xy";
        }
        int openBracketCount = 0;
        for (char c : function.toCharArray()) {
            if (c == '(') {
                openBracketCount++;
            } else if (c == ')') {
                openBracketCount--;
            }
            if (openBracketCount < 0) {
                return false; // не найдена соответствующая открывающаяся скобка
            }
            if (c != '(' && c != ')' && !validChars.contains(Character.toString(c))) {
                return false; // символ не является разрешенным
            }
        }
        if (openBracketCount != 0) {
            return false; // количество открывающихся и закрывающихся скобок не совпадает
        }

        // Проверка наличия оператора & в начале или в конце функции
        if (function.startsWith("&") || function.endsWith("&")) {
            System.out.println("&");
            return false;
        }

        // Проверка наличия оператора ~ в конце функции
        if (function.endsWith("~") || function.endsWith("*") || function.startsWith("*")) {
            System.out.println("~ or *");
            return false;
        }

        return true;
    }

    // Построение таблицы значений функции
    public static void printTruthTable(String function, int n, int k) {
        // Заголовок таблицы
        if (n == 1) {
            System.out.println("|  x  |  f  |");
        } else {
            System.out.println("|  x  |  y  |  f  |");
        }

        // Перебор всех возможных значений переменных
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                // Вычисление значения функции для текущих значений переменных
                int result = evaluateFunction(function, i, j, k);

                // Вывод строки таблицы
                if (n == 1) {
                    System.out.printf("|  %d  |  %d  |\n", i, result);
                    truthTable[i] = result;
                    break;
                } else {
                    System.out.printf("|  %d  |  %d  |  %d  |\n", i, j, result);
                    truthTable[j] = result;
                }
            }
        }
    }

    private static int evaluateSubExpression(String expression, int k) {
        // Проверяем наличие вложенных скобок
        while (expression.contains("(")) {
            int openingBracketIndex = expression.lastIndexOf("(");
            int closingBracketIndex = expression.indexOf(")", openingBracketIndex);
            String subExpression = expression.substring(openingBracketIndex + 1, closingBracketIndex);
            int result = evaluateSubExpression(subExpression, k);
            expression = expression.substring(0, openingBracketIndex) + result + expression.substring(closingBracketIndex + 1);
        }

        // Здесь остается только вычисление простых выражений без скобок
        return evaluateSimpleExpression(expression, k);
    }

    private static int evaluateSimpleExpression(String expression, int k) {

        // Вычисление значения функции
        while (expression.contains("~")) {
            int tildeIndex = expression.indexOf("~");
            int operand = Character.getNumericValue(expression.charAt(tildeIndex + 1));
            expression = expression.substring(0, tildeIndex) + (k - 1 - operand) + expression.substring(tildeIndex + 2);
        }

        // Обработка произведения
        while (expression.contains("*")) {
            int asteriskIndex = expression.indexOf("*");
            int leftOperand = Character.getNumericValue(expression.charAt(asteriskIndex - 1));
            int rightOperand = Character.getNumericValue(expression.charAt(asteriskIndex + 1));
            int composition = leftOperand * rightOperand;
            expression = expression.substring(0, asteriskIndex - 1) + composition + expression.substring(asteriskIndex + 2);
        }

        while (expression.contains("&")) {
            int ampersandIndex = expression.indexOf("&");
            int leftOperand = Character.getNumericValue(expression.charAt(ampersandIndex - 1));
            int rightOperand = Character.getNumericValue(expression.charAt(ampersandIndex + 1));
            int min = Math.min(leftOperand, rightOperand);
            expression = expression.substring(0, ampersandIndex - 1) + min + expression.substring(ampersandIndex + 2);
        }


        // Преобразование строки в число
        return Integer.parseInt(expression);
    }

    public static int evaluateFunction(String function, int x, int y, int k) {
        // Замена переменных в функции на их значения
        function = function.replaceAll("x", Integer.toString(x));
        function = function.replaceAll("y", Integer.toString(y));


        int result = evaluateSubExpression(function, k);
        if (result >= k) result %= k;


        // Вызываем метод для вычисления значения функции
        return result;
    }

    // Получение СКНФ функции
    public static String getSKNF(String function, int n, int k) {
        // В текущей реализации возвращается просто исходная функция
        StringBuilder analogCKNF = new StringBuilder();
        if (n == 1){
            for (int i = 0; i < k; i++){
                int result = evaluateFunction(function, i, 0, k);
                if (result == 0) { //J_i(x)
                    analogCKNF.append("~J_").append(i).append("(x)");
                } else if (result == k - 1) { //nothing
                } else { //result v ~J_i(x)
                    analogCKNF.append("(").append(result).append("v~J_").append(i).append("(x))");
                }
                if (i != k-1 && result != k-1) analogCKNF.append("&");
            }
        } else if (n == 2) {
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < k; j++) {
                    // Вычисление значения функции для текущих значений переменных
                    int result = evaluateFunction(function, i, j, k);
                    if (result == 0) { // ~J_i(x) v ~J_j(y)
                        analogCKNF.append("(~J_").append(i).append("(x)").append("v~J_").append(j).append("(y))");
                    } else if ( result == k - 1) { //nothing
                    } else { //result v ~J_i(x) v ~J_j(y)
                        analogCKNF.append("(").append(result).append("v~J_").append(i).append("(x)").append("v~J_").append(j).append("(y))");
                    }
                    if (i != k-1 && result != k-1) analogCKNF.append("&");
                }
            }
        }

        return analogCKNF.toString();
    }

    public static void saveE(int[] E, int n, int k) {
        boolean flag = true;
        /*
        Как происходит определения сохранения множества Е?
        1. Для функции одной переменной:
            1. Получение множества Е.
            2. Для каждого элемента Е находится значение функции с такими же входными данными
            3. Если результат функции есть в множестве Е, то множество сохраняется
            4. Если нет, то не сохраняется
         */

        if (n == 1) {
            int sizeE = E.length;
            for (int i = 0; i < E.length; i++) {
                int counter = 0;
                for (int j = 0; j < E.length; j++) {
                    if (truthTable[E[i]] == E[j]) {
                        counter++;
                    }
                }
                if (counter == 0) {
                    System.out.println("Не сохраняет");
                    return;
                }
            }
            System.out.println("Сохраняет");
            return;
        } else if (n == 2) {
            /*
        Как происходит определения сохранения множества Е?
        1. Для функции двух переменных:
            1. Получение множества Е.
            2. Составление эквивалентных пар, например, Е = (1,2) => эквивалентые пары (1,2)~(2,1)~(1,1)~(2,2)
            3. Результат функции на этих парах должен быть в Е, т.е truthTable[1][2] == E[0] || truthTable[1][2] == E[1] и так для каждой пары
            4. Если хоть в одной паре не выполняется 3 пункт, то множество сохраняется, иначе не сохраняется
            (1,2) k=3 => result[4] 2*3-1*3+1
            (1,2) k=4 => result[5] 8-4+1
            Если пара (1,2), то result[y*k - x*k + 1]
            еще обработка случая, когда число вызодит отрицательным, нужно домножить результат на (-1)

             */

            // Составляем эквивалентные пары
            for (int i = 0; i < E.length; i++) {
                for (int j = 0; j < E.length; j++) {
                    boolean found = false;
                    // Перебираем все эквивалентные пары для текущей пары переменных
                    for (int m = 0; m < E.length; m++) {
                        for (int l = 0; l < E.length; l++) {
                            int second = (k * i - k * j + 1);
                            if (second < 0) { second *=-1; }
                            if ((truthTable[k * j - k * i + 1] == E[m] || truthTable[second] == E[l]) && (E[i] != E[m] || E[j] != E[l])) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        System.out.println("Не сохраняет");
                        return;
                    }
                }

                System.out.println("Сохраняет");
                return;
            }
        }
    }

}


