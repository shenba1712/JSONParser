# JSON Parser

This is a simple JSON Parser built with Java and Spring Boot. It is built as part of John Crickett's [coding challenge](https://codingchallenges.fyi/challenges/challenge-json-parser).
It is built as an application server to mimic the behaviour of apps that would send JSON objects to services for processing. It basically validates the JSON and tells the user if it is valid or not.

And the magic behind this validation can be divided into two major steps:

## Lexical Analysis a.k.a Tokenization

Tokenizing is the process of breaking the input into smaller, understandable parts. It basically converts a JSON string into an array of tokens with predefined meanings.

This is needed for a number of reasons:

* Enables the parser to determine where to start and stop the parsing process, and how to process each token.
* Helps us understand the structure of the code or input being processed.
* Assists in error handling, especially with syntax checks.

###  How does this work?

Let’s imagine we have a JSON like this:

```(json)
{
"id": "647ceaf3657eade56f8224eb",
}
```

The above JSON is basically an object with:

* Opening brace
* String key “id”
* Colon
* String value "647ceaf3657eade56f8224eb"
* Closing braces

And the lexical analysis of this JSON will be:

```(json)
[
    { type: "BRACE_OPEN", value: "{" },
    { type: "STRING", value: "id" },
    { type: "COLON", value: ":" },
    { type: "STRING", value: "647ceaf3657eade56f8224eb" },
    { type: "BRACE_CLOSE", value: "}" }
]
```

In the above example, `BRACE_OPEN`, `STRING`, `COLON`, and `BRACE_CLOSE` are some of token types defined the code. We also have other values like numbers, booleans, arrays, nested objects, nulls, etc. 
The full list can be found [here](backend/src/main/java/org/com/backend/model/TokenType.java).

## Parsing

A parser converts the array of tokens into a data structure like Abstract Syntax Tree (AST).

The AST consists of nodes, each representing the syntactic construct of the JSON string. It neatly organizes the tokenized JSON data into key-value pairs and nested structures.

###  How does this work?

Let's take the same example from the Lexer section.

```
ObjectASTNode(
    value = {
        id = StringASTNode(value=647ceaf3657eade56f8224eb)
    }
)
```

This is the parser output of the tokenized JSON from the example above. Here, there are two nodes -> Object AST Node, which is the main node, and the String AST Node, which is the nested node.

It explains that an **Object AST Node** has a value that is a **String AST Node** with key `id` and value `647ceaf3657eade56f8224eb`.

Similarly, to generate our complete AST, we use the [method](backend/src/main/java/org/com/backend/serviceImpl/ParserServerImpl.java) that takes an array of tokens, iterates through it, and generates the AST according to the token.

Since the parser creates this AST structure, it also checks for errors and invalid syntatic expressions. For example:

* Invalid escape sequences
* Strings starting with 0
* Nesting levels greater that predefined limit.
* Unexpected tokens
* Unbalanced inputs (non-closed brackets and braces)
* ... and many more. (Check the `fail*.json` [test files](backend/src/test/resources/samples) to get an idea on the different error scenarios)

If there are no such errors found by the parser, then the AST generated is valid. Which also means that the JSON string is valid.

## API Call

```
POST http://localhost:8080/parser
Body: <JSON string>
```

For example:

```
POST http://localhost:8080/parser
Body:

{
"id": "647ceaf3657eade56f8224eb"
}
```

## Testing

The test uses the JSON files found in [samples folder](backend/src/test/resources/samples).
To run the tests, please change the file path to an absolute path, as the application only recognizes absolute file paths.
