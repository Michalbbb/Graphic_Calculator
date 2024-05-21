package com.example.KalkulatorGraficzny;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.util.*;

@Controller
public class DrawingController { // > is power and < is plus


    @GetMapping("/generateLine")
    @ResponseBody
    public Map<String, Double> generateLine(@RequestParam("input") String input,@RequestParam("min") int min,@RequestParam("max") int max) {
        String trimmedInput=input.replaceAll("[ \n]",""); //Delete spaces and new lines

        Map<String, Double> response = new HashMap<>();
        if(trimmedInput.isEmpty()){
            response.put("error",0d);
            return response;
        }
        if(trimmedInput.contains("y")||trimmedInput.contains("Y")){
            response.put("error",1d);
            return response;
        }
        if(!isStringValid(trimmedInput)){
            response.put("error",2d);
            return response;
        }
        double xForCanvas=0d;
        double numberTracker=min;
        int iteratorTracker=0;
        while(iteratorTracker<101) {
            String normalizedString=normalize(trimmedInput);

            String stringWithSpaces=addSpaces(normalizedString);
            String finalString= swapLettersToNumber(stringWithSpaces,numberTracker);
            System.out.println("PRE PRN->"+finalString+"\n");
            System.out.println("RPN ->"+infixToRpn(finalString));
            double y = evaluateRpn(infixToRpn(finalString));

            if(min<0)xForCanvas=numberTracker-min;
            else xForCanvas=numberTracker;
            response.put(String.format("x%d", iteratorTracker), xForCanvas);
            response.put(String.format("y%d", iteratorTracker), y);
            iteratorTracker++;
            numberTracker += (max - min) / 100d;

        }


        return response;
    }
    private String swapLettersToNumber(String target,double number) {

        BigDecimal convert= BigDecimal.valueOf(number);
        String noLetters=target.replaceAll("[^0-9-/*<>() .,]", ""+convert.toPlainString());

        return noLetters;
    }
    private boolean isStringValid(String input) {
        for(int i=0;i<input.length();i++) {
            if(isNotSupported(input.charAt(i)))return false;
        }
        return true;
    }
    private String normalize(String target){
        String normalizedString="";
        for(int i=0;i<target.length();i++){
            normalizedString+=target.charAt(i);
            if(i+1<target.length()&&isDigit(target.charAt(i))){
                if(isLetter(target.charAt(i+1)))normalizedString+="*";
            }
            if(i+1<target.length()&&isEndOfBracket(target.charAt(i))){
                if(!isProperSymbol(target.charAt(i+1)))normalizedString+="*";
            }
            if(i+1<target.length()&&isStartOfBracket(target.charAt(i+1))){
                if(isDigit(target.charAt(i)))normalizedString+="*";
            }

        }
        return normalizedString;
    }
    private String addSpaces(String target){
        String withSpaces="";
        if(target.charAt(0)=='-'){withSpaces+="0 ";}
        for(int i=0;i<target.length();i++){
            withSpaces+=target.charAt(i);
            if(isSpaceBetween(target.charAt(i)))withSpaces+=" ";
            if(isLetter(target.charAt(i)))withSpaces+=" ";
            if(i+1<target.length()&&!isDigitOrSeparator(target.charAt(i+1))){
                withSpaces+=" ";
            }
            if(i>0&&target.charAt(i)=='-'&&isLetter(target.charAt(i-1))){
                withSpaces+=" ";
            }
        }
        return withSpaces;
    }
    private static boolean letterOrDigit(char c)
    {
        // boolean check
        if (Character.isLetterOrDigit(c)||c=='.'||c==',')
            return true;
        else
            return false;
    }

    // Operator having higher precedence
    // value will be returned
    static int getPrecedence(String cha)
    {
        char ch;
        if(cha.length()==1)
        ch=cha.charAt(0);
        else ch=cha.charAt(1);
        if (ch == '<' || ch == '-')
            return 1;
        else if (ch == '*' || ch == '/')
            return 2;
        else if (ch == '>')
            return 3;
        else
            return -1;
    }

    // Operator has Left --> Right associativity
    static boolean hasLeftAssociativity(String cha) {
        char ch;
        if(cha.length()==1)ch=cha.charAt(0);
        else ch=cha.charAt(1);
        if (ch == '<' || ch == '-' || ch == '/' || ch == '*') {
            return true;
        } else {
            return false;
        }
    }

    // Method converts  given infixto postfix expression
    // to illustrate shunting yard algorithm
    static String infixToRpn(String expression)
    {
        // Initialising an empty String
        // (for output) and an empty stack
        Stack<String> stack = new Stack<>();

        String[] tokens = expression.split("\\s+");

        // Initially empty string taken
        String output = new String("");

        // Iterating over tokens using inbuilt
        // .length() function
        for (String c: tokens) {
            char compareMe;
            if(c.length()==1)compareMe=c.charAt(0);
            else compareMe=c.charAt(1);
            // If the scanned Token is an
            // operand, add it to output
            if (letterOrDigit(compareMe))
                output += c+" ";

                // If the scanned Token is an '('
                // push it to the stack
            else if (compareMe == '(')
                stack.push(c);

                // If the scanned Token is an ')' pop and append
                // it to output from the stack until an '(' is
                // encountered
            else if (compareMe == ')') {
                while (!stack.isEmpty()
                        && !Objects.equals(stack.peek(), "(")) {
                    output += stack.pop()+" ";

                }
                if(!stack.isEmpty()) stack.pop();

            }

            // If an operator is encountered then taken the
            // further action based on the precedence of the
            // operator

            else {
                while (
                        !stack.isEmpty()
                                && getPrecedence(c)
                                <= getPrecedence(stack.peek())
                                && hasLeftAssociativity(c)) {
                    // peek() inbuilt stack function to
                    // fetch the top element(token)

                    output += stack.pop()+" ";
                }
                stack.push(c);
            }
        }

        // pop all the remaining operators from
        // the stack and append them to output
        while (!stack.isEmpty()) {
            if (stack.peek() == "(")
                return "This expression is invalid";
            output += stack.pop()+" ";
        }
        return output;
    }

    public static double evaluateRpn(String expression) {
        // Stack to hold numbers
        Stack<Double> stack = new Stack<>();

        // Split the expression into tokens
        String[] tokens = expression.split("\\s+");

        // Iterate through each token
        for (String token : tokens) {
            switch (token) {
                case "<":
                    // Pop the top two numbers, apply the operator, and push the result
                    stack.push(stack.pop() + stack.pop());
                    break;
                case "-":
                    // Pop the top two numbers, apply the operator (in the correct order), and push the result
                    double subtractor = stack.pop();
                    double minuend = stack.pop();
                    stack.push(minuend - subtractor);
                    break;
                case "*":
                    // Pop the top two numbers, apply the operator, and push the result
                    stack.push(stack.pop() * stack.pop());
                    break;
                case "/":
                    // Pop the top two numbers, apply the operator (in the correct order), and push the result
                    double divisor = stack.pop();
                    double dividend = stack.pop();
                    stack.push(dividend / divisor);
                    break;
                case ">":
                    double power = stack.pop();
                    double number = stack.pop();
                    stack.push(Math.pow(number, power));
                    break;
                default:
                    // Push the number onto the stack
                    stack.push(Double.parseDouble(token));
                    break;
            }
        }

        // The final result should be the only number left in the stack
        return stack.pop();
    }

    private boolean isLetter(char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }
    private boolean isDigitOrSeparator(char c) {
        return c == ',' || c == '.' ||(c >= '0' && c <= '9');
    }
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private boolean isStartOfBracket(char c){
        return c == '(';
    }
    private boolean isEndOfBracket(char c){
        return c == ')';
    }
    private boolean isProperSymbol(char c){
        return c == '<' || c == '>' || c == '-' || c == '/' || c == '*';
    }
    private boolean isSpaceBetween(char c){
        return c == '<' || c == '>' || c == '/' || c == '*' || c == ')' || c== '(';
    }
    private boolean isNotSupported(char c){
        if(isLetter(c)) return false;
        if(isDigitOrSeparator(c)) return false;
        if(isProperSymbol(c)) return false;
        if(isStartOfBracket(c)) return false;
        if(isEndOfBracket(c)) return false;
        return c != ' ';

    }

}