package com.example.codepal.Services;

import java.util.Random;

public class CodePalAssistant {
    public static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = "";
    private static final String[] PYTHON_KEYWORDS = {
            "python", "py", "pyth", "import", "def ", "class ", "print(", "len(", "range(",
            "list", "dict", "tuple", "set", "string", "integer", "float", "boolean",
            "if __name__", "if name main", "pip install", "requirements.txt",
            "django", "flask", "numpy", "pandas", "matplotlib", "tensorflow", "pytorch",
            "list comprehension", "dictionary comprehension", "lambda", "decorator",
            "generator", "iterator", "exception", "try except", "with open", "json",
            "csv", "dataframe", "series", "array", "module", "package", "virtualenv",
            "conda", "jupyter", "notebook", "script", "syntax", "indentation",
            "pep8", "pep", "coding style", "pythonic", "zen of python"
    };
    private static final String[] REJECTION_PHRASES = {
            "I can only help with Python programming questions.",
            "This appears to be outside Python programming scope.",
            "Please ask about Python programming, syntax, libraries, or related topics.",
            "I'm specialized in Python programming assistance only.",
            "Let's focus on Python programming questions."
    };
    public static String getAuthorization() {
        return "Bearer " + OPENAI_API_KEY;
    }
    public static boolean isPythonRelated(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return false;
        }
        String filteredPrompt = prompt.toLowerCase().trim();

        for (String keyword : PYTHON_KEYWORDS) {
            if (filteredPrompt.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return (containsProgrammingPatterns(filteredPrompt) || containsCodeIndicators(filteredPrompt));
    }
    private static boolean containsProgrammingPatterns(String prompt) {
        String[] programmingPatterns = {
                "how to", "how do i", "what is", "why does", "explain", "example",
                "error", "exception", "bug", "fix", "solve", "problem",
                "code", "program", "script", "function", "method", "variable",
                "loop", "for ", "while ", "if ", "else", "elif", "return"
        };
        for (String pattern : programmingPatterns) {
            if (prompt.contains(pattern)) {
                // If it has programming patterns, check if it's likely programming-related
                return prompt.contains("program") || prompt.contains("code") ||
                        prompt.contains("function") || prompt.contains("variable");
            }
        }
        return false;
    }
    private static boolean containsCodeIndicators(String prompt) {
        // Check for code-like structures
        return prompt.contains("def ") || prompt.contains("class ") ||
                prompt.contains("import ") || prompt.contains("from ") ||
                prompt.contains(":") || prompt.contains("    ") || // 4 spaces for indentation
                prompt.contains("\t") || prompt.contains("print(") ||
                prompt.contains("=") && (prompt.contains("[]") || prompt.contains("{}"));
    }
    public static String createPythonFocusedSystemMessage() {
        return "You are a Python programming expert assistant. " +
                "Only answer questions related to Python programming, including: " +
                "Python syntax, libraries (Django, Flask, NumPy, Pandas, etc.), " +
                "code examples, debugging, best practices, PEP standards, and programming concepts. " +
                "If a question is not about Python programming, politely decline to answer " +
                "and remind the user that you only help with Python-related topics. " +
                "If your response includes any code, ALWAYS wrap the entire code block using triple backticks (```), " +
                "and specify the language as python (```python). " +
                "Do not provide code without proper code block formatting. " +
                "Keep responses focused, technical, and helpful for Python developers.";
    }
    public static String getRandomRejectionMessage() {
        Random random = new Random();
        int index = random.nextInt(REJECTION_PHRASES.length);
        return REJECTION_PHRASES[index];
    }
}
