
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AICalculator extends JFrame {

    private JTextField input;
    private JTextArea history;

    public AICalculator() {
        setTitle("AI Calculator - Java");
        setSize(420, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel("AI CALCULATOR", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setBorder(BorderFactory.createEmptyBorder(15, 5, 10, 5));
        add(heading, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        input = new JTextField();
        input.setFont(new Font("Arial", Font.PLAIN, 20));
        input.setToolTipText("Enter an expression or mathematical question");
        main.add(input, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(5, 4, 7, 7));

        String[] keys = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+",
            "(", ")", "C", "DEL"
        };

        for (String key : keys) {
            JButton button = new JButton(key);
            button.setFont(new Font("Arial", Font.BOLD, 18));

            button.addActionListener(e -> handleButton(key));
            buttons.add(button);
        }

        main.add(buttons, BorderLayout.CENTER);

        history = new JTextArea(5, 20);
        history.setEditable(false);
        history.setFont(new Font("Monospaced", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(history);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(new JLabel("Calculation History"), BorderLayout.NORTH);
        bottom.add(scrollPane, BorderLayout.CENTER);

        JButton calculate = new JButton("CALCULATE");
        calculate.setFont(new Font("Arial", Font.BOLD, 16));
        bottom.add(calculate, BorderLayout.SOUTH);

        main.add(bottom, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);

        calculate.addActionListener(e -> calculate());
        input.addActionListener(e -> calculate());

        setVisible(true);
    }

    private void handleButton(String key) {
        switch (key) {
            case "=":
                calculate();
                break;

            case "C":
                input.setText("");
                break;

            case "DEL":
                String current = input.getText();
                if (!current.isEmpty()) {
                    input.setText(current.substring(0, current.length() - 1));
                }
                break;

            default:
                input.setText(input.getText() + key);
        }
    }

    private void calculate() {
        String expression = input.getText().trim();

        if (expression.isEmpty()) {
            return;
        }

        try {
            String cleaned = expression.toLowerCase()
                    .replace("what is", "")
                    .replace("calculate", "")
                    .replace("please", "")
                    .replace("?", "")
                    .replace("×", "*")
                    .replace("÷", "/");

            cleaned = cleaned.replace("square root of", "sqrt(");
            cleaned = cleaned.replace("squared", "^2");

            if (cleaned.contains("sqrt(")) {
                cleaned = cleaned + ")";
            }

            double result = new Parser(cleaned).parse();

            if (!Double.isFinite(result)) {
                throw new ArithmeticException("Invalid result");
            }

            String answer = String.format("%.10f", result)
                    .replaceAll("0+$", "")
                    .replaceAll("\\.$", "");

            history.append(expression + " = " + answer + "\n");
            input.setText(answer);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid expression. Please check your input.",
                    "Calculation Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Mathematical expression parser
    static class Parser {

        private final String text;
        private int position = -1;
        private char current;

        Parser(String text) {
            this.text = text;
            nextChar();
        }

        private void nextChar() {
            position++;
            current = position < text.length()
                    ? text.charAt(position) : '\0';
        }

        private boolean eat(char character) {
            while (current == ' ') {
                nextChar();
            }

            if (current == character) {
                nextChar();
                return true;
            }
            return false;
        }

        double parse() {
            double value = expression();

            if (current != '\0') {
                throw new RuntimeException("Unexpected character");
            }

            return value;
        }

        // Addition and subtraction
        private double expression() {
            double value = term();

            while (true) {
                if (eat('+')) {
                    value += term();
                } else if (eat('-')) {
                    value -= term();
                } else {
                    return value;
                }
            }
        }

        // Multiplication and division
        private double term() {
            double value = power();

            while (true) {
                if (eat('*')) {
                    value *= power();
                } else if (eat('/')) {
                    double divisor = power();

                    if (divisor == 0) {
                        throw new ArithmeticException("Division by zero");
                    }

                    value /= divisor;
                } else {
                    return value;
                }
            }
        }

        // Exponents
        private double power() {
            double value = unary();

            if (eat('^')) {
                value = Math.pow(value, power());
            }

            return value;
        }

        private double unary() {
            if (eat('+')) return unary();
            if (eat('-')) return -unary();

            return primary();
        }

        // Numbers, parentheses and mathematical functions
        private double primary() {

            if (eat('(')) {
                double value = expression();

                if (!eat(')')) {
                    throw new RuntimeException("Missing bracket");
                }

                return value;
            }

            if (Character.isDigit(current) || current == '.') {
                int start = position;

                while (Character.isDigit(current) || current == '.') {
                    nextChar();
                }

                return Double.parseDouble(
                        text.substring(start, position)
                );
            }

            if (Character.isLetter(current)) {
                int start = position;

                while (Character.isLetter(current)) {
                    nextChar();
                }

                String function = text.substring(start, position);

                if (function.equals("pi")) return Math.PI;
                if (function.equals("e")) return Math.E;

                if (!eat('(')) {
                    throw new RuntimeException("Function needs parentheses");
                }

                double argument = expression();

                if (!eat(')')) {
                    throw new RuntimeException("Missing bracket");
                }

                switch (function) {
                    case "sqrt":
                        return Math.sqrt(argument);
                    case "sin":
                        return Math.sin(Math.toRadians(argument));
                    case "cos":
                        return Math.cos(Math.toRadians(argument));
                    case "tan":
                        return Math.tan(Math.toRadians(argument));
                    case "log":
                        return Math.log10(argument);
                    case "ln":
                        return Math.log(argument);
                    case "abs":
                        return Math.abs(argument);
                    default:
                        throw new RuntimeException("Unknown function");
                }
            }

            throw new RuntimeException("Invalid expression");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AICalculator::new);
    }
}