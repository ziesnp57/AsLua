/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2023  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
package com.yongle.aslua.lua;


import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.util.TrieTree;


// 定义一个 public 类 LuaTextTokenizer
public class LuaTextTokenizer {

    // 定义一个静态的 TrieTree 类型的 keywords 变量
    private static TrieTree<Tokens> keywords;

    // 静态代码块，执行 doStaticInit 方法
    static {
        doStaticInit();
    }

    // 定义一个公共的 getTree 方法，返回 keywords 变量
    public static TrieTree<Tokens> getTree() {
        return keywords;
    }

    // 定义一些变量
    private CharSequence source;
    protected int bufferLen;
    private int line;
    private int column;
    private int index;
    protected int offset;
    protected int length;
    private Tokens currToken;
    private boolean lcCal;

    // 构造方法，传入一个 CharSequence 类型的 src 参数
    public LuaTextTokenizer(CharSequence src) {
        if (src == null) {
            throw new IllegalArgumentException("src can not be null");
        }
        this.source = src;
        init();
    }

    // 初始化方法
    private void init() {
        line = 0;
        column = 0;
        length = 0;
        index = 0;
        currToken = Tokens.WHITESPACE;
        lcCal = false;
        this.bufferLen = source.length();
    }

    // 设置是否计算行列
    public void setCalculateLineColumn(boolean cal) {
        this.lcCal = cal;
    }

    // 将当前 token 的长度减去指定的长度
    public void pushBack(int length) {
        if (length > getTokenLength()) {
            throw new IllegalArgumentException("pushBack length too large");
        }
        this.length -= length;
    }

    // 判断一个字符是否是 Java 标识符的一部分
    private boolean isIdentifierPart(char ch) {
        return MyCharacter.isJavaIdentifierPart(ch);
    }

    // 判断一个字符是否是 Java 标识符的开头
    private boolean isIdentifierStart(char ch) {
        return MyCharacter.isJavaIdentifierStart(ch);
    }

    // 获取当前 token 的文本
    public CharSequence getTokenText() {
        return source.subSequence(offset, offset + length);
    }

    // 获取当前 token 的长度
    public int getTokenLength() {
        return length;
    }

    // 获取当前行数
    public int getLine() {
        return line;
    }

    // 获取当前列数
    public int getColumn() {
        return column;
    }

    // 获取当前索引
    public int getIndex() {
        return index;
    }

    // 获取当前 token 类型
    public Tokens getToken() {
        return currToken;
    }

    // 获取指定位置的字符
    private char charAt(int i) {
        return source.charAt(i);
    }

    // 获取当前位置的字符
    private char charAt() {
        return source.charAt(offset + length);
    }

    // 获取下一个 token
    public Tokens nextToken() {
        return currToken = nextTokenInternal();
    }

    // 获取下一个 token 的内部实现
    private Tokens nextTokenInternal() {
        // 如果需要计算行列，则进行计算
        if (lcCal) {
            boolean r = false;
            for (int i = offset; i < offset + length; i++) {
                char ch = charAt(i);
                if (ch == '\r') {
                    r = true;
                    line++;
                    column = 0;
                } else if (ch == '\n') {
                    if (r) {
                        r = false;
                        continue;
                    }
                    line++;
                    column = 0;
                } else {
                    r = false;
                    column++;
                }
            }
        }
        // 更新索引和偏移量
        index = index + length;
        offset = offset + length;
        // 如果偏移量已经超出了 source 的长度，返回 EOF
        if (offset >= bufferLen) {
            return Tokens.EOF;
        }
        // 获取当前字符
        char ch = source.charAt(offset);
        length = 1;
        // 如果当前字符是换行符，返回 NEWLINE
        if (ch == '\n') {
            return Tokens.NEWLINE;
        } else if (ch == '\r') {
            scanNewline();
            return Tokens.NEWLINE;
        } else if (isWhitespace(ch)) {
            // 如果当前字符是空白字符，扫描连续的空白字符并返回 WHITESPACE
            char chLocal;
            while (offset + length < bufferLen && isWhitespace(chLocal = charAt(offset + length))) {
                if (chLocal == '\r' || chLocal == '\n') {
                    break;
                }
                length++;
            }
            return Tokens.WHITESPACE;
        } else {
            // 如果当前字符是标识符的开头，扫描标识符并返回对应的 token
            if (isIdentifierStart(ch)) {
                return scanIdentifier(ch);
            }
            // 如果当前字符是数字字符，扫描数字并返回对应的 token
            if (isPrimeDigit(ch)) {
                return scanNumber();
            }
            /* Scan usual symbols first */
            // 如果当前字符是分号，返回 SEMICOLON
            if (ch == ';') {
                return Tokens.SEMICOLON;
            } else if (ch == '(') {
                return Tokens.LPAREN;
            } else if (ch == ')') {
                return Tokens.RPAREN;
            } else if (ch == ':') {
                return Tokens.COLON;
            } else if (ch == '<') {
                return scanLT();
            } else if (ch == '>') {
                return scanGT();
            }
            /* Scan secondly symbols */
            switch (ch) {
                case '=' -> {
                    return scanOperatorTwo(Tokens.EQ);
                }
                case '.' -> {
                    return Tokens.DOT;
                }
                case '@' -> {
                    return Tokens.AT;
                }
                case '{' -> {
                    return Tokens.LBRACE;
                }
                case '}' -> {
                    return Tokens.RBRACE;
                }
                case '/' -> {
                    return scanDIV();
                }
                case '*' -> {
                    return scanOperatorTwo(Tokens.MULT);
                }
                case '-' -> {
                    return scanOperatorTwo(Tokens.MINUS);
                }
                case '+' -> {
                    return scanOperatorTwo(Tokens.PLUS);
                }
                case '[' -> {
                    return Tokens.LBRACK;
                }
                case ']' -> {
                    return Tokens.RBRACK;
                }
                case ',' -> {
                    return Tokens.COMMA;
                }
                case '!' -> {
                    return Tokens.NOT;
                }
                case '~' -> {
                    return Tokens.COMP;
                }
                case '?' -> {
                    return Tokens.QUESTION;
                }
                case '&' -> {
                    return scanOperatorTwo(Tokens.AND);
                }
                case '|' -> {
                    return scanOperatorTwo(Tokens.OR);
                }
                case '^' -> {
                    return scanOperatorTwo(Tokens.XOR);
                }
                case '%' -> {
                    return scanOperatorTwo(Tokens.MOD);
                }
                case '\'' -> {
                    scanCharLiteral();
                    return Tokens.CHARACTER_LITERAL;
                }
                case '\"' -> {
                    scanStringLiteral();
                    return Tokens.STRING;
                }
                default -> {
                    return Tokens.UNKNOWN;
                }
            }
        }
    }

    protected final void throwIfNeeded() {
        if (offset + length == bufferLen) {
            throw new RuntimeException("Token too long");
        }
    }

    protected void scanNewline() {
        if (offset + length < bufferLen && charAt(offset + length) == '\n') {
            length++;
        }
    }

    protected Tokens scanIdentifier(char ch) {
        TrieTree.Node<Tokens> n = keywords.root.map.get(ch);
        while (offset + length < bufferLen && isIdentifierPart(ch = charAt(offset + length))) {
            length++;
            n = n == null ? null : n.map.get(ch);
        }
        return n == null ? Tokens.IDENTIFIER : (n.token == null ? Tokens.IDENTIFIER : n.token);
    }

    protected void scanTrans() {
        throwIfNeeded();
        char ch = charAt();
        if (ch == '\\' || ch == 't' || ch == 'f' || ch == 'n' || ch == 'r' || ch == '0' || ch == '\"' || ch == '\''
                || ch == 'b') {
            length++;
        } else if (ch == 'u') {
            length++;
            for (int i = 0; i < 4; i++) {
                throwIfNeeded();
                if (!isDigit(charAt(offset + length))) {
                    return;
                }
                length++;
            }
        }
    }

    protected void scanStringLiteral() {
        throwIfNeeded();
        char ch;
        while (offset + length < bufferLen && (ch = charAt(offset + length)) != '\"') {
            if (ch == '\\') {
                length++;
                scanTrans();
            } else {
                if (ch == '\n') {
                    return;
                }
                length++;
            }
        }
        if (offset + length < bufferLen) {
            length++;
        }
    }

    protected void scanCharLiteral() {
        throwIfNeeded();
        char ch;
        while (offset + length < bufferLen && (ch = charAt(offset + length)) != '\'') {
            if (ch == '\\') {
                length++;
                scanTrans();
            } else {
                if (ch == '\n') {
                    return;
                }
                length++;
                throwIfNeeded();
            }
        }
        if (offset + length != bufferLen) {
            length++;
        }
    }

    protected Tokens scanNumber() {
        if (offset + length == bufferLen) {
            return Tokens.INTEGER_LITERAL;
        }
        boolean flag = false;
        char ch = charAt(offset);
        if (ch == '0') {
            if (charAt() == 'x') {
                length++;
            }
            flag = true;
        }
        while (offset + length < bufferLen && isDigit(charAt())) {
            length++;
        }
        if (offset + length == bufferLen) {
            return Tokens.INTEGER_LITERAL;
        }
        ch = charAt();
        if (ch == '.') {
            if (flag) {
                return Tokens.INTEGER_LITERAL;
            }
            if (offset + length + 1 == bufferLen) {
                return Tokens.INTEGER_LITERAL;
            }
            length++;
            throwIfNeeded();
            while (offset + length < bufferLen && isDigit(charAt())) {
                length++;
            }
            if (offset + length == bufferLen) {
                return Tokens.FLOATING_POINT_LITERAL;
            }
            ch = charAt();
            if (ch == 'e' || ch == 'E') {
                length++;
                throwIfNeeded();
                if (charAt() == '-' || charAt() == '+') {
                    length++;
                    throwIfNeeded();
                }
                while (offset + length < bufferLen && isPrimeDigit(charAt())) {
                    length++;
                }
                if (offset + length == bufferLen) {
                    return Tokens.FLOATING_POINT_LITERAL;
                }
                ch = charAt();
            }
            if (ch == 'f' || ch == 'F' || ch == 'D'
                    || ch == 'd') {
                length++;
            }
            return Tokens.FLOATING_POINT_LITERAL;
        } else if (ch == 'l' || ch == 'L') {
            length++;
            return Tokens.INTEGER_LITERAL;
        } else if (ch == 'F' || ch == 'f' || ch == 'D'
                || ch == 'd') {
            length++;
            return Tokens.FLOATING_POINT_LITERAL;
        } else {
            return Tokens.INTEGER_LITERAL;
        }
    }
    

    protected Tokens scanDIV() {
        if (offset + 1 == bufferLen) {
            return Tokens.DIV;
        }
        char ch = charAt();
        if (ch == '/') {
            length++;
            while (offset + length < bufferLen && charAt() != '\n') {
                length++;
            }
            return Tokens.LINE_COMMENT;
        } else if (ch == '*') {
            length++;
            char pre, curr = '?';
            boolean finished = false;
            while (offset + length < bufferLen) {
                pre = curr;
                curr = charAt();
                if (curr == '/' && pre == '*') {
                    length++;
                    finished = true;
                    break;
                }
                length++;
            }
            return finished ? Tokens.LONG_COMMENT_COMPLETE : Tokens.LONG_COMMENT_INCOMPLETE;
        } else {
            return Tokens.DIV;
        }
    }

    @SuppressWarnings("SameReturnValue")
    protected Tokens scanLT() {
        return Tokens.LT;
    }

    @SuppressWarnings("SameReturnValue")
    protected Tokens scanGT() {
        return Tokens.GT;
    }

    protected Tokens scanOperatorTwo(Tokens ifWrong) {
        return ifWrong;
    }

    public void reset(CharSequence src) {
        if (src == null) {
            throw new IllegalArgumentException();
        }
        this.source = src;
        line = 0;
        column = 0;
        length = 0;
        index = 0;
        offset = 0;
        currToken = Tokens.WHITESPACE;
        bufferLen = src.length();
    }

    protected static String[] sKeywords;

    protected static void doStaticInit() {
        sKeywords = new String[]{
                "and", "break","do","else", "elseif", "end", "false", "for", "function", "if", "in", "local", "nil",
                "not", "or", "repeat", "return", "then", "true", "until", "while"
        };
        Tokens[] sTokens = new Tokens[]{
                Tokens.AND, Tokens.BREAK, Tokens.DO, Tokens.ELSE, Tokens.ELSEIF, Tokens.END, Tokens.FALSE, Tokens.FOR,
                Tokens.FUNCTION, Tokens.IF, Tokens.IN, Tokens.LOCAL, Tokens.NIL, Tokens.NOT, Tokens.OR, Tokens.REPEAT,
                Tokens.RETURN, Tokens.THEN, Tokens.TRUE, Tokens.UNTIL, Tokens.WHILE
        };
        keywords = new TrieTree<>();
        for (int i = 0; i < sKeywords.length; i++) {
            keywords.put(sKeywords[i], sTokens[i]);
        }
    }

    protected static boolean isDigit(char c) {
        return ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'));
    }

    protected static boolean isPrimeDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    protected static boolean isWhitespace(char c) {
        return (c == '\t' || c == ' ' || c == '\f' || c == '\n' || c == '\r');
    }
}
