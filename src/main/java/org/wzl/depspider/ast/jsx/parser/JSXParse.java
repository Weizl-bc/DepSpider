package org.wzl.depspider.ast.jsx.parser;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.wzl.depspider.ast.core.tokenizer.Token;
import org.wzl.depspider.ast.core.tokenizer.TokenType;
import org.wzl.depspider.ast.exception.CodeIllegalException;
import org.wzl.depspider.ast.jsx.parser.enumerate.SourceType;
import org.wzl.depspider.ast.jsx.parser.enumerate.SpecifierType;
import org.wzl.depspider.ast.jsx.parser.node.FileNode;
import org.wzl.depspider.ast.jsx.parser.node.ProgramNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrowFunctionExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.CallExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Extra;
import org.wzl.depspider.ast.jsx.parser.node.definition.Identifier;
import org.wzl.depspider.ast.jsx.parser.node.definition.ImportExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Loc;
import org.wzl.depspider.ast.jsx.parser.node.definition.Node;
import org.wzl.depspider.ast.jsx.parser.node.definition.Position;
import org.wzl.depspider.ast.jsx.parser.node.definition.ArrayExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.Expression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectExpression;
import org.wzl.depspider.ast.jsx.parser.node.definition.ObjectProperty;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ExportDefaultDeclaration;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.VariableDeclarator;
import org.wzl.depspider.ast.jsx.parser.node.definition.literal.NumericLiteral;
import org.wzl.depspider.ast.jsx.parser.node.definition.literal.StringLiteral;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.ImportDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.declaration.VariableDeclarationNode;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.ImportSpecifier;
import org.wzl.depspider.ast.jsx.parser.node.definition.specifier.Specifier;
import org.wzl.depspider.ast.jsx.tokenizer.JSXToken;
import org.wzl.depspider.ast.jsx.tokenizer.JSXTokenizer;
import org.wzl.depspider.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JSXParse {

    private List<Token> tokens;

    private final JSXTokenizer jsxTokenizer = new JSXTokenizer();

    private final int tokenSize;

    @Getter
    private Boolean isImportOnly = false;

    /**
     * 当前token的索引位置
     */
    private int tokenIndex = 0;


    private final static List<String> VARIABLE_KEY_WORD =
            new ArrayList<>();
    static {
        VARIABLE_KEY_WORD.add("const");
        VARIABLE_KEY_WORD.add("let");
        VARIABLE_KEY_WORD.add("var");
    }

    /**
     * 获取下一个token，并且index++
     * @return  Token
     */
    protected Token nextToken() {
        if (tokenIndex >= tokenSize) {
            return null;
        }
        Token token = tokens.get(tokenIndex);
        tokenIndex++;
        return token;
    }

    /**
     * 获取下一个token，index不变
     * @return  Token
     */
    protected Token peekNextToken() {
        if (tokenIndex + 1 >= tokenSize) {
            return null;
        }
        return tokens.get(tokenIndex + 1);
    }

    /**
     * 是否到达token的末尾
     * @return  boolean
     */
    protected boolean isAtEnd() {
        return tokenIndex >= tokenSize;
    }

    /**
     * 获取当前token
     * @return Token
     */
    protected Token peekToken() {
        if (tokenIndex >= tokenSize) {
            return null;
        }
        return tokens.get(tokenIndex);
    }

    public JSXParse(String filePath) {
        String inputString;
        try {
            inputString = FileUtil.getInputString(filePath);
        } catch (Exception e) {
            log.info("code file not found");
            throw new CodeIllegalException("code file not found");
        }
        jsxTokenizer.setSource(inputString);

        tokens = jsxTokenizer.tokenize();
        tokenSize = tokens.size();
//        tokens.forEach(System.out::println);
    }

    public FileNode parse() {
        return getFileNode();
    }

    public FileNode parse(Boolean isImportOnly) {
        this.isImportOnly = isImportOnly;
        return getFileNode();
    }

    private FileNode getFileNode() {
        Token eofToken = tokens.get(tokenSize - 1);
        int fileStartIndex = 0, fileEndIndex = eofToken.getEndIndex();
        FileNode fileNode = new FileNode(
                fileStartIndex,
                fileEndIndex,
                new Loc(
                        new Position(0, 0, 0),
                        new Position(eofToken.getLine(), eofToken.getColumn(), eofToken.getEndIndex())
                )
        );

        ProgramNode programNode = getProgramNode(fileStartIndex, fileEndIndex, eofToken);

        fileNode.setProgram(programNode);
        return fileNode;
    }

    /**
     * 获取ProgramNode
     * @param fileStartIndex 文件开始位置
     * @param fileEndIndex 文件结束位置
     * @param eofToken 文件结束符
     * @return ProgramNode
     */
    private ProgramNode getProgramNode(int fileStartIndex, int fileEndIndex, Token eofToken) {
        ProgramNode programNode = new ProgramNode(
                fileStartIndex,
                fileEndIndex,
                new Loc(
                        new Position(0, 0, 0),
                        new Position(eofToken.getLine(), eofToken.getColumn(), eofToken.getEndIndex())
                )
        );

        //暂时设置为null
        programNode.setInterpreter(null);
        //设置源代码类型
        programNode.setSourceType(currentSourceType());
        //设置body
        programNode.setBody(getProgramBody());
        return programNode;
    }

    /**
     * 获取ProgramNode的body
     * @return List<?>
     */
    private List<Node> getProgramBody() {
        List<Node> body = new ArrayList<>();

        while (true) {
            Token token = nextToken();
            if (token == null) {
                break;
            }
            TokenType type = token.getType();
            String value = token.getValue();

            if (type.equals(JSXToken.Type.EOF)) {// 遇到EOF，结束解析
                return body;
            }

            if (type.equals(JSXToken.Type.KEYWORD)) {
                // 关键字
                if (JSXTokenizer.KEYWORDS.contains(value)) {
                    if (value.equals("import")) {
                        Node importNode = importDeclaration();
                        body.add(importNode);
                    }
                    if (value.equals("export")) {
                        Node exportNode = exportDeclaration(token);
                        if (exportNode != null) {
                            body.add(exportNode);
                        }
                    }
                    if (isImportOnly && !value.equals("import")) {
                        break;
                    }
                    if (VARIABLE_KEY_WORD.contains(value)) {
                        Node variableDeclaration = variableDeclaration(token);
                        body.add(variableDeclaration);
                    }
                }
                //变量/普通标识符

            }
        }
        return body;
    }

    private Node variableDeclaration(Token kindToken) {
        List<VariableDeclarator> declarators = new ArrayList<>();

        int declarationStart = kindToken.getStartIndex();
        Position declarationStartPos = new Position(kindToken.getLine(), kindToken.getColumn(), kindToken.getStartIndex());
        Token lastToken = kindToken;

        while (!isAtEnd()) {
            Token identifierToken = nextToken();
            if (identifierToken == null) {
                break;
            }
            if (!identifierToken.getType().equals(JSXToken.Type.IDENTIFIER)) {
                lastToken = identifierToken;
                if (identifierToken.getType().equals(JSXToken.Type.EOF)) {
                    break;
                }
                continue;
            }

            Identifier identifier = buildIdentifier(identifierToken);
            lastToken = identifierToken;

            Node initNode = null;
            Token equalsToken = peekToken();
            if (equalsToken != null && equalsToken.getType().equals(JSXToken.Type.OPERATOR) && "=".equals(equalsToken.getValue())) {
                nextToken();
                Token initToken = nextToken();
                if (initToken != null) {
                    ParsedNode parsedNode = parseInitializer(initToken);
                    initNode = parsedNode.node;
                    if (parsedNode.lastToken != null) {
                        lastToken = parsedNode.lastToken;
                    } else {
                        lastToken = initToken;
                    }
                }
            }

            VariableDeclarator declarator = new VariableDeclarator(
                    identifierToken.getStartIndex(),
                    lastToken.getEndIndex(),
                    new Loc(
                            new Position(identifierToken.getLine(), identifierToken.getColumn(), identifierToken.getStartIndex()),
                            new Position(lastToken.getLine(), lastToken.getColumn(), lastToken.getEndIndex())
                    )
            );
            declarator.setId(identifier);
            declarator.setInit(initNode);
            declarator.setKind(kindToken.getValue());
            declarators.add(declarator);

            Token separator = peekToken();
            if (separator != null && separator.getType().equals(JSXToken.Type.COMMA)) {
                lastToken = nextToken();
                continue;
            }
            break;
        }

        Token possibleTerminator = peekToken();
        if (isStatementTerminator(possibleTerminator)) {
            lastToken = nextToken();
        }

        Position endPos = new Position(lastToken.getLine(), lastToken.getColumn(), lastToken.getEndIndex());
        VariableDeclarationNode node = new VariableDeclarationNode(
                declarationStart,
                lastToken.getEndIndex(),
                new Loc(declarationStartPos, endPos)
        );
        node.setDeclarations(declarators);
        return node;
    }

    private ParsedNode parseInitializer(Token initToken) {
        TokenType tokenType = initToken.getType();
        if (tokenType.equals(JSXToken.Type.STRING)) {
            return new ParsedNode(getStringLiteral(initToken), initToken);
        }
        if (tokenType.equals(JSXToken.Type.NUMBER)) {
            return new ParsedNode(getNumericLiteral(initToken), initToken);
        }
        if (tokenType.equals(JSXToken.Type.IDENTIFIER)) {
            Token potentialParen = peekToken();
            if (potentialParen != null && potentialParen.getType().equals(JSXToken.Type.LEFT_PARENTHESIS)) {
                ParsedNode callExpression = parseCallExpression(initToken);
                if (callExpression != null) {
                    return callExpression;
                }
            }
            if (isArrowFunctionWithSingleParam()) {
                ParsedNode arrowFunction = parseArrowFunctionWithSingleParam(initToken);
                if (arrowFunction != null) {
                    return arrowFunction;
                }
                Token last = skipArrowFunctionBody(initToken);
                return new ParsedNode(null, last);
            }
            return new ParsedNode(buildIdentifier(initToken), initToken);
        }
        if (tokenType.equals(JSXToken.Type.LEFT_BRACE)) {
            return parseObjectExpression(initToken);
        }
        if (tokenType.equals(JSXToken.Type.LEFT_PARENTHESIS)) {
            if (isArrowFunctionWithParentheses()) {
                ParsedNode arrowFunction = parseArrowFunctionWithParentheses(initToken);
                if (arrowFunction != null) {
                    return arrowFunction;
                }
            }
            Token last = skipArrowFunctionWithParentheses(initToken);
            return new ParsedNode(null, last);
        }
        if (tokenType.equals(JSXToken.Type.KEYWORD) && "function".equals(initToken.getValue())) {
            Token last = skipFunctionExpression();
            return new ParsedNode(null, last);
        }
        if (tokenType.equals(JSXToken.Type.KEYWORD) && "import".equals(initToken.getValue())) {
            ParsedNode callExpression = parseCallExpression(initToken);
            if (callExpression != null) {
                return callExpression;
            }
            Token last = skipExpressionAfterFirst(initToken);
            return new ParsedNode(null, last);
        }
        if (tokenType.equals(JSXToken.Type.LEFT_BRACKET)) {
            return parseArrayExpression(initToken);
        }
        return new ParsedNode(null, initToken);
    }

    private Node exportDeclaration(Token exportToken) {
        Token next = nextToken();
        if (next == null) {
            return null;
        }

        if (!next.getType().equals(JSXToken.Type.KEYWORD) || !"default".equals(next.getValue())) {
            skipExportClause(next);
            Token possibleTerminator = peekToken();
            if (isStatementTerminator(possibleTerminator)) {
                nextToken();
            }
            return null;
        }

        Token declarationStart = nextToken();
        Token lastToken = declarationStart != null ? declarationStart : next;
        Node declarationNode = null;

        if (declarationStart != null) {
            ParsedNode parsed = parseInitializer(declarationStart);
            if (parsed != null) {
                if (parsed.node != null) {
                    declarationNode = parsed.node;
                }
                if (parsed.lastToken != null) {
                    lastToken = parsed.lastToken;
                }
            }
        }

        Token possibleTerminator = peekToken();
        if (isStatementTerminator(possibleTerminator)) {
            lastToken = nextToken();
        }

        if (lastToken == null) {
            lastToken = exportToken;
        }

        ExportDefaultDeclaration exportDefaultDeclaration = new ExportDefaultDeclaration(
                exportToken.getStartIndex(),
                lastToken.getEndIndex(),
                new Loc(
                        new Position(exportToken.getLine(), exportToken.getColumn(), exportToken.getStartIndex()),
                        new Position(lastToken.getLine(), lastToken.getColumn(), lastToken.getEndIndex())
                )
        );
        exportDefaultDeclaration.setExportKind("value");
        exportDefaultDeclaration.setDeclaration(declarationNode);
        return exportDefaultDeclaration;
    }

    private Token skipExportClause(Token firstToken) {
        if (firstToken == null) {
            return null;
        }
        if (firstToken.getType().equals(JSXToken.Type.LEFT_BRACE)) {
            return consumeBalanced(firstToken, JSXToken.Type.LEFT_BRACE, JSXToken.Type.RIGHT_BRACE);
        }
        return skipExpressionAfterFirst(firstToken);
    }

    private boolean isStatementTerminator(Token token) {
        if (token == null) {
            return false;
        }
        if (!";".equals(token.getValue())) {
            return false;
        }
        TokenType type = token.getType();
        return type.equals(JSXToken.Type.OPERATOR) || type.equals(JSXToken.Type.OPERATOR_OR_JSX_TAG_START);
    }

    private NumericLiteral getNumericLiteral(Token numberToken) {
        return new NumericLiteral(
                numberToken.getStartIndex(),
                numberToken.getEndIndex(),
                new Loc(
                        new Position(numberToken.getLine(), numberToken.getColumn(), numberToken.getStartIndex()),
                        new Position(numberToken.getLine(), numberToken.getColumn(), numberToken.getEndIndex())
                )
        );
    }

    private Identifier buildIdentifier(Token token) {
        return new Identifier(
                token.getStartIndex(),
                token.getEndIndex(),
                new Loc(
                        new Position(token.getLine(), token.getColumn(), token.getStartIndex()),
                        new Position(token.getLine(), token.getColumn(), token.getEndIndex())
                ),
                token.getValue()
        );
    }

    private ParsedNode parseObjectExpression(Token leftBraceToken) {
        List<ObjectProperty> properties = new ArrayList<>();
        Token lastToken = leftBraceToken;

        while (!isAtEnd()) {
            Token token = nextToken();
            if (token == null) {
                break;
            }
            if (token.getType().equals(JSXToken.Type.RIGHT_BRACE)) {
                lastToken = token;
                break;
            }
            if (token.getType().equals(JSXToken.Type.COMMA)) {
                continue;
            }
            if (!token.getType().equals(JSXToken.Type.IDENTIFIER) && !token.getType().equals(JSXToken.Type.STRING)) {
                lastToken = token;
                continue;
            }

            Token keyToken = token;
            Token valueToken = peekToken();
            Node valueNode = null;
            Token propertyEndToken = keyToken;
            boolean hasExplicitValue = false;

            Node keyValue = keyToken.getType().equals(JSXToken.Type.STRING)
                    ? getStringLiteral(keyToken)
                    : buildIdentifier(keyToken);

            if (valueToken != null) {
                TokenType valueType = valueToken.getType();
                if (!valueType.equals(JSXToken.Type.COMMA) && !valueType.equals(JSXToken.Type.RIGHT_BRACE)) {
                    Token firstValueToken = nextToken();
                    ParsedNode parsedValue = parseInitializer(firstValueToken);
                    hasExplicitValue = true;
                    if (parsedValue != null) {
                        if (parsedValue.node != null) {
                            valueNode = parsedValue.node;
                        }
                        if (parsedValue.lastToken != null) {
                            propertyEndToken = parsedValue.lastToken;
                        } else {
                            propertyEndToken = firstValueToken;
                        }
                    } else {
                        propertyEndToken = firstValueToken;
                    }
                }
            }

            ObjectProperty property = new ObjectProperty(
                    keyToken.getStartIndex(),
                    propertyEndToken.getEndIndex(),
                    new Loc(
                            new Position(keyToken.getLine(), keyToken.getColumn(), keyToken.getStartIndex()),
                            new Position(propertyEndToken.getLine(), propertyEndToken.getColumn(), propertyEndToken.getEndIndex())
                    )
            );
            property.setMethod(false);
            property.setComputed(false);
            property.setShorthand(!hasExplicitValue);
            property.setKey(keyValue);
            property.setValue(valueNode);
            properties.add(property);
            lastToken = propertyEndToken;
        }

        ObjectExpression objectExpression = new ObjectExpression(
                leftBraceToken.getStartIndex(),
                lastToken.getEndIndex(),
                new Loc(
                        new Position(leftBraceToken.getLine(), leftBraceToken.getColumn(), leftBraceToken.getStartIndex()),
                        new Position(lastToken.getLine(), lastToken.getColumn(), lastToken.getEndIndex())
                )
        );
        objectExpression.setProperties(properties);
        return new ParsedNode(objectExpression, lastToken);
    }

    private ParsedNode parseArrayExpression(Token leftBracketToken) {
        List<Expression> elements = new ArrayList<>();
        Token lastToken = leftBracketToken;

        while (!isAtEnd()) {
            Token token = nextToken();
            if (token == null) {
                break;
            }

            if (token.getType().equals(JSXToken.Type.RIGHT_BRACKET)) {
                lastToken = token;
                break;
            }

            if (token.getType().equals(JSXToken.Type.COMMA) || token.getType().equals(JSXToken.Type.COMMENT)) {
                continue;
            }

            ParsedNode parsedElement = parseArrayElement(token);
            if (parsedElement != null) {
                if (parsedElement.node instanceof Expression) {
                    elements.add((Expression) parsedElement.node);
                }
                if (parsedElement.lastToken != null) {
                    lastToken = parsedElement.lastToken;
                } else {
                    lastToken = token;
                }
            } else {
                lastToken = token;
            }
        }

        ArrayExpression arrayExpression = new ArrayExpression(
                leftBracketToken.getStartIndex(),
                lastToken.getEndIndex(),
                new Loc(
                        new Position(leftBracketToken.getLine(), leftBracketToken.getColumn(), leftBracketToken.getStartIndex()),
                        new Position(lastToken.getLine(), lastToken.getColumn(), lastToken.getEndIndex())
                )
        );
        arrayExpression.setElements(elements);
        return new ParsedNode(arrayExpression, lastToken);
    }

    private ParsedNode parseArrayElement(Token firstToken) {
        TokenType type = firstToken.getType();
        if (type.equals(JSXToken.Type.STRING)) {
            return new ParsedNode(getStringLiteral(firstToken), firstToken);
        }
        if (type.equals(JSXToken.Type.NUMBER)) {
            return new ParsedNode(getNumericLiteral(firstToken), firstToken);
        }
        if (type.equals(JSXToken.Type.IDENTIFIER)) {
            if (isArrowFunctionWithSingleParam()) {
                ParsedNode arrowFunction = parseArrowFunctionWithSingleParam(firstToken);
                if (arrowFunction != null) {
                    return arrowFunction;
                }
                Token last = skipArrowFunctionBody(firstToken);
                return new ParsedNode(null, last);
            }
            return new ParsedNode(buildIdentifier(firstToken), firstToken);
        }
        if (type.equals(JSXToken.Type.LEFT_BRACE)) {
            return parseObjectExpression(firstToken);
        }
        if (type.equals(JSXToken.Type.LEFT_BRACKET)) {
            return parseArrayExpression(firstToken);
        }
        if (type.equals(JSXToken.Type.LEFT_PARENTHESIS)) {
            if (isArrowFunctionWithParentheses()) {
                ParsedNode arrowFunction = parseArrowFunctionWithParentheses(firstToken);
                if (arrowFunction != null) {
                    return arrowFunction;
                }
            }
            Token last = skipArrowFunctionWithParentheses(firstToken);
            return new ParsedNode(null, last);
        }
        if (type.equals(JSXToken.Type.KEYWORD) && "function".equals(firstToken.getValue())) {
            Token last = skipFunctionExpression();
            return new ParsedNode(null, last);
        }
        if (type.equals(JSXToken.Type.KEYWORD) && "import".equals(firstToken.getValue())) {
            ParsedNode callExpression = parseCallExpression(firstToken);
            if (callExpression != null) {
                return callExpression;
            }
            Token last = skipExpressionAfterFirst(firstToken);
            return new ParsedNode(null, last);
        }

        Token last = skipExpressionAfterFirst(firstToken);
        return new ParsedNode(null, last);
    }

    private ParsedNode parseArrowFunctionWithSingleParam(Token parameterToken) {
        Token arrowEq = nextToken();
        Token arrowGt = nextToken();
        if (!isArrowOperator(arrowEq, arrowGt)) {
            return null;
        }

        ParsedNode bodyNode = null;
        Token bodyStart = nextToken();
        Token lastToken = arrowGt != null ? arrowGt : parameterToken;
        if (bodyStart != null) {
            bodyNode = parseArrowFunctionBody(bodyStart);
            if (bodyNode != null && bodyNode.lastToken != null) {
                lastToken = bodyNode.lastToken;
            } else {
                lastToken = bodyStart;
            }
        }

        ArrowFunctionExpression arrowFunctionExpression = new ArrowFunctionExpression(
                parameterToken.getStartIndex(),
                lastToken.getEndIndex(),
                new Loc(
                        new Position(parameterToken.getLine(), parameterToken.getColumn(), parameterToken.getStartIndex()),
                        new Position(lastToken.getLine(), lastToken.getColumn(), lastToken.getEndIndex())
                )
        );
        arrowFunctionExpression.setId(null);
        arrowFunctionExpression.setGenerator(false);
        arrowFunctionExpression.setAsync(false);

        List<Node> params = new ArrayList<>();
        params.add(buildIdentifier(parameterToken));
        arrowFunctionExpression.setParams(params);
        arrowFunctionExpression.setBody(bodyNode != null ? bodyNode.node : null);

        return new ParsedNode(arrowFunctionExpression, lastToken);
    }

    private ParsedNode parseArrowFunctionWithParentheses(Token leftParenToken) {
        List<Node> params = new ArrayList<>();
        Token paramsEndToken = collectArrowFunctionParameters(leftParenToken, params);
        if (paramsEndToken == null) {
            return null;
        }

        Token arrowEq = nextToken();
        Token arrowGt = nextToken();
        if (!isArrowOperator(arrowEq, arrowGt)) {
            return null;
        }

        ParsedNode bodyNode = null;
        Token bodyStart = nextToken();
        Token lastToken = arrowGt != null ? arrowGt : paramsEndToken;
        if (bodyStart != null) {
            bodyNode = parseArrowFunctionBody(bodyStart);
            if (bodyNode != null && bodyNode.lastToken != null) {
                lastToken = bodyNode.lastToken;
            } else {
                lastToken = bodyStart;
            }
        }

        ArrowFunctionExpression arrowFunctionExpression = new ArrowFunctionExpression(
                leftParenToken.getStartIndex(),
                lastToken.getEndIndex(),
                new Loc(
                        new Position(leftParenToken.getLine(), leftParenToken.getColumn(), leftParenToken.getStartIndex()),
                        new Position(lastToken.getLine(), lastToken.getColumn(), lastToken.getEndIndex())
                )
        );
        arrowFunctionExpression.setId(null);
        arrowFunctionExpression.setGenerator(false);
        arrowFunctionExpression.setAsync(false);
        arrowFunctionExpression.setParams(params);
        arrowFunctionExpression.setBody(bodyNode != null ? bodyNode.node : null);

        return new ParsedNode(arrowFunctionExpression, lastToken);
    }

    private Token collectArrowFunctionParameters(Token leftParenToken, List<Node> params) {
        int depth = 1;
        while (!isAtEnd()) {
            Token token = nextToken();
            if (token == null) {
                return null;
            }

            if (token.getType().equals(JSXToken.Type.LEFT_PARENTHESIS)) {
                depth++;
            } else if (token.getType().equals(JSXToken.Type.RIGHT_PARENTHESIS)) {
                depth--;
                if (depth == 0) {
                    return token;
                }
            }

            if (depth == 1) {
                if (token.getType().equals(JSXToken.Type.COMMA)) {
                    continue;
                }
                if (token.getType().equals(JSXToken.Type.IDENTIFIER)) {
                    params.add(buildIdentifier(token));
                }
            }

        }

        return null;
    }

    private ParsedNode parseArrowFunctionBody(Token bodyStart) {
        if (bodyStart.getType().equals(JSXToken.Type.LEFT_BRACE)) {
            Token last = consumeBalanced(bodyStart, JSXToken.Type.LEFT_BRACE, JSXToken.Type.RIGHT_BRACE);
            return new ParsedNode(null, last);
        }
        if (bodyStart.getType().equals(JSXToken.Type.LEFT_BRACKET)) {
            return parseArrayExpression(bodyStart);
        }
        if (bodyStart.getType().equals(JSXToken.Type.KEYWORD) && "import".equals(bodyStart.getValue())) {
            return parseCallExpression(bodyStart);
        }
        if (bodyStart.getType().equals(JSXToken.Type.IDENTIFIER)) {
            Token potentialParen = peekToken();
            if (potentialParen != null && potentialParen.getType().equals(JSXToken.Type.LEFT_PARENTHESIS)) {
                ParsedNode callExpression = parseCallExpression(bodyStart);
                if (callExpression != null) {
                    return callExpression;
                }
            }
            return new ParsedNode(buildIdentifier(bodyStart), bodyStart);
        }
        if (bodyStart.getType().equals(JSXToken.Type.STRING)) {
            return new ParsedNode(getStringLiteral(bodyStart), bodyStart);
        }
        if (bodyStart.getType().equals(JSXToken.Type.NUMBER)) {
            return new ParsedNode(getNumericLiteral(bodyStart), bodyStart);
        }
        if (bodyStart.getType().equals(JSXToken.Type.LEFT_PARENTHESIS)) {
            if (isArrowFunctionWithParentheses()) {
                ParsedNode nestedArrow = parseArrowFunctionWithParentheses(bodyStart);
                if (nestedArrow != null) {
                    return nestedArrow;
                }
            }
            ParsedNode grouped = parseCallArgument(bodyStart);
            if (grouped != null) {
                return grouped;
            }
        }
        return parseCallArgument(bodyStart);
    }

    private ParsedNode parseCallExpression(Token calleeToken) {
        Expression callee;
        if (calleeToken.getType().equals(JSXToken.Type.IDENTIFIER)) {
            callee = buildIdentifier(calleeToken);
        } else if (calleeToken.getType().equals(JSXToken.Type.KEYWORD) && "import".equals(calleeToken.getValue())) {
            callee = buildImportExpression(calleeToken);
        } else {
            return null;
        }

        Token openParen = nextToken();
        if (openParen == null || !openParen.getType().equals(JSXToken.Type.LEFT_PARENTHESIS)) {
            return new ParsedNode(callee, calleeToken);
        }

        List<Expression> arguments = new ArrayList<>();
        Token lastToken = openParen;

        while (!isAtEnd()) {
            Token token = nextToken();
            if (token == null) {
                break;
            }
            if (token.getType().equals(JSXToken.Type.RIGHT_PARENTHESIS)) {
                lastToken = token;
                break;
            }
            if (token.getType().equals(JSXToken.Type.COMMA)) {
                continue;
            }

            ParsedNode argument = parseCallArgument(token);
            if (argument != null) {
                if (argument.node instanceof Expression) {
                    arguments.add((Expression) argument.node);
                }
                if (argument.lastToken != null) {
                    lastToken = argument.lastToken;
                } else {
                    lastToken = token;
                }
            } else {
                lastToken = token;
            }
        }

        CallExpression callExpression = new CallExpression(
                calleeToken.getStartIndex(),
                lastToken.getEndIndex(),
                new Loc(
                        new Position(calleeToken.getLine(), calleeToken.getColumn(), calleeToken.getStartIndex()),
                        new Position(lastToken.getLine(), lastToken.getColumn(), lastToken.getEndIndex())
                )
        );
        callExpression.setCallee(callee);
        callExpression.setArguments(arguments);

        return new ParsedNode(callExpression, lastToken);
    }

    private ParsedNode parseCallArgument(Token firstToken) {
        TokenType type = firstToken.getType();
        if (type.equals(JSXToken.Type.STRING)) {
            return new ParsedNode(getStringLiteral(firstToken), firstToken);
        }
        if (type.equals(JSXToken.Type.NUMBER)) {
            return new ParsedNode(getNumericLiteral(firstToken), firstToken);
        }
        if (type.equals(JSXToken.Type.IDENTIFIER)) {
            if (isArrowFunctionWithSingleParam()) {
                ParsedNode arrow = parseArrowFunctionWithSingleParam(firstToken);
                if (arrow != null) {
                    return arrow;
                }
            }
            return new ParsedNode(buildIdentifier(firstToken), firstToken);
        }
        if (type.equals(JSXToken.Type.LEFT_BRACE)) {
            return parseObjectExpression(firstToken);
        }
        if (type.equals(JSXToken.Type.LEFT_BRACKET)) {
            return parseArrayExpression(firstToken);
        }
        if (type.equals(JSXToken.Type.LEFT_PARENTHESIS)) {
            if (isArrowFunctionWithParentheses()) {
                ParsedNode arrow = parseArrowFunctionWithParentheses(firstToken);
                if (arrow != null) {
                    return arrow;
                }
            }
            Token last = consumeBalanced(firstToken, JSXToken.Type.LEFT_PARENTHESIS, JSXToken.Type.RIGHT_PARENTHESIS);
            return new ParsedNode(null, last);
        }
        if (type.equals(JSXToken.Type.KEYWORD) && "function".equals(firstToken.getValue())) {
            Token last = skipFunctionExpression();
            return new ParsedNode(null, last);
        }
        if (type.equals(JSXToken.Type.KEYWORD) && "import".equals(firstToken.getValue())) {
            return parseCallExpression(firstToken);
        }

        Token last = skipExpressionAfterFirst(firstToken);
        return new ParsedNode(null, last);
    }

    private ImportExpression buildImportExpression(Token token) {
        return new ImportExpression(
                token.getStartIndex(),
                token.getEndIndex(),
                new Loc(
                        new Position(token.getLine(), token.getColumn(), token.getStartIndex()),
                        new Position(token.getLine(), token.getColumn(), token.getEndIndex())
                )
        );
    }

    private boolean isArrowOperator(Token eqToken, Token gtToken) {
        return eqToken != null
                && gtToken != null
                && eqToken.getType().equals(JSXToken.Type.OPERATOR)
                && "=".equals(eqToken.getValue())
                && gtToken.getType().equals(JSXToken.Type.OPERATOR_OR_JSX_TAG_START)
                && ">".equals(gtToken.getValue());
    }

    private boolean isArrowFunctionWithParentheses() {
        int index = tokenIndex;
        int depth = 1;
        while (index < tokenSize) {
            Token token = tokens.get(index);
            if (token.getType().equals(JSXToken.Type.LEFT_PARENTHESIS)) {
                depth++;
            } else if (token.getType().equals(JSXToken.Type.RIGHT_PARENTHESIS)) {
                depth--;
                if (depth == 0) {
                    if (index + 2 < tokenSize) {
                        Token eqToken = tokens.get(index + 1);
                        Token gtToken = tokens.get(index + 2);
                        return isArrowOperator(eqToken, gtToken);
                    }
                    return false;
                }
            }
            index++;
        }
        return false;
    }

    private boolean isArrowFunctionWithSingleParam() {
        Token arrowStart = peekToken();
        Token arrowEnd = peekNextToken();
        return arrowStart != null
                && arrowEnd != null
                && arrowStart.getType().equals(JSXToken.Type.OPERATOR)
                && "=".equals(arrowStart.getValue())
                && arrowEnd.getType().equals(JSXToken.Type.OPERATOR_OR_JSX_TAG_START)
                && ">".equals(arrowEnd.getValue());
    }

    private Token skipArrowFunctionBody(Token parameterToken) {
        nextToken(); // consume '=' from '=>'
        Token gtToken = nextToken();
        Token lastToken = gtToken != null ? gtToken : parameterToken;

        Token bodyStart = nextToken();
        if (bodyStart == null) {
            return lastToken;
        }

        lastToken = bodyStart;
        if (bodyStart.getType().equals(JSXToken.Type.LEFT_BRACE)) {
            lastToken = consumeBalanced(bodyStart, JSXToken.Type.LEFT_BRACE, JSXToken.Type.RIGHT_BRACE);
        } else {
            lastToken = skipExpressionAfterFirst(bodyStart);
        }
        return lastToken;
    }

    private Token skipArrowFunctionWithParentheses(Token leftParenToken) {
        Token lastToken = consumeBalanced(leftParenToken, JSXToken.Type.LEFT_PARENTHESIS, JSXToken.Type.RIGHT_PARENTHESIS);
        Token arrowEq = peekToken();
        if (arrowEq != null && arrowEq.getType().equals(JSXToken.Type.OPERATOR) && "=".equals(arrowEq.getValue())) {
            nextToken();
            Token arrowGt = nextToken();
            lastToken = arrowGt != null ? arrowGt : leftParenToken;
            Token bodyStart = nextToken();
            if (bodyStart != null) {
                if (bodyStart.getType().equals(JSXToken.Type.LEFT_BRACE)) {
                    lastToken = consumeBalanced(bodyStart, JSXToken.Type.LEFT_BRACE, JSXToken.Type.RIGHT_BRACE);
                } else {
                    lastToken = skipExpressionAfterFirst(bodyStart);
                }
            } else {
                lastToken = arrowGt != null ? arrowGt : leftParenToken;
            }
        }
        return lastToken;
    }

    private Token skipFunctionExpression() {
        Token lastToken = null;
        Token maybeName = peekToken();
        if (maybeName != null && maybeName.getType().equals(JSXToken.Type.IDENTIFIER)) {
            lastToken = nextToken();
        }

        Token paramsStart = peekToken();
        if (paramsStart != null && paramsStart.getType().equals(JSXToken.Type.LEFT_PARENTHESIS)) {
            lastToken = consumeBalanced(nextToken(), JSXToken.Type.LEFT_PARENTHESIS, JSXToken.Type.RIGHT_PARENTHESIS);
        }

        Token bodyStart = peekToken();
        if (bodyStart != null && bodyStart.getType().equals(JSXToken.Type.LEFT_BRACE)) {
            lastToken = consumeBalanced(nextToken(), JSXToken.Type.LEFT_BRACE, JSXToken.Type.RIGHT_BRACE);
        }

        return lastToken;
    }

    private Token consumeBalanced(Token openingToken, TokenType openType, TokenType closeType) {
        int depth = 1;
        Token lastToken = openingToken;
        while (!isAtEnd() && depth > 0) {
            Token token = nextToken();
            if (token == null) {
                break;
            }
            lastToken = token;
            if (token.getType().equals(openType)) {
                depth++;
            } else if (token.getType().equals(closeType)) {
                depth--;
            }
        }
        return lastToken;
    }

    private Token skipExpressionAfterFirst(Token firstToken) {
        Token lastToken = firstToken;
        int braceDepth = firstToken.getType().equals(JSXToken.Type.LEFT_BRACE) ? 1 : 0;
        int parenDepth = firstToken.getType().equals(JSXToken.Type.LEFT_PARENTHESIS) ? 1 : 0;
        int bracketDepth = firstToken.getType().equals(JSXToken.Type.LEFT_BRACKET) ? 1 : 0;

        while (!isAtEnd()) {
            Token next = peekToken();
            if (next == null) {
                break;
            }

            if (braceDepth == 0 && parenDepth == 0 && bracketDepth == 0) {
                TokenType type = next.getType();
                if (type.equals(JSXToken.Type.COMMA) || type.equals(JSXToken.Type.KEYWORD) || type.equals(JSXToken.Type.EOF)) {
                    break;
                }
            }

            Token consumed = nextToken();
            if (consumed == null) {
                break;
            }
            lastToken = consumed;

            if (consumed.getType().equals(JSXToken.Type.LEFT_BRACE)) {
                braceDepth++;
            } else if (consumed.getType().equals(JSXToken.Type.RIGHT_BRACE)) {
                if (braceDepth == 0) {
                    break;
                }
                braceDepth--;
                if (braceDepth == 0) {
                    Token potentialBreak = peekToken();
                    if (potentialBreak == null) {
                        break;
                    }
                    if (potentialBreak.getType().equals(JSXToken.Type.COMMA) || potentialBreak.getType().equals(JSXToken.Type.KEYWORD)) {
                        break;
                    }
                }
            } else if (consumed.getType().equals(JSXToken.Type.LEFT_PARENTHESIS)) {
                parenDepth++;
            } else if (consumed.getType().equals(JSXToken.Type.RIGHT_PARENTHESIS)) {
                if (parenDepth > 0) {
                    parenDepth--;
                }
            } else if (consumed.getType().equals(JSXToken.Type.LEFT_BRACKET)) {
                bracketDepth++;
            } else if (consumed.getType().equals(JSXToken.Type.RIGHT_BRACKET)) {
                if (bracketDepth == 0) {
                    break;
                }
                bracketDepth--;
                if (bracketDepth == 0) {
                    Token potentialBreak = peekToken();
                    if (potentialBreak == null) {
                        break;
                    }
                    TokenType potentialType = potentialBreak.getType();
                    if (potentialType.equals(JSXToken.Type.COMMA) || potentialType.equals(JSXToken.Type.KEYWORD)) {
                        break;
                    }
                }
            }
        }
        return lastToken;
    }

    private static class ParsedNode {
        private final Node node;
        private final Token lastToken;

        private ParsedNode(Node node, Token lastToken) {
            this.node = node;
            this.lastToken = lastToken;
        }
    }

    /**
     * 处理import声明
     * @return Node
     */
    private Node importDeclaration() {
        Token peekToken = nextToken();
        TokenType type = peekToken.getType();
        int startIndex = peekToken.getStartIndex();
        //import token的顶层节点
        ImportDeclarationNode importDeclarationNode = new ImportDeclarationNode(
                startIndex,
                0,
                null //TODO: 需要设置Loc
        );
        importDeclarationNode.setType("ImportDeclaration");

        List<Specifier> specifiers = new ArrayList<>();
        importDeclarationNode.setSpecifiers(specifiers);

        /*

            1、读取导入的内容

         */

        /*
            当import语句为 import { useCallback, useActionState } from 'react' 时
         */
        if (type.equals(JSXToken.Type.LEFT_BRACE)) {
            int endIndex = 0;
            List<Token> importedTokens = new ArrayList<>();
            while (!isAtEnd()) {
                Token token = nextToken();
                if (token.getType().equals(JSXToken.Type.RIGHT_BRACE)) {
                    endIndex = token.getEndIndex();
                    break;
                }

                if (token.getType().equals(JSXToken.Type.IDENTIFIER)) {
                    importedTokens.add(token);
                }
            }

            for (Token importedToken : importedTokens) {
                Specifier importSpecifier = getImportSpecifier(importedToken);
                specifiers.add(importSpecifier);
            }

        }
        /*
            当import语句为 import reactLogo from './assets/react.svg' 时
         */
        else if (type.equals(JSXToken.Type.IDENTIFIER)) {
            ImportSpecifier importSpecifier = new ImportSpecifier(
                    SpecifierType.IMPORT_DEFAULT_SPECIFIER,
                    startIndex,
                    peekToken.getEndIndex(),
                    new Loc(
                            new Position(peekToken.getLine(), peekToken.getColumn(), peekToken.getStartIndex()),
                            new Position(peekToken.getLine(), peekToken.getColumn(), peekToken.getEndIndex())
                    )
            );
            importSpecifier.setImported(new Identifier(
                    peekToken.getStartIndex(),
                    peekToken.getEndIndex(),
                    new Loc(
                            new Position(peekToken.getLine(), peekToken.getColumn(), peekToken.getStartIndex()),
                            new Position(peekToken.getLine(), peekToken.getColumn(), peekToken.getEndIndex())
                    ),
                    peekToken.getValue()
            ));
            specifiers.add(importSpecifier);

            //当 import React, { CSSProperties } from 'react' 这种情况时
            if (peekNextToken().getType().equals(JSXToken.Type.LEFT_BRACE)) {
                int endIndex = 0;
                List<Token> importedTokens = new ArrayList<>();
                while (!isAtEnd()) {
                    Token token = nextToken();
                    if (token.getType().equals(JSXToken.Type.RIGHT_BRACE)) {
                        endIndex = token.getEndIndex();
                        break;
                    }

                    if (token.getType().equals(JSXToken.Type.IDENTIFIER)) {
                        importedTokens.add(token);
                    }
                }

                for (Token importedToken : importedTokens) {
                    Specifier importSpecifierCur = getImportSpecifier(importedToken);
                    specifiers.add(importSpecifierCur);
                }
            }
        }

        /*

            2、读取导入的模块路径

         */
        //设置source，也就是 import从哪里导入的
        nextToken(); //跳过 from 关键字
        Token sourceToken = nextToken();
        StringLiteral source = getStringLiteral(sourceToken);

        importDeclarationNode.setSource(source);
        return importDeclarationNode;
    }

    private static StringLiteral getStringLiteral(Token sourceToken) {
        String value = sourceToken.getValue();
        // 结束位置待定
        return new StringLiteral(
                sourceToken.getStartIndex(),
                0, // 结束位置待定
                new Loc(
                        new Position(sourceToken.getLine(), sourceToken.getColumn(), sourceToken.getStartIndex()),
                        new Position(sourceToken.getLine(), sourceToken.getColumn(), sourceToken.getEndIndex())
                ),
                Extra.builder()
                        .raw(value).rawValue(value)
                        .build(),
                value
        );
    }

    /**
     * 获取ImportSpecifier
     * @param importedToken 导入的token
     * @return ImportSpecifier
     */
    private static Specifier getImportSpecifier(Token importedToken) {
        ImportSpecifier importSpecifier = new ImportSpecifier(
                SpecifierType.IMPORT_SPECIFIER,
                importedToken.getStartIndex(),
                importedToken.getEndIndex(),
                new Loc(
                        new Position(importedToken.getLine(), importedToken.getColumn(), importedToken.getStartIndex()),
                        new Position(importedToken.getLine(), importedToken.getColumn(), importedToken.getEndIndex())
                )
        );
        importSpecifier.setImported(new Identifier(
                importedToken.getStartIndex(),
                importedToken.getEndIndex(),
                new Loc(
                        new Position(importedToken.getLine(), importedToken.getColumn(), importedToken.getStartIndex()),
                        new Position(importedToken.getLine(), importedToken.getColumn(), importedToken.getEndIndex())
                ),
                importedToken.getValue()
        ));

        return importSpecifier;
    }

    /**
     * 获取当前的源代码类型
     *  1、module类型  2、script类型
     * @return SourceType
     */
    private SourceType currentSourceType() {
        //TODO: 目前只支持module类型
        return SourceType.MODULE;
    }

}
